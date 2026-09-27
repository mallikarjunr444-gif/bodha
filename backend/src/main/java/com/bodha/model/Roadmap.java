package com.bodha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * JPA entity mapped to the `roadmaps` table.
 *
 * Schema definition (exact):
 *   id                           BIGSERIAL     PRIMARY KEY
 *   user_id                      BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE
 *   learner_goal_id              BIGINT        UNIQUE NOT NULL REFERENCES learner_goals(id) ON DELETE CASCADE
 *   title                        VARCHAR(255)  NOT NULL
 *   curated_recommendation       TEXT          (nullable)
 *   overall_progress_percentage  INTEGER       NOT NULL DEFAULT 0 CHECK (overall_progress_percentage >= 0 AND overall_progress_percentage <= 100)
 *   is_active                    BOOLEAN       NOT NULL DEFAULT true
 *   ai_generated                 BOOLEAN       NOT NULL DEFAULT false
 *   created_at                   TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
 *   updated_at                   TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
 *
 * Architectural Significance:
 *   - The parent container for a learner's personalized curriculum path.
 *   - Exactly one active roadmap is generated per enrolled `learner_goal` (1-to-1 via `learner_goal_id UNIQUE NOT NULL`).
 *   - Bridges diagnostic skill gaps (Module E) to sequenced milestone execution (Module F):
 *       * Mastered prerequisites are bypassed or marked complete.
 *       * Identified skill gaps directly dictate the ordered roadmap modules and lessons.
 *   - `curated_recommendation` persists high-level pedagogical feedback displayed on the roadmap view.
 *   - `ai_generated` flags whether the path was synthesized via rules/templates or generated dynamically via LLM.
 *
 * Relationships:
 *   - ManyToOne -> User         (owning side; holds user_id FK).
 *   - OneToOne  -> LearnerGoal  (owning side; holds unique learner_goal_id FK).
 *   - OneToMany -> RoadmapModule (will be mapped once RoadmapModule is created).
 */
@Entity
@Table(name = "roadmaps")
public class Roadmap {

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
    // learner_goal_id: BIGINT UNIQUE NOT NULL REFERENCES learner_goals(id) ON DELETE CASCADE
    // Exactly one personalized roadmap per learner goal.
    // -------------------------------------------------------------------------
    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_goal_id", nullable = false, unique = true)
    private LearnerGoal learnerGoal;

    // -------------------------------------------------------------------------
    // title: VARCHAR(255) NOT NULL
    // e.g. "Full-Stack Java: Custom Fast-Track Roadmap"
    // -------------------------------------------------------------------------
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String title;

    // -------------------------------------------------------------------------
    // curated_recommendation: TEXT (nullable)
    // High-level pedagogical summary tailored to the learner's diagnostic baseline.
    // -------------------------------------------------------------------------
    @Column(name = "curated_recommendation", columnDefinition = "TEXT")
    private String curatedRecommendation;

    // -------------------------------------------------------------------------
    // overall_progress_percentage: INTEGER NOT NULL DEFAULT 0
    // CHECK (overall_progress_percentage >= 0 AND overall_progress_percentage <= 100)
    // -------------------------------------------------------------------------
    @Min(0)
    @Max(100)
    @Column(name = "overall_progress_percentage", nullable = false)
    private int overallProgressPercentage = 0;

    // -------------------------------------------------------------------------
    // is_active: BOOLEAN NOT NULL DEFAULT true
    // -------------------------------------------------------------------------
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // -------------------------------------------------------------------------
    // ai_generated: BOOLEAN NOT NULL DEFAULT false
    // -------------------------------------------------------------------------
    @Column(name = "ai_generated", nullable = false)
    private boolean aiGenerated = false;

    // -------------------------------------------------------------------------
    // created_at / updated_at: TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    // -------------------------------------------------------------------------
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    protected Roadmap() {
        // Required by JPA spec
    }

    public Roadmap(User user, LearnerGoal learnerGoal, String title) {
        this.user = user;
        this.learnerGoal = learnerGoal;
        this.title = title;
    }

    public Roadmap(User user, LearnerGoal learnerGoal, String title, String curatedRecommendation) {
        this.user = user;
        this.learnerGoal = learnerGoal;
        this.title = title;
        this.curatedRecommendation = curatedRecommendation;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCuratedRecommendation() {
        return curatedRecommendation;
    }

    public void setCuratedRecommendation(String curatedRecommendation) {
        this.curatedRecommendation = curatedRecommendation;
    }

    public int getOverallProgressPercentage() {
        return overallProgressPercentage;
    }

    public void setOverallProgressPercentage(int overallProgressPercentage) {
        this.overallProgressPercentage = overallProgressPercentage;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /** Alias setter for DTO mappers and JSON deserializers */
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isAiGenerated() {
        return aiGenerated;
    }

    public void setAiGenerated(boolean aiGenerated) {
        this.aiGenerated = aiGenerated;
    }

    /** Alias setter for DTO mappers and JSON deserializers */
    public void setIsAiGenerated(boolean isAiGenerated) {
        this.aiGenerated = isAiGenerated;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Roadmap{id=" + id
                + ", userId=" + (user != null ? user.getId() : null)
                + ", goalId=" + (learnerGoal != null ? learnerGoal.getId() : null)
                + ", title='" + title + '\''
                + ", progress=" + overallProgressPercentage + "%"
                + ", isActive=" + isActive
                + ", aiGenerated=" + aiGenerated + "}";
    }
}
