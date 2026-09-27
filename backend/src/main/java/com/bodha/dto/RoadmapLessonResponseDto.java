package com.bodha.dto;

import com.bodha.model.RoadmapLesson;

import java.time.OffsetDateTime;

/**
 * Data Transfer Object representing an individual lesson within a roadmap module (Module F).
 */
public record RoadmapLessonResponseDto(
    Long id,
    Long moduleId,
    String title,
    String lessonType,
    String type,
    String contentBody,
    int orderIndex,
    boolean completed,
    OffsetDateTime createdAt
) {
    public static RoadmapLessonResponseDto fromEntity(RoadmapLesson lesson) {
        if (lesson == null) return null;
        String typeStr = lesson.getLessonType() != null ? lesson.getLessonType().getDbValue() : null;
        Long modId = lesson.getModule() != null ? lesson.getModule().getId() : null;

        return new RoadmapLessonResponseDto(
            lesson.getId(),
            modId,
            lesson.getTitle(),
            typeStr,
            typeStr,
            lesson.getContentBody(),
            lesson.getOrderIndex(),
            false,
            lesson.getCreatedAt()
        );
    }
}
