package com.fpt.project.repository;

import com.fpt.project.dto.response.UserResponse;
import com.fpt.project.entity.Project;
import com.fpt.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
    @Query("""
        select distinct p
        from Project p
        left join p.projectMembers pm
        where pm.user.email = :email
    """)
    List<Project> findAllByUserEmail(@Param("email") String email);



    @Query("""
        select new com.fpt.project.dto.response.UserResponse(
            u.id, u.displayName, u.email, u.avatar, pm.role
        )
        from ProjectMember pm
        join pm.user u
        where pm.project.id = :projectId
        order by u.displayName asc
    """)
    List<UserResponse> findUsersByIdProject(Integer projectId);

    @Query("""
    select p
    from Project p
    where p.isPublic = 1
    and p.id not in (
        select pm.project.id
        from ProjectMember pm
        where pm.user.id = :userId
    )
""")
    List<Project> findAllPublicProjectsNotJoinedByUser(@Param("userId") Integer userId);

}
