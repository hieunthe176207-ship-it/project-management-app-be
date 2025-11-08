package com.fpt.project.repository;

import com.fpt.project.entity.ProjectMember;
import com.fpt.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer> {
    boolean existsByProjectIdAndUserId(Integer projectId, Integer userId);

    @Query("SELECT u FROM User u WHERE u.id NOT IN (SELECT pm.user.id FROM ProjectMember pm WHERE pm.project.id = ?1 AND pm.status = 'ACTIVE')")
    List<User> findUsersWithoutMember(Integer projectId);

    @Query("""
        SELECT COALESCE(pm.lastReadMessageId, 0)
        FROM ProjectMember pm
        WHERE pm.project.id = :projectId AND pm.user.id = :userId
    """)
    Integer findLastReadMessageId(@Param("projectId") Integer projectId,
                                  @Param("userId") Integer userId);


    @Query("""
        select pm.user
        from ProjectMember pm
        where pm.project.id = :projectId
          and pm.status = 'ACTIVE'
    """)
    List<User> findUsersByProjectId(Integer projectId);

    @Modifying
    @Query("""
        UPDATE ProjectMember pm
           SET pm.lastReadMessageId = :maxId
         WHERE pm.project.id = :projectId
           AND pm.user.id     = :userId
    """)
    void markProjectAsRead(@Param("projectId") Integer projectId,
                          @Param("userId") Integer userId,
                          @Param("maxId") Integer maxId);

    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = ?1 AND pm.user.id = ?2 AND pm.status = 'ACTIVE'")
    ProjectMember findUserByProjectIdAndUserId(Integer projectId, Integer userId);


    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = ?1 AND pm.user.id = ?2")
    ProjectMember findMemberByProjectId(Integer projectId, Integer userId);
}
