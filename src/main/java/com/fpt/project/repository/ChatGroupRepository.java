package com.fpt.project.repository;

import com.fpt.project.dto.response.ChatGroupResponse;
import com.fpt.project.entity.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, Integer> {

    @Query(value = """
        SELECT g.*
        FROM chat_groups g
        JOIN project_member pm
             ON pm.project_id = g.project_id
            AND pm.user_id    = :userId
            AND pm.status     = 'ACTIVE'
        LEFT JOIN (
            SELECT m.group_id, MAX(m.created_at) AS last_time
            FROM message m
            GROUP BY m.group_id
        ) lm ON lm.group_id = g.id
        ORDER BY (lm.last_time IS NULL), lm.last_time DESC
        """, nativeQuery = true)
    List<ChatGroup> findAllByUserOrderByLastMsg(@Param("userId") Integer userId);


    @Query("SELECT c.project.id FROM ChatGroup c WHERE c.id = ?1")
    Integer findProjectIdById(Integer groupId);

}
