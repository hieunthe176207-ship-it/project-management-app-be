package com.fpt.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class SubTask extends BaseEntity{
    private String title;
    private boolean completed;

    @ManyToOne
    @JoinColumn(name="task_id", nullable=false)
    Task task;
}
