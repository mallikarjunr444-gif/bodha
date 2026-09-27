package com.bodha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * JPA entity mapped to the `lesson_progress` table.
 *
 * Schema definition (exact):
 *   id            BIGSERIAL    PRIMARY KEY
 *   user_id       BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE
 *   lesson_id     BIGINT       NOT NULL REFERENCES roadmap_lessons(id) ON DELETE CASCADE
 *   is_completed  BOOLEAN      NOT NULL DEFAULT true
 *   completed_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
 *   CONSTRAINT uq_user_lesson UNIQUE (user_id, lesson_id)
 *
 * Architectural Significance:
 *   - Tracks individual lesson completion per user across their personalized learning roadmaps.
 *   - The unique constraint `uq_user_lesson` guarantees idempotent progress recording per user/lesson pair.
 *   - Drives milestone progression:
 *       * Triggers automatic recalculation of `roadmaps.overall_progress_percentage`.
 *       * Promotes module state (from `UNLOCKED` to `COMPLETED`) once all child lessons are finished.
 *       * Feeds daily completion counts into `user_activity_logs` and increments `learner_profiles.total_xp`.
 *
 * Relationships:
 *   - ManyToOne -> User          (owning side; holds user_id FK).
 *   - ManyToOne -> RoadmapLesson (owning side; holds lesson_id FK).
 */
@Entity
@Table(
    name = "lesson_progress",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_lesson",
        columnNames = {"user_id", "lesson_id"}
    )
)
public class LessonProgress {

    // -------------------------------------------------------------------------
    // Primary Key — BIGSERIAL in PostgreSQL maps to Long with GenerationType.IDENTITY
    // -------------------------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------------------------------------------------------------------------
    // user_id: BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE
    // The learner who completed this lesson.
    // -------------------------------------------------------------------------
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // -------------------------------------------------------------------------
    // lesson_id: BIGINT NOT NULL REFERENCES roadmap_lessons(id) ON DELETE CASCADE
    // The specific roadmap lesson that was completed.
    // -------------------------------------------------------------------------
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private RoadmapLesson lesson;

    // -------------------------------------------------------------------------
    // is_completed: BOOLEAN NOT NULL DEFAULT true
    // -------------------------------------------------------------------------
    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = true;

    // -------------------------------------------------------------------------
    // completed_at: TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    // Timestamp when the lesson was finished.
    // -------------------------------------------------------------------------
    @CreationTimestamp
    @Column(name = "completed_at", nullable = false)
    private OffsetDateTime completedAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    protected LessonProgress() {
        // Required by JPA spec
    }

    public LessonProgress(User user, RoadmapLesson lesson) {
        this.user = user;
        this.lesson = lesson;
        this.isCompleted = true;
    }

    public LessonProgress(User user, RoadmapLesson lesson, boolean isCompleted) {
        this.user = user;
        this.lesson = lesson;
        this.isCompleted = isCompleted;
    }

    public LessonProgress(User user, RoadmapLesson lesson, boolean isCompleted, OffsetDateTime completedAt) {
        this.user = user;
        this.lesson = lesson;
        this.isCompleted = isCompleted;
        this.completedAt = completedAt;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public RoadmapLesson getLesson() {
        return lesson;
    }

    public void setLesson(RoadmapLesson lesson) {
        this.lesson = lesson;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    /** Alias setter for DTO mappers and JSON deserializers */
    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public String toString() {
        return "LessonProgress{id=" + id
                + ", userId=" + (user != null ? user.getId() : null)
                + ", lessonId=" + (lesson != null ? lesson.getId() : null)
                + ", isCompleted=" + isCompleted
                + ", completedAt=" + completedAt + "}";
    }
}
