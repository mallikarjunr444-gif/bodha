package com.bodha.dto;

import com.bodha.model.Subject;

import java.util.List;

/**
 * Data Transfer Object representing a curriculum subject track (Module B).
 *
 * Provides fields required by the frontend Choose Subject page and journey navigation.
 */
public record SubjectResponseDto(
    String id,
    String domainId,
    String domain,
    String domainName,
    String title,
    String tagline,
    String difficultyLevel,
    String difficulty,
    int estimatedWeeks,
    boolean isPopular,
    boolean popular,
    boolean isCustom,
    List<String> skillsCovered
) {
    public static SubjectResponseDto fromEntity(Subject subject) {
        return fromEntity(subject, List.of());
    }

    public static SubjectResponseDto fromEntity(Subject subject, List<String> skillsCovered) {
        if (subject == null) return null;
        String domainId = subject.getDomain() != null ? subject.getDomain().getId() : null;
        String domainName = subject.getDomain() != null ? subject.getDomain().getName() : null;
        String diff = subject.getDifficultyLevel() != null ? subject.getDifficultyLevel().getDbValue() : null;
        List<String> skills = (skillsCovered != null) ? skillsCovered : List.of();

        return new SubjectResponseDto(
            subject.getId(),
            domainId,
            domainId,
            domainName,
            subject.getTitle(),
            subject.getTagline(),
            diff,
            diff,
            subject.getEstimatedWeeks(),
            subject.isPopular(),
            subject.isPopular(),
            subject.isCustom(),
            skills
        );
    }
}
