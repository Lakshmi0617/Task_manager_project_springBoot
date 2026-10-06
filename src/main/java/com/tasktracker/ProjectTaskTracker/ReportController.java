package com.tasktracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private TaskRepository repo;

    // Get task statistics
    @GetMapping("/statistics")
    public Map<String, Object> getTaskStatistics() {
        List<Task> allTasks = repo.findAll();
        
        long totalTasks = allTasks.size();
        long completedTasks = allTasks.stream().filter(t -> Task.STATUS_COMPLETED.equals(t.getStatus())).count();
        long inProgressTasks = allTasks.stream().filter(t -> Task.STATUS_IN_PROGRESS.equals(t.getStatus())).count();
        long inReviewTasks = allTasks.stream().filter(t -> Task.STATUS_IN_REVIEW.equals(t.getStatus())).count();
        long todoTasks = allTasks.stream().filter(t -> Task.STATUS_TO_DO.equals(t.getStatus())).count();
        long overdueTasks = allTasks.stream().filter(Task::isOverdue).count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", totalTasks);
        stats.put("completedTasks", completedTasks);
        stats.put("inProgressTasks", inProgressTasks);
        stats.put("inReviewTasks", inReviewTasks);
        stats.put("todoTasks", todoTasks);
        stats.put("overdueTasks", overdueTasks);
        stats.put("completionRate", totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0);
        
        return stats;
    }

    // Get developer-wise task status
    @GetMapping("/developer-status")
    public Map<String, Map<String, Long>> getDeveloperWiseTaskStatus() {
        List<Task> allTasks = repo.findAll();
        
        Map<String, Map<String, Long>> developerStats = new HashMap<>();
        
        // Group tasks by developer
        Map<String, List<Task>> tasksByDeveloper = allTasks.stream()
                .filter(task -> task.getDeveloper() != null)
                .collect(Collectors.groupingBy(Task::getDeveloper));
        
        for (Map.Entry<String, List<Task>> entry : tasksByDeveloper.entrySet()) {
            String developer = entry.getKey();
            List<Task> tasks = entry.getValue();
            
            Map<String, Long> statusCount = new HashMap<>();
            statusCount.put("completed", tasks.stream().filter(t -> Task.STATUS_COMPLETED.equals(t.getStatus())).count());
            statusCount.put("inProgress", tasks.stream().filter(t -> Task.STATUS_IN_PROGRESS.equals(t.getStatus())).count());
            statusCount.put("inReview", tasks.stream().filter(t -> Task.STATUS_IN_REVIEW.equals(t.getStatus())).count());
            statusCount.put("todo", tasks.stream().filter(t -> Task.STATUS_TO_DO.equals(t.getStatus())).count());
            statusCount.put("total", (long) tasks.size());
            
            developerStats.put(developer, statusCount);
        }
        
        return developerStats;
    }

    // Get priority-wise task distribution
    @GetMapping("/priority-distribution")
    public Map<String, Long> getPriorityDistribution() {
        List<Task> allTasks = repo.findAll();
        
        Map<String, Long> priorityCount = new HashMap<>();
        priorityCount.put("high", allTasks.stream().filter(t -> Task.PRIORITY_HIGH.equals(t.getPriority())).count());
        priorityCount.put("medium", allTasks.stream().filter(t -> Task.PRIORITY_MEDIUM.equals(t.getPriority())).count());
        priorityCount.put("low", allTasks.stream().filter(t -> Task.PRIORITY_LOW.equals(t.getPriority())).count());
        
        return priorityCount;
    }

    // Get sprint-wise task distribution
    @GetMapping("/sprint-distribution")
    public Map<String, Long> getSprintDistribution() {
        List<Task> allTasks = repo.findAll();
        
        return allTasks.stream()
                .filter(task -> task.getSprintName() != null)
                .collect(Collectors.groupingBy(Task::getSprintName, Collectors.counting()));
    }

    // Get overdue tasks with days overdue
    @GetMapping("/overdue-details")
    public List<Map<String, Object>> getOverdueTaskDetails() {
        return repo.findAll().stream()
                .filter(Task::isOverdue)
                .map(task -> {
                    Map<String, Object> details = new HashMap<>();
                    details.put("id", task.getId());
                    details.put("title", task.getTitle());
                    details.put("developer", task.getDeveloper());
                    details.put("dueDate", task.getDueDate());
                    details.put("daysOverdue", task.getDaysOverdue());
                    details.put("priority", task.getPriority());
                    return details;
                })
                .collect(Collectors.toList());
    }

    // Get tasks due this week
    @GetMapping("/due-this-week")
    public List<Map<String, Object>> getTasksDueThisWeek() {
        return repo.findAll().stream()
                .filter(task -> task.getDueDate() != null)
                .filter(task -> {
                    java.time.LocalDate today = java.time.LocalDate.now();
                    java.time.LocalDate weekEnd = today.plusDays(7);
                    return !task.getDueDate().isBefore(today) && !task.getDueDate().isAfter(weekEnd);
                })
                .map(task -> {
                    Map<String, Object> details = new HashMap<>();
                    details.put("id", task.getId());
                    details.put("title", task.getTitle());
                    details.put("developer", task.getDeveloper());
                    details.put("dueDate", task.getDueDate());
                    details.put("priority", task.getPriority());
                    details.put("status", task.getStatus());
                    return details;
                })
                .collect(Collectors.toList());
    }

    // Get developer performance metrics
    @GetMapping("/developer-performance/{developer}")
    public Map<String, Object> getDeveloperPerformance(@PathVariable String developer) {
        List<Task> developerTasks = repo.findAll().stream()
                .filter(task -> developer.equals(task.getDeveloper()))
                .collect(Collectors.toList());
        
        long totalTasks = developerTasks.size();
        long completedTasks = developerTasks.stream().filter(t -> Task.STATUS_COMPLETED.equals(t.getStatus())).count();
        long inProgressTasks = developerTasks.stream().filter(t -> Task.STATUS_IN_PROGRESS.equals(t.getStatus())).count();
        long inReviewTasks = developerTasks.stream().filter(t -> Task.STATUS_IN_REVIEW.equals(t.getStatus())).count();
        long todoTasks = developerTasks.stream().filter(t -> Task.STATUS_TO_DO.equals(t.getStatus())).count();
        long overdueTasks = developerTasks.stream().filter(Task::isOverdue).count();
        
        Map<String, Object> performance = new HashMap<>();
        performance.put("developer", developer);
        performance.put("totalTasks", totalTasks);
        performance.put("completedTasks", completedTasks);
        performance.put("inProgressTasks", inProgressTasks);
        performance.put("inReviewTasks", inReviewTasks);
        performance.put("todoTasks", todoTasks);
        performance.put("completionRate", totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0);
        performance.put("overdueTasks", overdueTasks);
        performance.put("highPriorityTasks", developerTasks.stream().filter(t -> Task.PRIORITY_HIGH.equals(t.getPriority())).count());
        
        return performance;
    }

    // Get all developers list
    @GetMapping("/developers")
    public List<String> getAllDevelopers() {
        return repo.findAll().stream()
                .filter(task -> task.getDeveloper() != null)
                .map(Task::getDeveloper)
                .distinct()
                .collect(Collectors.toList());
    }
}
