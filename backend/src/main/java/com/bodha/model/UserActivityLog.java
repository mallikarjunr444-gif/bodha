package com.bodha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * JPA entity mapped to the `user_activity_logs` table.
 *
 * Schema definition (exact):
 *   id                 BIGSERIAL    PRIMARY KEY
 *   user_id            BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE
 *   activity_date      DATE         NOT NULL
 *   minutes_spent      INTEGER      NOT NULL DEFAULT 0 CHECK (minutes_spent >= 0)
 *   lessons_completed  INTEGER      NOT NULL DEFAULT 0 CHECK (lessons_completed >= 0)
 *   streak_maintained  BOOLEAN      NOT NULL DEFAULT true
 *   created_at         TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
 *   CONSTRAINT uq_user_activity_date UNIQUE (user_id, activity_date)
 *
 * Architectural Significance:
 *   - Captures daily granular study telemetry per user.
 *   - The unique constraint `uq_user_activity_date` ensures a single aggregation record per learner per day.
 *   - Directly powers:
 *       * Daily study metrics and time commitment calculations on the learner dashboard.
 *       * Consecutive study day streak tracking (`learner_profiles.current_streak_days`).
 *       * Gamification XP calculation based on daily lessons completed.
 *
 * Relationships:
 *   - ManyToOne -> User (owning side; holds user_id FK).
 */
@Entity
@Table(
    name = "user_activity_logs",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_activity_date",
        columnNames = {"user_id", "activity_date"}
    )
)
public class UserActivityLog {

    // -------------------------------------------------------------------------
    // Primary Key — BIGSERIAL in PostgreSQL maps to Long with GenerationType.IDENTITY
    // -------------------------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------------------------------------------------------------------------
    // user_id: BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE
    // The learner associated with this daily activity record.
    // -------------------------------------------------------------------------
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // -------------------------------------------------------------------------
    // activity_date: DATE NOT NULL
    // The specific calendar date of the recorded activity.
    // -------------------------------------------------------------------------
    @NotNull
    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    // -------------------------------------------------------------------------
    // minutes_spent: INTEGER NOT NULL DEFAULT 0 CHECK (minutes_spent >= 0)
    // Total study minutes invested by the user on this date.
    // -------------------------------------------------------------------------
    @Min(0)
    @Column(name = "minutes_spent", nullable = false)
    private int minutesSpent = 0;

    // -------------------------------------------------------------------------
    // lessons_completed: INTEGER NOT NULL DEFAULT 0 CHECK (lessons_completed >= 0)
    // Count of roadmap lessons finished on this date.
    // -------------------------------------------------------------------------
    @Min(0)
    @Column(name = "lessons_completed", nullable = false)
    private int lessonsCompleted = 0;

    // -------------------------------------------------------------------------
    // streak_maintained: BOOLEAN NOT NULL DEFAULT true
    // Whether this day's activity satisfied the minimum streak continuation threshold.
    // -------------------------------------------------------------------------
    @Column(name = "streak_maintained", nullable = false)
    private boolean streakMaintained = true;

    // -------------------------------------------------------------------------
    // created_at: TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    // Audit timestamp when this log entry was initially created.
    // -------------------------------------------------------------------------
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    protected UserActivityLog() {
        // Required by JPA spec
    }

    public UserActivityLog(User user, LocalDate activityDate) {
        this.user = user;
        this.activityDate = activityDate;
        this.minutesSpent = 0;
        this.lessonsCompleted = 0;
        this.streakMaintained = true;
    }

    public UserActivityLog(User user, LocalDate activityDate, int minutesSpent,
                           int lessonsCompleted, boolean streakMaintained) {
        this.user = user;
        this.activityDate = activityDate;
        this.minutesSpent = minutesSpent;
        this.lessonsCompleted = lessonsCompleted;
        this.streakMaintained = streakMaintained;
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

    public LocalDate getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(LocalDate activityDate) {
        this.activityDate = activityDate;
    }

    public int getMinutesSpent() {
        return minutesSpent;
    }

    public void setMinutesSpent(int minutesSpent) {
        this.minutesSpent = minutesSpent;
    }

    public int getLessonsCompleted() {
        return lessonsCompleted;
    }

    public void setLessonsCompleted(int lessonsCompleted) {
        this.lessonsCompleted = lessonsCompleted;
    }

    public boolean isStreakMaintained() {
        return streakMaintained;
    }

    public void setStreakMaintained(boolean streakMaintained) {
        this.streakMaintained = streakMaintained;
    }

    /** Alias setter for frameworks and JSON deserializers */
    public void setIsStreakMaintained(boolean streakMaintained) {
        this.streakMaintained = streakMaintained;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "UserActivityLog{id=" + id
                + ", userId=" + (user != null ? user.getId() : null)
                + ", activityDate=" + activityDate
                + ", minutesSpent=" + minutesSpent
                + ", lessonsCompleted=" + lessonsCompleted
                + ", streakMaintained=" + streakMaintained + "}";
    }
}
