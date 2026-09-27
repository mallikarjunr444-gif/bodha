package com.bodha.dto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Data Transfer Object providing an aggregated overview of a learner's study progress,
 * gamification XP, daily consistency streaks, and roadmap milestones (Module G).
 */
public record ProgressSummaryResponseDto(
    Long userId,
    int currentStreakDays,
    int totalXp,
    OffsetDateTime lastActiveAt,
    int totalCompletedLessons,
    RoadmapProgressResponseDto activeRoadmapProgress,
    List<UserActivityResponseDto> recentActivities
) {}
