package com.bodha.dto;

import java.util.List;

/**
 * Structured response containing personalized learning recommendations synthesized from
 * learner state, diagnostic gaps, and active roadmap progress (Module H).
 */
public record AiRecommendationResponseDto(
        String title,
        String summary,
        String reason,
        String priority, // e.g. "HIGH", "MEDIUM", "LOW"
        String relatedSkill,
        String relatedLesson,
        String suggestedAction,
        String learningApproach,
        List<String> practiceSuggestions,
        boolean fallbackUsed,
        String provider
) {}
