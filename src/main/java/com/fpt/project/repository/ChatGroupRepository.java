package com.fpt.project.repository;

import com.fpt.project.dto.response.ChatGroupResponse;
import com.fpt.project.entity.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, Integer> {

    @Query("""
    SELECT new com.fpt.project.dto.response.ChatGroupResponse(
        c.id,
        c.name,
        c.avatar
    )
    FROM ChatGroup c
    JOIN c.project p
    JOIN p.projectMembers pm
    WHERE pm.user.id = :userId
    AND pm.status = 'ACTIVE'
""")
    List<ChatGroupResponse> findByUser(@Param("userId") Integer userId);


    @Query("SELECT c.project.id FROM ChatGroup c WHERE c.id = ?1")
    Integer findProjectIdById(Integer groupId);

}
