package org.example.condigbat.entity;

import org.example.condigbat.entity.template.AbsLongEntity;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        columnNames = {"user_id", "problem_id"}))
public class UserProblem extends AbsLongEntity {

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Problem problem;

    @Column(columnDefinition = "text")
    private String solution;

    @Column(nullable = false)
    private Boolean solved;

}