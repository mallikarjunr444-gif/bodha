package com.bodha.repository;

import com.bodha.model.Assessment;
import com.bodha.model.AssessmentType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    @Override
    @EntityGraph(attributePaths = {"subject"})
    Optional<Assessment> findById(Long id);

    @EntityGraph(attributePaths = {"subject"})
    List<Assessment> findBySubjectId(String subjectId);

    @EntityGraph(attributePaths = {"subject"})
    Optional<Assessment> findBySubjectIdAndAssessmentType(String subjectId, AssessmentType assessmentType);
}
