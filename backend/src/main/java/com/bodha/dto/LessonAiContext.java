package com.bodha.dto;

/**
 * Contextual metadata for a specific roadmap lesson passed to AI assistance services (Module H).
 */
public record LessonAiContext(
        Long lessonId,
        String title,
        String lessonType,
        String contentBody,
        int orderIndex,
        boolean isCompleted,
        Long moduleId,
        String moduleTitle,
        String durationLabel
) {}
