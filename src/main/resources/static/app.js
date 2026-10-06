// Advanced Task Management JavaScript

let developerChart = null;

// Load statistics and developers on page load
document.addEventListener('DOMContentLoaded', function() {
    loadStatistics();
    loadDevelopers();
});

// Load statistics
async function loadStatistics() {
    try {
        const response = await fetch('/api/reports/statistics');
        const stats = await response.json();
        
        document.getElementById('totalTasks').textContent = stats.totalTasks;
        document.getElementById('completedTasks').textContent = stats.completedTasks;
        document.getElementById('inProgressTasks').textContent = stats.inProgressTasks;
        document.getElementById('overdueTasks').textContent = stats.overdueTasks;
    } catch (error) {
        console.error('Error loading statistics:', error);
    }
}

// Load developers for dropdown
async function loadDevelopers() {
    try {
        const response = await fetch('/api/reports/developers');
        const developers = await response.json();
        
        const select = document.getElementById('developerSelect');
        developers.forEach(dev => {
            const option = document.createElement('option');
            option.value = dev;
            option.textContent = dev;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading developers:', error);
    }
}

// Update developer chart
async function updateDeveloperChart() {
    const selectedDeveloper = document.getElementById('developerSelect').value;
    if (!selectedDeveloper) {
        if (developerChart) {
            developerChart.destroy();
            developerChart = null;
        }
        return;
    }

    try {
        const response = await fetch(`/api/reports/developer-performance/${selectedDeveloper}`);
        const performance = await response.json();
        
        const ctx = document.getElementById('developerChart').getContext('2d');
        
        if (developerChart) {
            developerChart.destroy();
        }
        
        developerChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Completed', 'In Progress', 'In Review', 'To Do'],
                datasets: [{
                    data: [
                        performance.completedTasks || 0,
                        performance.inProgressTasks || 0,
                        performance.inReviewTasks || 0,
                        performance.todoTasks || 0
                    ],
                    backgroundColor: [
                        '#28a745',  // Green for Completed
                        '#ffc107',  // Yellow for In Progress
                        '#17a2b8',  // Blue for In Review
                        '#007bff'   // Blue for To Do
                    ],
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                devicePixelRatio: 1,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            boxWidth: 12,
                            padding: 10,
                            font: {
                                size: 11
                            }
                        }
                    },
                    title: {
                        display: true,
                        text: `${selectedDeveloper}'s Task Status`,
                        font: {
                            size: 14
                        }
                    }
                },
                animation: {
                    animateScale: false,
                    animateRotate: true
                }
            }
        });
    } catch (error) {
        console.error('Error updating developer chart:', error);
    }
}

// Open Create Task Modal
function openCreateTaskModal() {
    console.log('Opening create task modal...');
    try {
        const modalElement = document.getElementById('createTaskModal');
        if (modalElement) {
            const modal = new bootstrap.Modal(modalElement);
            modal.show();
            console.log('Modal should be visible now');
        } else {
            console.error('Modal element not found!');
            alert('Error: Modal not found');
        }
    } catch (error) {
        console.error('Error opening modal:', error);
        alert('Error opening modal: ' + error.message);
    }
}

// Create Task from Modal
async function createTaskFromModal() {
    const formData = {
        title: document.getElementById('modalTaskTitle').value,
        description: document.getElementById('modalTaskDescription').value,
        developer: document.getElementById('modalTaskDeveloper').value,
        teamLead: document.getElementById('modalTaskTeamLead').value,
        manager: document.getElementById('modalTaskManager').value,
        priority: document.getElementById('modalTaskPriority').value,
        dueDate: document.getElementById('modalTaskDueDate').value,
        sprintName: document.getElementById('modalTaskSprint').value,
        createdBy: document.getElementById('modalTaskCreatedBy').value
    };
    
    try {
        const params = new URLSearchParams();
        Object.entries(formData).forEach(([key, value]) => {
            if (value) params.append(key, value);
        });
        
        const response = await fetch('/api/tasks/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: params.toString()
        });
        
        if (response.ok) {
            // Clear form and close modal
            document.getElementById('createTaskModalForm').reset();
            bootstrap.Modal.getInstance(document.getElementById('createTaskModal')).hide();
            
            // Reload page to show new task
            location.reload();
        } else {
            alert('Error creating task');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error creating task');
    }
}

