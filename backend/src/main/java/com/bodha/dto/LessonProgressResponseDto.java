package com.bodha.dto;

import com.bodha.model.LessonProgress;
import com.bodha.model.RoadmapLesson;

import java.time.OffsetDateTime;

/**
 * Data Transfer Object representing an individual lesson's progress status (Module G).
 */
public record LessonProgressResponseDto(
    Long id,
    Long userId,
    Long lessonId,
    String lessonTitle,
    String lessonType,
    Long moduleId,
    String moduleTitle,
    Long roadmapId,
    String status,
    boolean isCompleted,
    OffsetDateTime completedAt
) {
    public static LessonProgressResponseDto fromEntity(LessonProgress progress, String status) {
        if (progress == null) return null;
        Long uId = progress.getUser() != null ? progress.getUser().getId() : null;
        RoadmapLesson lesson = progress.getLesson();
        Long lId = lesson != null ? lesson.getId() : null;
        String lTitle = lesson != null ? lesson.getTitle() : null;
        String lType = lesson != null && lesson.getLessonType() != null ? lesson.getLessonType().getDbValue() : null;

        Long mId = lesson != null && lesson.getModule() != null ? lesson.getModule().getId() : null;
        String mTitle = lesson != null && lesson.getModule() != null ? lesson.getModule().getTitle() : null;
        Long rId = lesson != null && lesson.getModule() != null && lesson.getModule().getRoadmap() != null
                ? lesson.getModule().getRoadmap().getId() : null;

        return new LessonProgressResponseDto(
            progress.getId(),
            uId,
            lId,
            lTitle,
            lType,
            mId,
            mTitle,
            rId,
            status,
            progress.isCompleted(),
            progress.getCompletedAt()
        );
    }

    public static LessonProgressResponseDto notStarted(RoadmapLesson lesson, Long userId) {
        if (lesson == null) return null;
        Long mId = lesson.getModule() != null ? lesson.getModule().getId() : null;
        String mTitle = lesson.getModule() != null ? lesson.getModule().getTitle() : null;
        Long rId = lesson.getModule() != null && lesson.getModule().getRoadmap() != null
                ? lesson.getModule().getRoadmap().getId() : null;
        String lType = lesson.getLessonType() != null ? lesson.getLessonType().getDbValue() : null;

        return new LessonProgressResponseDto(
            null,
            userId,
            lesson.getId(),
            lesson.getTitle(),
            lType,
            mId,
            mTitle,
            rId,
            "NOT_STARTED",
            false,
            null
        );
    }
}
