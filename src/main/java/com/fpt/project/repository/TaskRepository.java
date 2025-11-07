package com.fpt.project.repository;
import com.fpt.project.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignees_Id(Integer userId);

    @Query("""
    select t
    from Task t
    join t.project p
    join p.projectMembers pm
    where pm.user.id = :userId
    and (t.title like %:keyword%)
""")
    List<Task> searchAllTasksInProjectsUserJoined(@Param("userId") Integer userId, @Param("keyword") String keyword);



    @Query("""
    select t
    from Task t
    where t.project.id = :projectId
    """)
    List<Task> findByProject_Id(Integer projectId);
}