// Edit Task
function editTask(button) {
    const id = button.getAttribute('data-id');
    const title = button.getAttribute('data-title');
    const description = button.getAttribute('data-description');
    const developer = button.getAttribute('data-developer');
    const teamLead = button.getAttribute('data-teamlead');
    const manager = button.getAttribute('data-manager');
    const priority = button.getAttribute('data-priority');
    const dueDate = button.getAttribute('data-duedate');
    const sprint = button.getAttribute('data-sprint');
    const createdBy = button.getAttribute('data-createdby');
    
    document.getElementById('editTaskId').value = id;
    document.getElementById('editTaskTitle').value = title || '';
    document.getElementById('editTaskDescription').value = description || '';
    document.getElementById('editTaskDeveloper').value = developer || '';
    document.getElementById('editTaskTeamLead').value = teamLead || '';
    document.getElementById('editTaskManager').value = manager || '';
    document.getElementById('editTaskPriority').value = priority || 'MEDIUM';
    document.getElementById('editTaskDueDate').value = dueDate || '';
    document.getElementById('editTaskSprint').value = sprint || '';
    
    new bootstrap.Modal(document.getElementById('editTaskModal')).show();
}

// Save Task (from edit modal)
async function saveTask() {
    const id = document.getElementById('editTaskId').value;
    const formData = {
        title: document.getElementById('editTaskTitle').value,
        description: document.getElementById('editTaskDescription').value,
        priority: document.getElementById('editTaskPriority').value,
        dueDate: document.getElementById('editTaskDueDate').value,
        sprintName: document.getElementById('editTaskSprint').value,
        changedBy: document.getElementById('editTaskDeveloper').value
    };
    
    try {
        const params = new URLSearchParams();
        Object.entries(formData).forEach(([key, value]) => {
            if (value) params.append(key, value);
        });
        
        await fetch(`/api/tasks/update/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: params.toString()
        });
        
        // Update developer if changed
        const developer = document.getElementById('editTaskDeveloper').value;
        if (developer) {
            await fetch(`/api/tasks/assign/${id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `developer=${encodeURIComponent(developer)}&changedBy=${encodeURIComponent(developer)}`
            });
        }
        
        // Close modal and reload
        bootstrap.Modal.getInstance(document.getElementById('editTaskModal')).hide();
        location.reload();
    } catch (error) {
        console.error('Error:', error);
        alert('Error updating task');
    }
}

