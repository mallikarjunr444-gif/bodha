package com.bodha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * JPA entity mapped to the `assessment_questions` table.
 *
 * Schema definition (exact):
 *   id             BIGSERIAL     PRIMARY KEY
 *   assessment_id  BIGINT        NOT NULL REFERENCES assessments(id) ON DELETE CASCADE
 *   skill_id       BIGINT        REFERENCES skills(id) ON DELETE SET NULL (nullable)
 *   question_text  TEXT          NOT NULL
 *   explanation    TEXT          (nullable)
 *   order_index    INTEGER       NOT NULL DEFAULT 1
 *   created_at     TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
 *
 * Architectural Significance:
 *   - The direct foreign key to `skills(id)` (`skill_id`) is a core differentiator
 *     for BODHA. By tagging each question directly to a granular skill, the diagnostic
 *     engine can instantly isolate specific skill gaps (Module E) when a question is answered
 *     incorrectly, rather than just computing a generic aggregate percentage.
 *   - `order_index` allows deterministic presentation order of questions across assessments.
 *
 * Relationships:
 *   - ManyToOne -> Assessment (owning side; holds assessment_id FK).
 *   - ManyToOne -> Skill      (owning side; holds nullable skill_id FK).
 *   - OneToMany -> AssessmentOption (will be mapped once AssessmentOption is implemented).
 */
@Entity
@Table(name = "assessment_questions")
public class AssessmentQuestion {

    // -------------------------------------------------------------------------
    // Primary Key — BIGSERIAL in PostgreSQL maps to Long with GenerationType.IDENTITY
    // -------------------------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------------------------------------------------------------------------
    // assessment_id: BIGINT NOT NULL REFERENCES assessments(id) ON DELETE CASCADE
    // Questions belong to a specific assessment test.
    // -------------------------------------------------------------------------
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    // -------------------------------------------------------------------------
    // skill_id: BIGINT REFERENCES skills(id) ON DELETE SET NULL (nullable)
    // Direct link to the granular competency evaluated by this question.
    // Nullable because questions can occasionally evaluate overarching concepts.
    // -------------------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "skill_id", nullable = true)
    private Skill skill;

    // -------------------------------------------------------------------------
    // question_text: TEXT NOT NULL
    // -------------------------------------------------------------------------
    @NotBlank
    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    // -------------------------------------------------------------------------
    // explanation: TEXT (nullable)
    // Pedagogical explanation shown to the learner during review.
    // -------------------------------------------------------------------------
    @Column(columnDefinition = "TEXT")
    private String explanation;

    // -------------------------------------------------------------------------
    // order_index: INTEGER NOT NULL DEFAULT 1
    // Controls deterministic sequence of questions in an assessment.
    // -------------------------------------------------------------------------
    @Min(1)
    @Column(name = "order_index", nullable = false)
    private int orderIndex = 1;

    // -------------------------------------------------------------------------
    // created_at: TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    // -------------------------------------------------------------------------
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    protected AssessmentQuestion() {
        // Required by JPA spec
    }

    public AssessmentQuestion(Assessment assessment, String questionText) {
        this.assessment = assessment;
        this.questionText = questionText;
    }

    public AssessmentQuestion(Assessment assessment, Skill skill, String questionText,
                              String explanation, int orderIndex) {
        this.assessment = assessment;
        this.skill = skill;
        this.questionText = questionText;
        this.explanation = explanation;
        this.orderIndex = orderIndex;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------
    public Long getId() {
        return id;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "AssessmentQuestion{id=" + id + ", assessmentId=" + (assessment != null ? assessment.getId() : null)
                + ", skillId=" + (skill != null ? skill.getId() : null)
                + ", orderIndex=" + orderIndex + "}";
    }
}
