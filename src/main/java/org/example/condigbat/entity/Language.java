package org.example.condigbat.entity;

import lombok.Getter;
import lombok.Setter;
import org.example.condigbat.entity.template.AbsTitleIntegerEntity;
import org.example.condigbat.util.CommonUtils;

import javax.persistence.*;
import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

@Entity
@Getter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"title"}))
public class Language extends AbsTitleIntegerEntity {

    @Column(unique = true, nullable = false)
    private String url;

    @OneToMany(mappedBy = "language")
    private List<Section> sections;

    @PrePersist
    @PreUpdate
    private void setUrl() {
        this.url = CommonUtils.makeUrl(this.getTitle());
    }
}
