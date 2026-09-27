package com.bodha.dto;

import java.util.List;

/**
 * Data Transfer Object representing aggregate progress across a personalized roadmap (Module G).
 */
public record RoadmapProgressResponseDto(
    Long roadmapId,
    Long userId,
    Long learnerGoalId,
    String subjectId,
    String title,
    int totalLessons,
    int completedLessons,
    int overallProgressPercentage,
    double completionPercentage,
    List<ModuleProgressResponseDto> modules
) {}
