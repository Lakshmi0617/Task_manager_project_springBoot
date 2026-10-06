package com.tasktracker;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Task {

    // Status constants
    public static final String STATUS_TO_DO = "TO_DO";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_IN_REVIEW = "IN_REVIEW";
    public static final String STATUS_COMPLETED = "COMPLETED";

    // Priority constants
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_MEDIUM = "MEDIUM";
    public static final String PRIORITY_LOW = "LOW";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String developer;
    private String teamLead;
    private String manager;
    private String status;
    private String priority;
    private LocalDate dueDate;
    private String uploadedFile;
    private String completionDescription;
    private String reviewComments;
    private Boolean teamLeadApproved;
    private Boolean managerApproved;
    private String sprintName;
    private LocalDateTime createdDate;
    private LocalDateTime completedDate;
    private String createdBy;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TaskHistory> history;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TaskComment> comments;

    public Task() {
        this.status = STATUS_TO_DO;
        this.priority = PRIORITY_MEDIUM;
        this.teamLeadApproved = false;
        this.managerApproved = false;
        this.createdDate = LocalDateTime.now();
    }

    public Task(String title) {
        this();
        this.title = title;
    }

    public Task(String title, String developer) {
        this(title);
        this.developer = developer;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getTeamLead() {
        return teamLead;
    }

    public void setTeamLead(String teamLead) {
        this.teamLead = teamLead;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        if (STATUS_COMPLETED.equals(status) && completedDate == null) {
            this.completedDate = LocalDateTime.now();
        }
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(String uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String getCompletionDescription() {
        return completionDescription;
    }

    public void setCompletionDescription(String completionDescription) {
        this.completionDescription = completionDescription;
    }

    public String getReviewComments() {
        return reviewComments;
    }

    public void setReviewComments(String reviewComments) {
        this.reviewComments = reviewComments;
    }

    public Boolean getTeamLeadApproved() {
        return teamLeadApproved;
    }

    public void setTeamLeadApproved(Boolean teamLeadApproved) {
        this.teamLeadApproved = teamLeadApproved;
    }

    public Boolean getManagerApproved() {
        return managerApproved;
    }

    public void setManagerApproved(Boolean managerApproved) {
        this.managerApproved = managerApproved;
    }

    public String getSprintName() {
        return sprintName;
    }

    public void setSprintName(String sprintName) {
        this.sprintName = sprintName;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDateTime completedDate) {
        this.completedDate = completedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<TaskHistory> getHistory() {
        return history;
    }

    public void setHistory(List<TaskHistory> history) {
        this.history = history;
    }

    public List<TaskComment> getComments() {
        return comments;
    }

    public void setComments(List<TaskComment> comments) {
        this.comments = comments;
    }

    // Helper methods
    public boolean isStatus(String status) {
        return this.status != null && this.status.equals(status);
    }

    public String getDisplayStatus() {
        if (status == null) return STATUS_TO_DO;
        return status.replace("_", " ");
    }

    public boolean isOverdue() {
        return dueDate != null && LocalDate.now().isAfter(dueDate) && !STATUS_COMPLETED.equals(status);
    }

    public long getDaysOverdue() {
        if (!isOverdue()) return 0;
        return LocalDate.now().toEpochDay() - dueDate.toEpochDay();
    }

    public long daysOverdue() {
        return getDaysOverdue();
    }
}
