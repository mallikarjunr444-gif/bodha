package com.bodha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * JPA entity mapped to the `learner_skill_gaps` table.
 *
 * Schema definition (exact):
 *   id                         BIGSERIAL    PRIMARY KEY
 *   user_id                    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE
 *   learner_goal_id            BIGINT       NOT NULL REFERENCES learner_goals(id) ON DELETE CASCADE
 *   skill_id                   BIGINT       NOT NULL REFERENCES skills(id) ON DELETE CASCADE
 *   status                     VARCHAR(30)  NOT NULL CHECK (status IN ('MASTERED', 'GAP'))
 *   gap_severity               VARCHAR(30)  CHECK (gap_severity IN ('HIGH', 'MEDIUM', 'LOW') OR gap_severity IS NULL)
 *   reason                     TEXT         (nullable)
 *   evaluated_from_attempt_id  BIGINT       REFERENCES assessment_attempts(id) ON DELETE SET NULL (nullable)
 *   updated_at                 TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
 *   CONSTRAINT uq_user_goal_skill UNIQUE (user_id, learner_goal_id, skill_id)
 *
 * Architectural Significance:
 *   - The foundational data structure of BODHA's Skill-Gap Engine.
 *   - Represents the persistent competency matrix for a learner enrolled in a specific target goal.
 *   - Directly drives the Skill-Gap Analysis dashboard (/skill-gap):
 *       * MASTERED skills are accelerated or skipped in the generated roadmap.
 *       * GAP skills (HIGH / MEDIUM / LOW severity) become the targeted milestones in Module F roadmaps.
 *   - Tracks provenance: `evaluated_from_attempt_id` records which diagnostic attempt determined this status.
 *
 * Relationships:
 *   - ManyToOne -> User              (owning side; holds user_id FK).
 *   - ManyToOne -> LearnerGoal       (owning side; holds learner_goal_id FK).
 *   - ManyToOne -> Skill             (owning side; holds skill_id FK).
 *   - ManyToOne -> AssessmentAttempt (owning side; holds nullable evaluated_from_attempt_id FK).
 */
@Entity
@Table(
    name = "learner_skill_gaps",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_goal_skill",
        columnNames = {"user_id", "learner_goal_id", "skill_id"}
    )
)
public class LearnerSkillGap {

    /**
     * Evaluation status of a competency.
     * Schema: CHECK (status IN ('MASTERED', 'GAP'))
     */
    public enum Status {
        MASTERED,
        GAP
    }

    /**
     * Deficiency severity level for an identified skill gap.
     * Schema: CHECK (gap_severity IN ('HIGH', 'MEDIUM', 'LOW') OR gap_severity IS NULL)
     */
    public enum Severity {
        HIGH,
        MEDIUM,
        LOW
    }

    // -------------------------------------------------------------------------
    // Primary Key — BIGSERIAL in PostgreSQL maps to Long with GenerationType.IDENTITY
    // -------------------------------------------------------------------------
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
    // learner_goal_id: BIGINT NOT NULL REFERENCES learner_goals(id) ON DELETE CASCADE
    // Binds the evaluated skill status to the specific target goal track.
    // -------------------------------------------------------------------------
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_goal_id", nullable = false)
    private LearnerGoal learnerGoal;

    // -------------------------------------------------------------------------
    // skill_id: BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE
    // The granular skill competency being evaluated.
    // -------------------------------------------------------------------------
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    // -------------------------------------------------------------------------
    // status: VARCHAR(30) NOT NULL CHECK (status IN ('MASTERED', 'GAP'))
    // -------------------------------------------------------------------------
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private Status status;

    // -------------------------------------------------------------------------
    // gap_severity: VARCHAR(30) CHECK (gap_severity IN ('HIGH', 'MEDIUM', 'LOW') OR NULL)
    // Nullable for MASTERED skills.
    // -------------------------------------------------------------------------
    @Enumerated(EnumType.STRING)
    @Column(name = "gap_severity", length = 30)
    private Severity gapSeverity;

    // -------------------------------------------------------------------------
    // reason: TEXT (nullable)
    // Pedagogical explanation why this topic is accelerated, prioritized, or remediated.
    // -------------------------------------------------------------------------
    @Column(columnDefinition = "TEXT")
    private String reason;

    // -------------------------------------------------------------------------
    // evaluated_from_attempt_id: BIGINT REFERENCES assessment_attempts(id) ON DELETE SET NULL
    // Nullable audit link connecting this gap diagnosis to the triggering assessment attempt.
    // -------------------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "evaluated_from_attempt_id", nullable = true)
    private AssessmentAttempt evaluatedFromAttempt;

    // -------------------------------------------------------------------------
    // updated_at: TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    // Refreshed whenever re-evaluations or module quizzes adjust the skill state.
    // -------------------------------------------------------------------------
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    protected LearnerSkillGap() {
        // Required by JPA spec
    }

    public LearnerSkillGap(User user, LearnerGoal learnerGoal, Skill skill, Status status) {
        this.user = user;
        this.learnerGoal = learnerGoal;
        this.skill = skill;
        this.status = status;
    }

    public LearnerSkillGap(User user, LearnerGoal learnerGoal, Skill skill, Status status,
                           Severity gapSeverity, String reason, AssessmentAttempt evaluatedFromAttempt) {
        this.user = user;
        this.learnerGoal = learnerGoal;
        this.skill = skill;
        this.status = status;
        this.gapSeverity = gapSeverity;
        this.reason = reason;
        this.evaluatedFromAttempt = evaluatedFromAttempt;
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

    public LearnerGoal getLearnerGoal() {
        return learnerGoal;
    }

    public void setLearnerGoal(LearnerGoal learnerGoal) {
        this.learnerGoal = learnerGoal;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Severity getGapSeverity() {
        return gapSeverity;
    }

    public void setGapSeverity(Severity gapSeverity) {
        this.gapSeverity = gapSeverity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public AssessmentAttempt getEvaluatedFromAttempt() {
        return evaluatedFromAttempt;
    }

    public void setEvaluatedFromAttempt(AssessmentAttempt evaluatedFromAttempt) {
        this.evaluatedFromAttempt = evaluatedFromAttempt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "LearnerSkillGap{id=" + id
                + ", userId=" + (user != null ? user.getId() : null)
                + ", goalId=" + (learnerGoal != null ? learnerGoal.getId() : null)
                + ", skillId=" + (skill != null ? skill.getId() : null)
                + ", status=" + status
                + ", severity=" + gapSeverity + "}";
    }
}
