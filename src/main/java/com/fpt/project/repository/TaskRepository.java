package com.fpt.project.repository;
import com.fpt.project.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignees_Id(Long userId);
}
