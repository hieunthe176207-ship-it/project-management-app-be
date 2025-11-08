package com.fpt.project.repository;

import com.fpt.project.entity.SubTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubTaskRepositry extends JpaRepository<SubTask, Integer> {
    @Query("""
    select st
    from SubTask st
    where st.task.id = :taskId
    """)
    List<SubTask> findByTask_Id(Integer taskId);


}
