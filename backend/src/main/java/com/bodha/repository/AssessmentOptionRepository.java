package com.bodha.repository;

import com.bodha.model.AssessmentOption;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentOptionRepository extends JpaRepository<AssessmentOption, Long> {

    @Override
    @EntityGraph(attributePaths = {"question"})
    Optional<AssessmentOption> findById(Long id);

    @EntityGraph(attributePaths = {"question"})
    List<AssessmentOption> findByQuestionIdOrderByOrderIndexAsc(Long questionId);

    @EntityGraph(attributePaths = {"question"})
    List<AssessmentOption> findByQuestionAssessmentIdOrderByQuestionOrderIndexAscOrderIndexAsc(Long assessmentId);
}
