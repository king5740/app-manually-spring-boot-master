package org.example.condigbat.entity;

import org.example.condigbat.entity.template.AbsTitleIntegerEntity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"title", "language_id"}),
        @UniqueConstraint(columnNames = {"url", "language_id"}),
}
)
public class Section extends AbsTitleIntegerEntity {

    @Column(nullable = false)
    private String url;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private Byte maxRate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Language language;

    @OneToMany(mappedBy = "section")
    private List<Problem> problems;
}
