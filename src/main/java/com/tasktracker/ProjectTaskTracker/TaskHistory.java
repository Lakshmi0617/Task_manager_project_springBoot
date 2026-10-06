package com.tasktracker;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class TaskHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    private String action;
    private String oldValue;
    private String newValue;
    private String changedBy;
    private LocalDateTime changedDate;

    public TaskHistory() {
        this.changedDate = LocalDateTime.now();
    }

    public TaskHistory(Task task, String action, String changedBy) {
        this();
        this.task = task;
        this.action = action;
        this.changedBy = changedBy;
    }

    public TaskHistory(Task task, String action, String oldValue, String newValue, String changedBy) {
        this(task, action, changedBy);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangedDate() {
        return changedDate;
    }

    public void setChangedDate(LocalDateTime changedDate) {
        this.changedDate = changedDate;
    }
}
