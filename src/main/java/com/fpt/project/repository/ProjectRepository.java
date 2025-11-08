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
        AND pm.status = 'ACTIVE'
    """)
    List<Project> findAllByUserEmail(@Param("email") String email);



    @Query("""
        select new com.fpt.project.dto.response.UserResponse(
            u.id, u.displayName, u.email, u.avatar, pm.role
        )
        from ProjectMember pm
        join pm.user u
        where pm.project.id = :projectId
        AND pm.status = 'ACTIVE'
        order by u.displayName asc
    """)
    List<UserResponse> findUsersByIdProject(Integer projectId);

    @Query("""
    select p
    from Project p
    where p.isPublic = 1
      and not exists (
            select 1
            from ProjectMember pm
            where pm.project = p
              and pm.user.id = :userId
              and pm.status = 'ACTIVE'
        )
""")
    List<Project> findAllPublicProjectsNotJoinedByUser(@Param("userId") Integer userId);

    @Query("""
    select distinct p
    from Project p
    left join p.projectMembers pm
    where (p.isPublic = 1 or pm.user.id = :userId)
      and lower(p.name) like concat('%', lower(:keyword), '%')
""")
    List<Project> findAllPublicOrJoinedProjects(
            @Param("userId") Integer userId,
            @Param("keyword") String keyword
    );



}
