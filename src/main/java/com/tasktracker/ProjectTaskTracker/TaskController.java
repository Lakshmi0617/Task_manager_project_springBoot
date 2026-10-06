package com.tasktracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskRepository repo;
    
    @Autowired
    private TaskHistoryRepository historyRepo;
    
    @Autowired
    private TaskCommentRepository commentRepo;

    // Get All Tasks
    @GetMapping("/all")
    public List<Task> getAllTasks() {
        return repo.findAll();
    }

    // Get Task by ID
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow();
    }

    // Create New Task with all fields
    @PostMapping("/create")
    public Task createTask(@RequestParam String title,
                        @RequestParam(required = false) String description,
                        @RequestParam(required = false) String developer,
                        @RequestParam(required = false) String teamLead,
                        @RequestParam(required = false) String manager,
                        @RequestParam(defaultValue = "MEDIUM") String priority,
                        @RequestParam(required = false) String dueDate,
                        @RequestParam(required = false) String sprintName,
                        @RequestParam(required = false) String createdBy) {
        
        Task task = new Task(title);
        task.setDescription(description);
        task.setDeveloper(developer);
        task.setTeamLead(teamLead);
        task.setManager(manager);
        task.setPriority(priority);
        task.setSprintName(sprintName);
        task.setCreatedBy(createdBy != null ? createdBy : "System");
        
        if (dueDate != null && !dueDate.isEmpty()) {
            task.setDueDate(LocalDate.parse(dueDate));
        }
        
        Task savedTask = repo.save(task);
        
        // Create history entry
        TaskHistory history = new TaskHistory(savedTask, "Task created", task.getCreatedBy());
        historyRepo.save(history);
        
        return savedTask;
    }

    // Update Task
    @PutMapping("/update/{id}")
    public Task updateTask(@PathVariable Long id, 
                         @RequestParam String title,
                         @RequestParam(required = false) String description,
                         @RequestParam(required = false) String priority,
                         @RequestParam(required = false) String dueDate,
                         @RequestParam(required = false) String sprintName,
                         @RequestParam(required = false) String changedBy) {
        
        Task task = repo.findById(id).orElseThrow();
        
        // Track changes for history
        String oldTitle = task.getTitle();
        String oldPriority = task.getPriority();
        LocalDate oldDueDate = task.getDueDate();
        
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setSprintName(sprintName);
        
        if (dueDate != null && !dueDate.isEmpty()) {
            task.setDueDate(LocalDate.parse(dueDate));
        }
        
        Task savedTask = repo.save(task);
        
        // Create history entries for changes
        String changer = changedBy != null ? changedBy : "System";
        
        if (!oldTitle.equals(title)) {
            historyRepo.save(new TaskHistory(savedTask, "Title changed", oldTitle, title, changer));
        }
        if (!oldPriority.equals(priority)) {
            historyRepo.save(new TaskHistory(savedTask, "Priority changed", oldPriority, priority, changer));
        }
        if ((oldDueDate == null && task.getDueDate() != null) || 
            (oldDueDate != null && !oldDueDate.equals(task.getDueDate()))) {
            historyRepo.save(new TaskHistory(savedTask, "Due date changed", 
                oldDueDate != null ? oldDueDate.toString() : "None", 
                task.getDueDate() != null ? task.getDueDate().toString() : "None", changer));
        }
        
        return savedTask;
    }

    // Assign Developer to Task
    @PutMapping("/assign/{id}")
    public Task assignDeveloper(@PathVariable Long id, 
                             @RequestParam String developer,
                             @RequestParam(required = false) String changedBy) {
        Task task = repo.findById(id).orElseThrow();
        String oldDeveloper = task.getDeveloper();
        task.setDeveloper(developer);
        
        Task savedTask = repo.save(task);
        
        if (!oldDeveloper.equals(developer)) {
            String changer = changedBy != null ? changedBy : "System";
            TaskHistory history = new TaskHistory(savedTask, "Developer assigned", oldDeveloper, developer, changer);
            historyRepo.save(history);
        }
        
        return savedTask;
    }

    // Add comment to task
    @PostMapping("/{id}/comment")
    public TaskComment addComment(@PathVariable Long id,
                                @RequestParam String comment,
                                @RequestParam String author) {
        Task task = repo.findById(id).orElseThrow();
        TaskComment taskComment = new TaskComment(task, comment, author);
        return commentRepo.save(taskComment);
    }

    // Get task comments
    @GetMapping("/{id}/comments")
    public List<TaskComment> getTaskComments(@PathVariable Long id) {
        return commentRepo.findByTaskIdOrderByCreatedDateDesc(id);
    }

    // Get task history
    @GetMapping("/{id}/history")
    public List<TaskHistory> getTaskHistory(@PathVariable Long id) {
        return historyRepo.findByTaskIdOrderByChangedDateDesc(id);
    }

    // Complete task with description
    @PutMapping("/complete/{id}")
    public Task completeTask(@PathVariable Long id,
                           @RequestParam String completionDescription,
                           @RequestParam(required = false) String completedBy) {
        Task task = repo.findById(id).orElseThrow();
        task.setStatus(Task.STATUS_COMPLETED);
        task.setCompletionDescription(completionDescription);
        
        Task savedTask = repo.save(task);
        
        String completer = completedBy != null ? completedBy : task.getDeveloper();
        TaskHistory history = new TaskHistory(savedTask, "Task completed", null, completionDescription, completer);
        historyRepo.save(history);
        
        return savedTask;
    }

    // Delete Task
    @DeleteMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        repo.deleteById(id);
        return "Task deleted successfully";
    }

    // Get tasks by developer
    @GetMapping("/developer/{developer}")
    public List<Task> getTasksByDeveloper(@PathVariable String developer) {
        return repo.findAll().stream()
                .filter(task -> developer.equals(task.getDeveloper()))
                .toList();
    }

    // Get tasks by priority
    @GetMapping("/priority/{priority}")
    public List<Task> getTasksByPriority(@PathVariable String priority) {
        return repo.findAll().stream()
                .filter(task -> priority.equals(task.getPriority()))
                .toList();
    }

    // Get overdue tasks
    @GetMapping("/overdue")
    public List<Task> getOverdueTasks() {
        return repo.findAll().stream()
                .filter(Task::isOverdue)
                .toList();
    }
}
