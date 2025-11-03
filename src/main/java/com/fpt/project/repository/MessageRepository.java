package com.fpt.project.repository;

import com.fpt.project.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Integer > {
}
