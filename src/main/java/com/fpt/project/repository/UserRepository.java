package com.fpt.project.repository;

import com.fpt.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);

    // 2. Phương thức mới, đổi tên để tránh xung đột
    // Phương thức này vẫn tìm theo trường 'email', nhưng tên phương thức là 'findByEmailSecure'
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailOptional(String email);
}