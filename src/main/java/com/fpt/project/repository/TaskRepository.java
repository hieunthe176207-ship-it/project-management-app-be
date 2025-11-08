package com.fpt.project.repository;

import com.fpt.project.constant.TaskStatus;
import com.fpt.project.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByAssignees_Id(Integer userId);

    @Query("""
        select distinct t
        from Task t
        join t.project p
        join p.projectMembers pm
        join t.assignees a
        where pm.user.id = :userId
          and pm.status = 'ACTIVE'
          and a.id = :userId
        """)
    List<Task> findAllAssignedToUser(@Param("userId") Integer userId);

    @Query("SELECT t fROM Task t  WHERE t.project.id = :projectId")
    List<Task> findByProjectIdWithAssignees(@Param("projectId") Integer projectId);

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


    @Query("SELECT t FROM Task t JOIN FETCH t.assignees WHERE t.project.id = :projectId AND t.status = :status")
    List<Task> findByProjectIdAndStatusWithAssignees(@Param("projectId") Integer projectId, @Param("status") TaskStatus status);


    @Query("""
    SELECT t FROM Task t
    JOIN t.assignees a
    WHERE a.id = :userId 
    AND t.project.id = :projectId
    """)
    List<Task> findTasksAssignedToUserInProject(@Param("userId") Integer userId, @Param("projectId") Integer projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    int countAllTaskFromProject(int projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status <> 'DONE'")
    int countTaskNotDoneFormProject(int projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status = 'DONE'")
    int countCompletedTaskFromProject(int projectId);
}