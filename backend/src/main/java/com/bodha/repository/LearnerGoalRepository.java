package com.bodha.repository;

import com.bodha.model.GoalStatus;
import com.bodha.model.LearnerGoal;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearnerGoalRepository extends JpaRepository<LearnerGoal, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "subject"})
    Optional<LearnerGoal> findById(Long id);

    @EntityGraph(attributePaths = {"user", "subject"})
    Optional<LearnerGoal> findByUserIdAndSubjectId(Long userId, String subjectId);

    @EntityGraph(attributePaths = {"user", "subject"})
    List<LearnerGoal> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "subject"})
    List<LearnerGoal> findByUserIdAndStatus(Long userId, GoalStatus status);

    @EntityGraph(attributePaths = {"user", "subject"})
    List<LearnerGoal> findByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, GoalStatus status);

    boolean existsByUserIdAndSubjectId(Long userId, String subjectId);
}
