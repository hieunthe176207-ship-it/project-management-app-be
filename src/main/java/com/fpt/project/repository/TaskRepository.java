package com.fpt.project.repository;

import com.fpt.project.constant.TaskStatus;
import com.fpt.project.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    List<Task> findByProjectIdAndStatus(Integer projectId, TaskStatus status);

    List<Task> findByProjectIdOrderByStatusAsc(Integer projectId);

    @Query("SELECT t FROM Task t JOIN FETCH t.assignees WHERE t.project.id = :projectId")
    List<Task> findByProjectIdWithAssignees(@Param("projectId") Integer projectId);

    @Query("SELECT t FROM Task t JOIN FETCH t.assignees WHERE t.project.id = :projectId AND t.status = :status")
    List<Task> findByProjectIdAndStatusWithAssignees(@Param("projectId") Integer projectId, @Param("status") TaskStatus status);
}