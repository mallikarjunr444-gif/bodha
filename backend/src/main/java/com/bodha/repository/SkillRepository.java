package com.bodha.repository;

import com.bodha.model.Skill;
import com.bodha.model.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    List<Skill> findBySubjectId(String subjectId);

    List<Skill> findBySubjectIdAndCategory(String subjectId, SkillCategory category);
}
