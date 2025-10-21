package com.fpt.project.repository;


import com.fpt.project.dto.PageResponse;
import com.fpt.project.dto.response.ProjectResponse;
import com.fpt.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);

}