// Move to Next Status
async function nextStatus(button) {
    const id = button.getAttribute('data-id');
    
    try {
        const response = await fetch(`/api/status/next/${id}`, {
            method: 'PUT'
        });
        
        if (response.ok) {
            location.reload();
        } else {
            alert('Error updating status');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error updating status');
    }
}

// Move to Previous Status
async function previousStatus(button) {
    const id = button.getAttribute('data-id');
    
    try {
        const response = await fetch(`/api/status/previous/${id}`, {
            method: 'PUT'
        });
        
        if (response.ok) {
            location.reload();
        } else {
            alert('Error updating status');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error updating status');
    }
}

// Add Comment
function addComment(button) {
    const id = button.getAttribute('data-id');
    const title = button.getAttribute('data-title');
    
    document.getElementById('commentTaskId').value = id;
    document.getElementById('commentText').value = '';
    document.getElementById('commentAuthor').value = '';
    
    new bootstrap.Modal(document.getElementById('commentModal')).show();
}

// Save Comment
async function saveComment() {
    const id = document.getElementById('commentTaskId').value;
    const comment = document.getElementById('commentText').value;
    const author = document.getElementById('commentAuthor').value;
    
    try {
        const response = await fetch(`/api/tasks/${id}/comment`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `comment=${encodeURIComponent(comment)}&author=${encodeURIComponent(author)}`
        });
        
        if (response.ok) {
            bootstrap.Modal.getInstance(document.getElementById('commentModal')).hide();
            alert('Comment added successfully');
        } else {
            alert('Error adding comment');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error adding comment');
    }
}

// Format file size for display
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// Upload Multiple Files (Enhanced for all file sizes including >2GB)
async function uploadMultipleFiles(input, taskId) {
    if (input.files && input.files.length > 0) {
        // Check file sizes before upload
        const maxSize = 10 * 1024 * 1024 * 1024; // 10GB in bytes
        
        for (let file of input.files) {
            if (file.size > maxSize) {
                const formattedSize = formatFileSize(file.size);
                alert(`File "${file.name}" is too large (${formattedSize}). Maximum size is 10GB.`);
                return;
            }
        }

        // Show upload progress with file details
        const fileNames = Array.from(input.files).map(f => `${f.name} (${formatFileSize(f.size)})`).join('\n');
        const totalSize = Array.from(input.files).reduce((sum, f) => sum + f.size, 0);
        const totalSizeFormatted = formatFileSize(totalSize);
        
        if (!confirm(`Upload ${input.files.length} file(s)?\n\nTotal Size: ${totalSizeFormatted}\n\nFiles:\n${fileNames}\n\nThis may take a very long time for large files (>1GB)...`)) {
            return;
        }

        const formData = new FormData();
        
        // Add all files to FormData
        for (let i = 0; i < input.files.length; i++) {
            formData.append('files', input.files[i]);
        }
        
        try {
            // Show loading message for very large files
            const hasVeryLargeFile = Array.from(input.files).some(f => f.size > 1 * 1024 * 1024 * 1024); // >1GB
            if (hasVeryLargeFile) {
                alert('Very large file upload started. This may take 10+ minutes. Please do not close the browser...');
                
                // Show progress indicator
                showUploadProgress();
            }
            
            const response = await fetch(`/api/upload/multiple/${taskId}`, {
                method: 'POST',
                body: formData
            });
            
            // Hide progress indicator
            hideUploadProgress();
            
            if (response.ok) {
                const result = await response.json();
                if (result.success) {
                    const uploadedFiles = result.files.map(f => `${f.originalName} (${formatFileSize(f.size)})`).join(', ');
                    alert(`Successfully uploaded ${result.message}!\n\nFiles: ${uploadedFiles}`);
                    location.reload();
                } else {
                    alert('Upload failed: ' + (result.message || 'Unknown error'));
                }
            } else {
                const error = await response.json();
                alert('Error uploading files: ' + (error.message || error.error || 'Unknown error'));
            }
        } catch (error) {
            hideUploadProgress();
            console.error('Error:', error);
            alert('Error uploading files: ' + error.message);
        }
    }
}

// Show upload progress for very large files
function showUploadProgress() {
    const progressHtml = `
        <div id="uploadProgress" class="position-fixed top-50 start-50 translate-middle bg-white p-4 shadow-lg rounded" style="z-index: 9999;">
            <div class="text-center">
                <div class="spinner-border text-primary mb-3" role="status">
                    <span class="visually-hidden">Uploading...</span>
                </div>
                <h5>Uploading Large Files...</h5>
                <p class="text-muted">This may take several minutes. Please wait.</p>
                <small class="text-info">Do not close this window</small>
            </div>
        </div>
    `;
    document.body.insertAdjacentHTML('beforeend', progressHtml);
}

// Hide upload progress
function hideUploadProgress() {
    const progressElement = document.getElementById('uploadProgress');
    if (progressElement) {
        progressElement.remove();
    }
}

// View Task Files
async function viewTaskFiles(button) {
    const taskId = button.getAttribute('data-id');
    
    try {
        const response = await fetch(`/api/upload/files/${taskId}`);
        const result = await response.json();
        
        if (result.success) {
            displayFilesInModal(result);
        } else {
            alert('Error loading files');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error loading files');
    }
}

// Display Files in Modal
function displayFilesInModal(fileData) {
    const container = document.getElementById('fileListContainer');
    
    if (fileData.files.length === 0) {
        container.innerHTML = `
            <div class="text-center text-muted">
                <i class="fa-solid fa-folder-open fa-3x mb-3"></i>
                <h5>No files uploaded yet</h5>
                <p>Upload files to see them here</p>
            </div>
        `;
    } else {
        let filesHtml = '<div class="file-list">';
        
        fileData.files.forEach(file => {
            filesHtml += `
                <div class="file-item mb-3 p-3 border rounded">
                    <div class="d-flex justify-content-between align-items-center">
                        <div class="d-flex align-items-center">
                            <i class="${getFileIcon(file.name)} me-3 fa-2x"></i>
                            <div>
                                <h6 class="mb-1">${file.originalName || file.name}</h6>
                                <small class="text-muted">${formatFileSize(file.size || 0)}</small>
                            </div>
                        </div>
                        <a href="/api/upload/download-file/${fileData.taskId}/${file.originalName || file.name}" class="btn btn-sm btn-primary">
                            <i class="fa-solid fa-download"></i> Download
                        </a>
                    </div>
                </div>
            `;
        });
        
        filesHtml += '</div>';
        container.innerHTML = filesHtml;
    }
    
    // Show modal
    new bootstrap.Modal(document.getElementById('fileViewerModal')).show();
}

// Get File Icon based on type
function getFileIcon(fileType) {
    switch (fileType) {
        case 'PDF': return 'fa-solid fa-file-pdf text-danger';
        case 'Word Document': return 'fa-solid fa-file-word text-primary';
        case 'Excel': return 'fa-solid fa-file-excel text-success';
        case 'PowerPoint': return 'fa-solid fa-file-powerpoint text-warning';
        case 'Image': return 'fa-solid fa-file-image text-info';
        case 'Video': return 'fa-solid fa-file-video text-danger';
        case 'Audio': return 'fa-solid fa-file-audio text-secondary';
        case 'Archive': return 'fa-solid fa-file-zipper text-warning';
        case 'Text': return 'fa-solid fa-file-lines text-muted';
        default: return 'fa-solid fa-file text-secondary';
    }
}

// Upload Single File (fallback)
async function uploadFile(input, taskId) {
    if (input.files && input.files[0]) {
        const file = input.files[0];
        const formData = new FormData();
        formData.append('file', file);
        
        try {
            const response = await fetch(`/api/upload/${taskId}`, {
                method: 'POST',
                body: formData
            });
            
            if (response.ok) {
                const result = await response.text();
                alert(result);
                location.reload();
            } else {
                alert('Error uploading file');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Error uploading file');
        }
    }
}

// Download File
async function downloadFile(button) {
    const id = button.getAttribute('data-id');
    
    try {
        const response = await fetch(`/api/upload/download/${id}`);
        
        if (response.ok) {
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = response.headers.get('Content-Disposition')?.split('filename=')[1]?.replace(/"/g, '') || 'download';
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        } else {
            alert('File not found');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error downloading file');
    }
}

// Delete Task
async function deleteTask(button) {
    const id = button.getAttribute('data-id');
    
    if (confirm('Are you sure you want to delete this task?')) {
        try {
            const response = await fetch(`/api/tasks/delete/${id}`, {
                method: 'DELETE'
            });
            
            if (response.ok) {
                location.reload();
            } else {
                alert('Error deleting task');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Error deleting task');
        }
    }
}

// View Task History
async function viewHistory(taskId) {
    try {
        const response = await fetch(`/api/tasks/${taskId}/history`);
        const history = await response.json();
        
        let historyText = 'Task History:\n\n';
        history.forEach(entry => {
            historyText += `${entry.changedDate} - ${entry.action}`;
            if (entry.oldValue && entry.newValue) {
                historyText += ` (${entry.oldValue} → ${entry.newValue})`;
            }
            historyText += ` by ${entry.changedBy}\n`;
        });
        
        alert(historyText);
    } catch (error) {
        console.error('Error:', error);
        alert('Error loading task history');
    }
}

// View Task Comments
async function viewComments(taskId) {
    try {
        const response = await fetch(`/api/tasks/${taskId}/comments`);
        const comments = await response.json();
        
        let commentsText = 'Task Comments:\n\n';
        comments.forEach(comment => {
            commentsText += `${comment.createdDate} - ${comment.author}:\n${comment.comment}\n\n`;
        });
        
        alert(commentsText || 'No comments yet.');
    } catch (error) {
        console.error('Error:', error);
        alert('Error loading comments');
    }
}
