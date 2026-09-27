package com.bodha.dto;

import com.bodha.model.Roadmap;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Data Transfer Object representing a personalized roadmap (Module F).
 * Bridges evaluated skill gaps to sequenced curriculum milestones.
 */
public record RoadmapResponseDto(
    Long id,
    Long userId,
    Long learnerGoalId,
    String subjectId,
    String subjectTitle,
    String title,
    String curatedRecommendation,
    int overallProgressPercentage,
    boolean isActive,
    boolean aiGenerated,
    int totalModules,
    int totalLessons,
    List<RoadmapModuleResponseDto> modules,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static RoadmapResponseDto fromEntity(Roadmap roadmap, List<RoadmapModuleResponseDto> modules) {
        if (roadmap == null) return null;
        Long uId = roadmap.getUser() != null ? roadmap.getUser().getId() : null;
        Long gId = roadmap.getLearnerGoal() != null ? roadmap.getLearnerGoal().getId() : null;
        String sId = roadmap.getLearnerGoal() != null && roadmap.getLearnerGoal().getSubject() != null
                ? roadmap.getLearnerGoal().getSubject().getId() : null;
        String sTitle = roadmap.getLearnerGoal() != null && roadmap.getLearnerGoal().getSubject() != null
                ? roadmap.getLearnerGoal().getSubject().getTitle() : null;

        List<RoadmapModuleResponseDto> modList = modules != null ? modules : List.of();
        int lessonCount = modList.stream()
                .mapToInt(m -> m.lessons() != null ? m.lessons().size() : 0)
                .sum();

        return new RoadmapResponseDto(
            roadmap.getId(),
            uId,
            gId,
            sId,
            sTitle,
            roadmap.getTitle(),
            roadmap.getCuratedRecommendation(),
            roadmap.getOverallProgressPercentage(),
            roadmap.isActive(),
            roadmap.isAiGenerated(),
            modList.size(),
            lessonCount,
            modList,
            roadmap.getCreatedAt(),
            roadmap.getUpdatedAt()
        );
    }
}
