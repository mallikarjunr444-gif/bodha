package com.bodha.dto;

import java.util.List;

/**
 * Structured response containing context-aware AI tutoring for a specific roadmap lesson (Module H).
 */
public record AiLessonAssistanceResponseDto(
        Long lessonId,
        String lessonTitle,
        String lessonType,
        String moduleTitle,
        String explanation,
        List<String> learningTips,
        List<String> commonMistakes,
        List<String> practiceSuggestions,
        boolean fallbackUsed,
        String provider
) {}
