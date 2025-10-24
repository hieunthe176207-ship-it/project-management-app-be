package com.fpt.project.repository;

import com.fpt.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
    @Query("""
        select distinct p
        from Project p
        left join p.members m
        where m.email = :email
    """)
    List<Project> findAllByUserEmail(@Param("email") String email);
}
