package com.bodha.dto;

import com.bodha.model.LearnerGoal;

import java.time.OffsetDateTime;

/**
 * Data Transfer Object representing a configured learner goal (Module C).
 * Exposes learner-facing goal attributes, study commitment, and status.
 */
public record LearnerGoalResponseDto(
    Long id,
    Long userId,
    String subjectId,
    String subjectTitle,
    String goalType,
    String baselineLevel,
    int dailyTimeMinutes,
    String dailyTime,
    String status,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static LearnerGoalResponseDto fromEntity(LearnerGoal goal) {
        if (goal == null) return null;
        Long uId = goal.getUser() != null ? goal.getUser().getId() : null;
        String sId = goal.getSubject() != null ? goal.getSubject().getId() : null;
        String sTitle = goal.getSubject() != null ? goal.getSubject().getTitle() : null;
        String gType = goal.getGoalType() != null ? goal.getGoalType().getDbValue() : null;
        String bLevel = goal.getBaselineLevel() != null ? goal.getBaselineLevel().getDbValue() : null;
        String stat = goal.getStatus() != null ? goal.getStatus().name() : null;
        String paceStr = goal.getDailyTimeMinutes() + "m";

        return new LearnerGoalResponseDto(
            goal.getId(),
            uId,
            sId,
            sTitle,
            gType,
            bLevel,
            goal.getDailyTimeMinutes(),
            paceStr,
            stat,
            goal.getCreatedAt(),
            goal.getUpdatedAt()
        );
    }
}
