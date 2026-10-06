package com.tasktracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private TaskRepository repo;
    
    private static final String UPLOAD_FOLDER = "uploads/";

    // Handle file size exceeded exception
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "File size exceeds maximum limit (10GB)");
        response.put("message", "Please upload files smaller than 10GB. For extremely large files, consider splitting them into smaller parts.");
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/{id}")
    public String uploadFile(@PathVariable Long id,
                             @RequestParam MultipartFile file) throws Exception {

        Task task = repo.findById(id).orElseThrow();

        // Upload folder
        File dir = new File(UPLOAD_FOLDER);
        if (!dir.exists()) {
            dir.mkdir();
        }

        // Save file
        Path path = Path.of(UPLOAD_FOLDER + file.getOriginalFilename());
        Files.write(path, file.getBytes());

        // Store file name in DB
        task.setUploadedFile(file.getOriginalFilename());
        repo.save(task);

        return "Uploaded Successfully: " + file.getOriginalFilename();
    }

    // Upload multiple files with enhanced support for all file types
    @PostMapping("/multiple/{id}")
    public Map<String, Object> uploadMultipleFiles(@PathVariable Long id,
                                               @RequestParam("files") MultipartFile[] files) throws Exception {
        
        Task task = repo.findById(id).orElseThrow();
        
        // Create task-specific upload folder
        String taskFolder = UPLOAD_FOLDER + "task_" + id + "/";
        File dir = new File(taskFolder);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        List<Map<String, String>> uploadedFiles = new ArrayList<>();
        int count = 0;
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String originalFilename = file.getOriginalFilename();
                
                // Handle different file types
                if (originalFilename != null && !originalFilename.trim().isEmpty()) {
                    String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;
                    Path filePath = Path.of(taskFolder + uniqueFilename);
                    
                    // Save file
                    Files.write(filePath, file.getBytes());
                    
                    // If it's a ZIP file, extract it
                    if (originalFilename.toLowerCase().endsWith(".zip")) {
                        extractZipFile(filePath, taskFolder);
                    }
                    
                    Map<String, String> fileInfo = new HashMap<>();
                    fileInfo.put("originalName", originalFilename);
                    fileInfo.put("savedName", uniqueFilename);
                    fileInfo.put("size", formatFileSize(file.getSize()));
                    fileInfo.put("type", getFileType(originalFilename));
                    
                    uploadedFiles.add(fileInfo);
                    count++;
                }
            }
        }
        
        // Store file information in task
        if (count > 0) {
            String fileList = uploadedFiles.stream()
                .map(f -> f.get("originalName"))
                .collect(Collectors.joining(", "));
            task.setUploadedFile(fileList);
            repo.save(task);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Successfully uploaded " + count + " files");
        response.put("files", uploadedFiles);
        response.put("taskId", id);
        
        return response;
    }

    // Extract ZIP files
    private void extractZipFile(Path zipPath, String extractTo) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    Path filePath = Path.of(extractTo + entry.getName());
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zis, filePath);
                }
            }
        }
    }

    // Get file type
    private String getFileType(String filename) {
        if (filename == null) return "unknown";
        
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return "PDF";
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) return "Word Document";
        if (lower.endsWith(".xls") || lower.endsWith(".xlsx")) return "Excel";
        if (lower.endsWith(".ppt") || lower.endsWith(".pptx")) return "PowerPoint";
        if (lower.endsWith(".txt")) return "Text";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif")) return "Image";
        if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mov")) return "Video";
        if (lower.endsWith(".mp3") || lower.endsWith(".wav")) return "Audio";
        if (lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".7z")) return "Archive";
        return "File";
    }

    // Format file size
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }

    // List files in task folder
    @GetMapping("/files/{taskId}")
    public Map<String, Object> getTaskFiles(@PathVariable Long taskId) {
        String taskFolder = UPLOAD_FOLDER + "task_" + taskId + "/";
        File folder = new File(taskFolder);
        
        List<Map<String, Object>> files = new ArrayList<>();
        
        if (folder.exists() && folder.isDirectory()) {
            File[] fileList = folder.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isFile()) {
                        Map<String, Object> fileInfo = new HashMap<>();
                        fileInfo.put("name", file.getName());
                        fileInfo.put("originalName", getOriginalName(file.getName()));
                        fileInfo.put("size", formatFileSize(file.length()));
                        fileInfo.put("type", getFileType(file.getName()));
                        fileInfo.put("lastModified", new Date(file.lastModified()));
                        fileInfo.put("downloadUrl", "/api/upload/download-file/" + taskId + "/" + file.getName());
                        files.add(fileInfo);
                    }
                }
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("files", files);
        response.put("taskId", taskId);
        response.put("totalFiles", files.size());
        
        return response;
    }

    // Get original name from saved filename
    private String getOriginalName(String savedName) {
        if (savedName.contains("_")) {
            return savedName.substring(savedName.indexOf("_") + 1);
        }
        return savedName;
    }

    // Download specific file from task folder
    @GetMapping("/download-file/{taskId}/{filename}")
    public ResponseEntity<Resource> downloadSpecificFile(@PathVariable Long taskId, 
                                                   @PathVariable String filename) throws IOException {
        String taskFolder = UPLOAD_FOLDER + "task_" + taskId + "/";
        File folder = new File(taskFolder);
        
        if (!folder.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        // Look for file that matches the original filename (may have timestamp prefix)
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith("_" + filename)) {
                    Resource resource = new FileSystemResource(file);
                    String contentType = Files.probeContentType(file.toPath());
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                    
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CONTENT_DISPOSITION, 
                                   "attachment; filename=\"" + filename + "\"")
                            .body(resource);
                }
                // Also check exact match for files without timestamp
                if (file.isFile() && file.getName().equals(filename)) {
                    Resource resource = new FileSystemResource(file);
                    String contentType = Files.probeContentType(file.toPath());
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                    
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CONTENT_DISPOSITION, 
                                   "attachment; filename=\"" + filename + "\"")
                            .body(resource);
                }
            }
        }
        
        return ResponseEntity.notFound().build();
    }

    // Download file
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws IOException {
        Task task = repo.findById(id).orElseThrow();
        
        if (task.getUploadedFile() == null || task.getUploadedFile().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = Paths.get(UPLOAD_FOLDER + task.getUploadedFile());
        Resource resource = new FileSystemResource(filePath.toFile());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + task.getUploadedFile() + "\"")
                .body(resource);
    }

    // Get all uploaded files list
    @GetMapping("/files")
    public java.util.List<Map<String, Object>> getAllUploadedFiles() {
        return repo.findAll().stream()
                .filter(task -> task.getUploadedFile() != null && !task.getUploadedFile().isEmpty())
                .map(task -> {
                    Map<String, Object> fileInfo = new java.util.HashMap<>();
                    fileInfo.put("taskId", task.getId());
                    fileInfo.put("taskTitle", task.getTitle());
                    fileInfo.put("fileName", task.getUploadedFile());
                    fileInfo.put("developer", task.getDeveloper());
                    return fileInfo;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    // Delete uploaded file
    @DeleteMapping("/delete/{id}")
    public String deleteUploadedFile(@PathVariable Long id) {
        Task task = repo.findById(id).orElseThrow();
        
        if (task.getUploadedFile() != null && !task.getUploadedFile().isEmpty()) {
            try {
                Path filePath = Paths.get(UPLOAD_FOLDER + task.getUploadedFile());
                Files.deleteIfExists(filePath);
                task.setUploadedFile(null);
                repo.save(task);
                return "File deleted successfully";
            } catch (IOException e) {
                return "Error deleting file: " + e.getMessage();
            }
        }
        
        return "No file to delete";
    }
}
