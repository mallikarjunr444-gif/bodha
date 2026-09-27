package com.bodha.dto;

import com.bodha.model.RoadmapModule;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Data Transfer Object representing a curriculum milestone module within a roadmap (Module F).
 */
public record RoadmapModuleResponseDto(
    Long id,
    Long roadmapId,
    String title,
    String description,
    String durationLabel,
    String duration,
    int orderIndex,
    String status,
    boolean isCurrent,
    String prerequisiteSummary,
    String prerequisite,
    List<RoadmapLessonResponseDto> lessons,
    OffsetDateTime createdAt
) {
    public static RoadmapModuleResponseDto fromEntity(RoadmapModule module, List<RoadmapLessonResponseDto> lessons) {
        if (module == null) return null;
        Long roadId = module.getRoadmap() != null ? module.getRoadmap().getId() : null;
        String statusStr = module.getStatus() != null ? module.getStatus().name().toLowerCase() : "locked";

        return new RoadmapModuleResponseDto(
            module.getId(),
            roadId,
            module.getTitle(),
            module.getDescription(),
            module.getDurationLabel(),
            module.getDurationLabel(),
            module.getOrderIndex(),
            statusStr,
            module.isCurrent(),
            module.getPrerequisiteSummary(),
            module.getPrerequisiteSummary(),
            lessons != null ? lessons : List.of(),
            module.getCreatedAt()
        );
    }
}
