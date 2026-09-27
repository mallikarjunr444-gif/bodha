package com.bodha.repository;

import com.bodha.model.AssessmentResponse;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentResponseRepository extends JpaRepository<AssessmentResponse, Long> {

    @EntityGraph(attributePaths = {"question", "question.skill", "selectedOption"})
    List<AssessmentResponse> findByAttemptId(Long attemptId);

    @EntityGraph(attributePaths = {"question", "question.skill", "selectedOption"})
    Optional<AssessmentResponse> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);

    long countByAttemptId(Long attemptId);
}
