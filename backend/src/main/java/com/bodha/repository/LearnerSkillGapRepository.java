package com.bodha.repository;

import com.bodha.model.LearnerSkillGap;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearnerSkillGapRepository extends JpaRepository<LearnerSkillGap, Long> {

    @EntityGraph(attributePaths = {"skill", "learnerGoal", "learnerGoal.subject", "evaluatedFromAttempt"})
    List<LearnerSkillGap> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"skill", "learnerGoal", "learnerGoal.subject", "evaluatedFromAttempt"})
    List<LearnerSkillGap> findByUserIdAndLearnerGoalId(Long userId, Long learnerGoalId);

    @EntityGraph(attributePaths = {"skill", "learnerGoal", "learnerGoal.subject", "evaluatedFromAttempt"})
    List<LearnerSkillGap> findByUserIdAndLearnerGoalSubjectId(Long userId, String subjectId);

    @EntityGraph(attributePaths = {"skill", "learnerGoal", "learnerGoal.subject", "evaluatedFromAttempt"})
    Optional<LearnerSkillGap> findByUserIdAndLearnerGoalIdAndSkillId(Long userId, Long learnerGoalId, Long skillId);

    @EntityGraph(attributePaths = {"skill", "learnerGoal", "learnerGoal.subject", "evaluatedFromAttempt"})
    List<LearnerSkillGap> findByUserIdAndLearnerGoalIdAndStatus(Long userId, Long learnerGoalId, LearnerSkillGap.Status status);
}
