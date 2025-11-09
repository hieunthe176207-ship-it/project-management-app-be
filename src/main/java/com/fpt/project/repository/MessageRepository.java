package com.fpt.project.repository;

import com.fpt.project.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer > {

    @Query("SELECT m FROM Message m WHERE m.group.id = ?1 ORDER BY m.createdAt ASC")
    List<Message> findByGroupId(int chatGroupId);

    Message findTopByGroup_IdOrderByIdDesc(Integer groupId);

    @Query("SELECT COALESCE(MAX(m.id), 0) FROM Message m WHERE m.group.id = :groupId")
    Integer findMaxIdByGroup(@Param("groupId") Integer groupId);

    @Query("""
    select count(m)
    from Message m
    join m.group g
    join g.project p
    join p.projectMembers pm
    where pm.user.id = :userId
      and m.id > coalesce(pm.lastReadMessageId, 0)
""")
    int countUnreadMessages(@Param("userId") Integer userId);


    @Query("SELECT m FROM Message m WHERE m.group.id = :groupId ORDER BY m.createdAt DESC")
    List<Message> findLastMessage(@Param("groupId") Integer groupId, Pageable pageable);
}
