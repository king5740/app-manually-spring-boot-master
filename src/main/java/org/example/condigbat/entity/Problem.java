package org.example.condigbat.entity;

import org.example.condigbat.entity.template.AbsIntegerEntity;
import org.example.condigbat.entity.template.AbsTitleIntegerEntity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(uniqueConstraints =
@UniqueConstraint(
        columnNames = {"title", "section_id"}
))
public class Problem extends AbsTitleIntegerEntity {

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private String methodSignature;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Section section;

    @OneToMany(mappedBy = "problem")
    private List<Case> cases;
}
