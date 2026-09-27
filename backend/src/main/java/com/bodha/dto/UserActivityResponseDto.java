package com.bodha.dto;

import com.bodha.model.UserActivityLog;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Data Transfer Object representing daily study activity and consistency telemetry (Module G).
 */
public record UserActivityResponseDto(
    Long id,
    Long userId,
    LocalDate activityDate,
    int minutesSpent,
    int lessonsCompleted,
    boolean streakMaintained,
    OffsetDateTime createdAt
) {
    public static UserActivityResponseDto fromEntity(UserActivityLog log) {
        if (log == null) return null;
        Long uId = log.getUser() != null ? log.getUser().getId() : null;

        return new UserActivityResponseDto(
            log.getId(),
            uId,
            log.getActivityDate(),
            log.getMinutesSpent(),
            log.getLessonsCompleted(),
            log.isStreakMaintained(),
            log.getCreatedAt()
        );
    }
}
