package org.example.condigbat.entity;

import org.example.condigbat.entity.enums.RoleEnum;
import org.example.condigbat.entity.template.AbsIntegerEntity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends AbsIntegerEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private RoleEnum role;

    @OneToMany(mappedBy = "user")
    private List<UserProblem> userProblems;
}
