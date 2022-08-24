package org.example.condigbat.repository;

import org.example.condigbat.entity.Language;
import org.example.condigbat.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface SectionRepository extends JpaRepository<Section, Integer> {

    long countAllByLanguageId(Integer languageId);
}
