package com.bodha.dto;

import java.util.List;

/**
 * Data Transfer Object representing progress across an individual roadmap milestone module (Module G).
 */
public record ModuleProgressResponseDto(
    Long moduleId,
    String title,
    String durationLabel,
    int orderIndex,
    String status,
    boolean isCurrent,
    int totalLessons,
    int completedLessons,
    double progressPercentage,
    List<LessonProgressResponseDto> lessons
) {}
