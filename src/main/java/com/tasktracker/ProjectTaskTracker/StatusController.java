package com.tasktracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    @Autowired
    private TaskRepository repo;

    // Get All Available Statuses
    @GetMapping("/all")
    public List<String> getAllStatuses() {
        return Arrays.asList(
            Task.STATUS_TO_DO, 
            Task.STATUS_IN_PROGRESS, 
            Task.STATUS_IN_REVIEW, 
            Task.STATUS_COMPLETED
        );
    }

    // Update Task Status
    @PutMapping("/update/{id}")
    public Task updateStatus(@PathVariable Long id,
                             @RequestParam String status) {
        Task task = repo.findById(id).orElseThrow();
        task.setStatus(status);
        return repo.save(task);
    }

    // Get Tasks by Status
    @GetMapping("/tasks/{status}")
    public List<Task> getTasksByStatus(@PathVariable String status) {
        return repo.findAll().stream()
                .filter(task -> status.equals(task.getStatus()))
                .toList();
    }

    // Move Task to Next Status
    @PutMapping("/next/{id}")
    public Task moveToNextStatus(@PathVariable Long id) {
        Task task = repo.findById(id).orElseThrow();
        String currentStatus = task.getStatus();
        
        switch (currentStatus) {
            case Task.STATUS_TO_DO -> task.setStatus(Task.STATUS_IN_PROGRESS);
            case Task.STATUS_IN_PROGRESS -> task.setStatus(Task.STATUS_IN_REVIEW);
            case Task.STATUS_IN_REVIEW -> task.setStatus(Task.STATUS_COMPLETED);
            case Task.STATUS_COMPLETED -> task.setStatus(Task.STATUS_COMPLETED); // Stay in completed
            default -> task.setStatus(Task.STATUS_TO_DO); // Default to TO_DO if invalid status
        }
        
        return repo.save(task);
    }

    // Move Task to Previous Status
    @PutMapping("/previous/{id}")
    public Task moveToPreviousStatus(@PathVariable Long id) {
        Task task = repo.findById(id).orElseThrow();
        String currentStatus = task.getStatus();
        
        switch (currentStatus) {
            case Task.STATUS_COMPLETED -> task.setStatus(Task.STATUS_IN_REVIEW);
            case Task.STATUS_IN_REVIEW -> task.setStatus(Task.STATUS_IN_PROGRESS);
            case Task.STATUS_IN_PROGRESS -> task.setStatus(Task.STATUS_TO_DO);
            case Task.STATUS_TO_DO -> task.setStatus(Task.STATUS_TO_DO); // Stay in TO_DO
            default -> task.setStatus(Task.STATUS_TO_DO); // Default to TO_DO if invalid status
        }
        
        return repo.save(task);
    }
}
