package com.bodha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity mapped to the `learner_goals` table.
 *
 * Schema definition (exact):
 *   id                   BIGSERIAL    PRIMARY KEY
 *   user_id              BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE
 *   subject_id           VARCHAR(80)  NOT NULL REFERENCES subjects(id) ON DELETE CASCADE
 *   goal_type            VARCHAR(50)  NOT NULL CHECK ('career','exam','project','mastery')
 *   baseline_level       VARCHAR(50)  NOT NULL CHECK ('beginner','intermediate','advanced')
 *   daily_time_minutes   INTEGER      NOT NULL CHECK (IN (15, 30, 45, 60))
 *   status               VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE' CHECK ('ACTIVE','PAUSED','COMPLETED')
 *   created_at           TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
 *   updated_at           TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
 *   CONSTRAINT uq_user_subject_goal UNIQUE (user_id, subject_id)
 *
 * Relationships:
 *   - ManyToOne -> User      (this table holds user_id FK)
 *   - ManyToOne -> Subject   (this table holds subject_id FK)
 *   - OneToMany -> AssessmentAttempt (learner_goal_id FK in assessment_attempts)
 */
@Entity
@Table(
    name = "learner_goals",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_subject_goal",
        columnNames = {"user_id", "subject_id"}
    )
)
public class LearnerGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------------------------------------------------------------------------
    // user_id: BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE
    // -------------------------------------------------------------------------
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // -------------------------------------------------------------------------
    // subject_id: VARCHAR(80) NOT NULL REFERENCES subjects(id) ON DELETE CASCADE
    // -------------------------------------------------------------------------
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // -------------------------------------------------------------------------
    // goal_type: VARCHAR(50) NOT NULL CHECK ('career','exam','project','mastery')
    // GoalTypeConverter (autoApply=true) maps CAREER <-> 'career', etc.
    // -------------------------------------------------------------------------
    @NotNull
    @Column(name = "goal_type", nullable = false, length = 50)
    private GoalType goalType;

    // -------------------------------------------------------------------------
    // baseline_level: VARCHAR(50) NOT NULL CHECK ('beginner','intermediate','advanced')
    // BaselineLevelConverter (autoApply=true) maps BEGINNER <-> 'beginner', etc.
    // -------------------------------------------------------------------------
    @NotNull
    @Column(name = "baseline_level", nullable = false, length = 50)
    private BaselineLevel baselineLevel;

    // -------------------------------------------------------------------------
    // daily_time_minutes: INTEGER NOT NULL CHECK (IN (15, 30, 45, 60))
    // Modelled as a plain int — the DB CHECK constraint enforces allowed values.
    // No enum needed: the four integer options map more naturally as an int.
    // -------------------------------------------------------------------------
    @Column(name = "daily_time_minutes", nullable = false)
    private int dailyTimeMinutes;

    // -------------------------------------------------------------------------
    // status: VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
    //         CHECK (status IN ('ACTIVE', 'PAUSED', 'COMPLETED'))
    //
    // @Enumerated(EnumType.STRING) works directly here — all values are valid
    // Java identifiers stored as UPPERCASE in PostgreSQL. No converter needed.
    // -------------------------------------------------------------------------
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, columnDefinition = "VARCHAR(30) DEFAULT 'ACTIVE'")
    private GoalStatus status = GoalStatus.ACTIVE;

    // -------------------------------------------------------------------------
    // created_at / updated_at: TIMESTAMPTZ NOT NULL
    // -------------------------------------------------------------------------
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // -------------------------------------------------------------------------
    // Relationship: LearnerGoal -> AssessmentAttempt (One-to-Many)
    // learner_goal_id FK is nullable in assessment_attempts — a diagnostic
    // attempt is linked to a goal, but goal is not required for standalone tests.
    // -------------------------------------------------------------------------
    @OneToMany(mappedBy = "learnerGoal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AssessmentAttempt> assessmentAttempts = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    protected LearnerGoal() {}

    public LearnerGoal(User user, Subject subject, GoalType goalType,
                       BaselineLevel baselineLevel, int dailyTimeMinutes) {
        this.user = user;
        this.subject = subject;
        this.goalType = goalType;
        this.baselineLevel = baselineLevel;
        this.dailyTimeMinutes = dailyTimeMinutes;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------
    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public GoalType getGoalType() { return goalType; }
    public void setGoalType(GoalType goalType) { this.goalType = goalType; }

    public BaselineLevel getBaselineLevel() { return baselineLevel; }
    public void setBaselineLevel(BaselineLevel baselineLevel) { this.baselineLevel = baselineLevel; }

    public int getDailyTimeMinutes() { return dailyTimeMinutes; }
    public void setDailyTimeMinutes(int dailyTimeMinutes) { this.dailyTimeMinutes = dailyTimeMinutes; }

    public GoalStatus getStatus() { return status; }
    public void setStatus(GoalStatus status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public List<AssessmentAttempt> getAssessmentAttempts() { return assessmentAttempts; }

    @Override
    public String toString() {
        return "LearnerGoal{id=" + id + ", goalType=" + goalType
                + ", status=" + status + ", dailyTimeMinutes=" + dailyTimeMinutes + "}";
    }
}
