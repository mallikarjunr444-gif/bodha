package com.bodha.repository;

import com.bodha.model.AssessmentAttempt;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "assessment", "assessment.subject", "learnerGoal"})
    Optional<AssessmentAttempt> findById(Long id);

    @EntityGraph(attributePaths = {"user", "assessment", "assessment.subject", "learnerGoal"})
    List<AssessmentAttempt> findByUserIdAndLearnerGoalIdOrderByCompletedAtDesc(Long userId, Long learnerGoalId);

    @EntityGraph(attributePaths = {"user", "assessment", "assessment.subject", "learnerGoal"})
    List<AssessmentAttempt> findByUserIdOrderByCompletedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "assessment", "assessment.subject", "learnerGoal"})
    List<AssessmentAttempt> findByAssessmentId(Long assessmentId);
}
