package com.fpt.project.repository;

import com.fpt.project.constant.JoinStatus;
import com.fpt.project.entity.JoinRequest;
import com.fpt.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Integer> {

    @Query("""
        select jr
        from JoinRequest jr
        where jr.user.id = :userId
        and jr.project.id = :projectId
        and jr.status = 'PENDING'
    """)
    JoinRequest   findByUserIdAndProjectId(Integer userId, Integer projectId);

    @Query("""
        select count(jr)
        from JoinRequest jr
        where jr.project.id = :projectId
        and jr.status = 'PENDING'
    """)
    int countRecordsPendingByProjectId(Integer projectId);

    @Query("""
        select jr.user
        from JoinRequest jr
        where jr.project.id = :projectId
        and jr.status = :status
    """)
    List<User> findUsersByProjectIdAndStatus(Integer projectId, JoinStatus status);
}
