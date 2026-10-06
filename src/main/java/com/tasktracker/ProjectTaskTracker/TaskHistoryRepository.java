package com.tasktracker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
    List<TaskHistory> findByTaskIdOrderByChangedDateDesc(Long taskId);
    List<TaskHistory> findByChangedByOrderByChangedDateDesc(String changedBy);
}
