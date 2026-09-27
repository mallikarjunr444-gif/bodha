package com.bodha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * JPA entity mapped to the `assessment_attempts` table.
 *
 * Schema definition (exact):
 *   id                BIGSERIAL      PRIMARY KEY
 *   user_id           BIGINT         NOT NULL REFERENCES users(id) ON DELETE CASCADE
 *   assessment_id     BIGINT         NOT NULL REFERENCES assessments(id) ON DELETE CASCADE
 *   learner_goal_id   BIGINT         REFERENCES learner_goals(id) ON DELETE CASCADE (nullable)
 *   score_percentage  NUMERIC(5,2)   NOT NULL CHECK (score_percentage >= 0 AND score_percentage <= 100)
 *   total_questions   INTEGER        NOT NULL
 *   correct_answers   INTEGER        NOT NULL
 *   completed_at      TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP
 *
 * Architectural Significance:
 *   - Serves as the junction connecting a learner's goal (`learner_goals`) with a curriculum test (`assessments`).
 *   - While an Assessment belongs to a Subject (curriculum catalog level), an AssessmentAttempt
 *     captures a specific learner's evaluation instance towards their enrolled goal.
 *   - Captures high-level performance metrics (`score_percentage`, `correct_answers`) used to compute
 *     skill mastery vs gap thresholds (Module E).
 *
 * Relationships:
 *   - ManyToOne -> User         (owning side; holds user_id FK).
 *   - ManyToOne -> Assessment   (owning side; holds assessment_id FK).
 *   - ManyToOne -> LearnerGoal  (owning side; holds nullable learner_goal_id FK, mappedBy in LearnerGoal).
 *   - OneToMany -> AssessmentResponse (will be mapped once AssessmentResponse is created).
 */
@Entity
@Table(name = "assessment_attempts")
public class AssessmentAttempt {

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
    // assessment_id: BIGINT NOT NULL REFERENCES assessments(id) ON DELETE CASCADE
    // -------------------------------------------------------------------------
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    // -------------------------------------------------------------------------
    // learner_goal_id: BIGINT REFERENCES learner_goals(id) ON DELETE CASCADE
    // Nullable: diagnostic tests can be taken standalone or linked to an active goal.
    // Matches mappedBy = "learnerGoal" in LearnerGoal.java.
    // -------------------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "learner_goal_id", nullable = true)
    private LearnerGoal learnerGoal;

    // -------------------------------------------------------------------------
    // score_percentage: NUMERIC(5,2) NOT NULL CHECK (score_percentage >= 0 AND <= 100)
    // -------------------------------------------------------------------------
    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Digits(integer = 3, fraction = 2)
    @Column(name = "score_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal scorePercentage;

    // -------------------------------------------------------------------------
    // total_questions: INTEGER NOT NULL
    // -------------------------------------------------------------------------
    @Min(0)
    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;

    // -------------------------------------------------------------------------
    // correct_answers: INTEGER NOT NULL
    // -------------------------------------------------------------------------
    @Min(0)
    @Column(name = "correct_answers", nullable = false)
    private int correctAnswers;

    // -------------------------------------------------------------------------
    @Column(name = "completed_at", nullable = false)
    private OffsetDateTime completedAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    protected AssessmentAttempt() {
        // Required by JPA spec
    }

    public AssessmentAttempt(User user, Assessment assessment, BigDecimal scorePercentage,
                             int totalQuestions, int correctAnswers) {
        this.user = user;
        this.assessment = assessment;
        this.scorePercentage = scorePercentage;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.completedAt = OffsetDateTime.now();
    }

    public AssessmentAttempt(User user, Assessment assessment, LearnerGoal learnerGoal,
                             BigDecimal scorePercentage, int totalQuestions, int correctAnswers) {
        this.user = user;
        this.assessment = assessment;
        this.learnerGoal = learnerGoal;
        this.scorePercentage = scorePercentage;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.completedAt = OffsetDateTime.now();
    }

    /**
     * Checks if the attempt has been evaluated and completed.
     * In-progress attempts have totalQuestions = 0; completed attempts have totalQuestions > 0.
     */
    public boolean isCompleted() {
        return this.totalQuestions > 0;
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

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public LearnerGoal getLearnerGoal() {
        return learnerGoal;
    }

    public void setLearnerGoal(LearnerGoal learnerGoal) {
        this.learnerGoal = learnerGoal;
    }

    public BigDecimal getScorePercentage() {
        return scorePercentage;
    }

    public void setScorePercentage(BigDecimal scorePercentage) {
        this.scorePercentage = scorePercentage;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public String toString() {
        return "AssessmentAttempt{id=" + id + ", userId=" + (user != null ? user.getId() : null)
                + ", assessmentId=" + (assessment != null ? assessment.getId() : null)
                + ", goalId=" + (learnerGoal != null ? learnerGoal.getId() : null)
                + ", score=" + scorePercentage + "%"
                + ", correct=" + correctAnswers + "/" + totalQuestions + "}";
    }
}
