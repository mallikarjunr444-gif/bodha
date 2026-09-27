package com.bodha.dto;

/**
 * Structured response recommending the learner's immediate next pedagogical step
 * based on current module position, uncompleted lessons, and skill gap priorities (Module H).
 */
public record AiNextStepResponseDto(
        String title,
        String summary,
        String currentModuleTitle,
        Long nextLessonId,
        String nextLessonTitle,
        String suggestedAction,
        String reason,
        int overallProgressPercentage,
        int remainingLessonsInModule,
        boolean fallbackUsed,
        String provider
) {}
