package com.bodha.dto;

import com.bodha.model.LearnerSkillGap;

import java.time.OffsetDateTime;

/**
 * Data Transfer Object representing an evaluated competency gap or mastery state (Module E).
 */
public record SkillGapResponseDto(
    Long id,
    Long userId,
    Long learnerGoalId,
    String subjectId,
    String subjectTitle,
    Long skillId,
    String skillName,
    String category,
    String status,
    String gapSeverity,
    String reason,
    Long evaluatedFromAttemptId,
    OffsetDateTime updatedAt
) {
    public static SkillGapResponseDto fromEntity(LearnerSkillGap gap) {
        if (gap == null) return null;

        Long userId = gap.getUser() != null ? gap.getUser().getId() : null;
        Long goalId = gap.getLearnerGoal() != null ? gap.getLearnerGoal().getId() : null;
        String subjectId = gap.getLearnerGoal() != null && gap.getLearnerGoal().getSubject() != null
                ? gap.getLearnerGoal().getSubject().getId() : null;
        String subjectTitle = gap.getLearnerGoal() != null && gap.getLearnerGoal().getSubject() != null
                ? gap.getLearnerGoal().getSubject().getTitle() : null;

        Long skillId = gap.getSkill() != null ? gap.getSkill().getId() : null;
        String skillName = gap.getSkill() != null ? gap.getSkill().getName() : null;
        String category = gap.getSkill() != null && gap.getSkill().getCategory() != null
                ? gap.getSkill().getCategory().getDbValue() : null;

        String statusStr = gap.getStatus() != null ? gap.getStatus().name() : null;
        String severityStr = gap.getGapSeverity() != null ? gap.getGapSeverity().name() : null;
        Long attemptId = gap.getEvaluatedFromAttempt() != null ? gap.getEvaluatedFromAttempt().getId() : null;

        return new SkillGapResponseDto(
            gap.getId(),
            userId,
            goalId,
            subjectId,
            subjectTitle,
            skillId,
            skillName,
            category,
            statusStr,
            severityStr,
            gap.getReason(),
            attemptId,
            gap.getUpdatedAt()
        );
    }
}
