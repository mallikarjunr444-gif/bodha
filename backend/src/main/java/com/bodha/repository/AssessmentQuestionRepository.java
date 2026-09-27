package com.bodha.repository;

import com.bodha.model.AssessmentQuestion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, Long> {

    @Override
    @EntityGraph(attributePaths = {"skill", "assessment"})
    Optional<AssessmentQuestion> findById(Long id);

    @EntityGraph(attributePaths = {"skill", "assessment"})
    List<AssessmentQuestion> findByAssessmentIdOrderByOrderIndexAsc(Long assessmentId);

    @EntityGraph(attributePaths = {"skill", "assessment"})
    List<AssessmentQuestion> findBySkillId(Long skillId);

    long countByAssessmentId(Long assessmentId);
}
