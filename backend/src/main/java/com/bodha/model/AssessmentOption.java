package com.bodha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * JPA entity mapped to the `assessment_options` table.
 *
 * Schema definition (exact):
 *   id           BIGSERIAL  PRIMARY KEY
 *   question_id  BIGINT     NOT NULL REFERENCES assessment_questions(id) ON DELETE CASCADE
 *   option_text  TEXT       NOT NULL
 *   is_correct   BOOLEAN    NOT NULL DEFAULT false
 *   order_index  INTEGER    NOT NULL DEFAULT 1
 *
 * Architectural Significance:
 *   - Represents a normalized (3NF) multiple-choice answer option belonging to an AssessmentQuestion.
 *   - `is_correct` identifies the ground truth answer used by the diagnostic engine to evaluate
 *     competencies and calculate score percentages.
 *   - `order_index` guarantees deterministic display ordering of choices (e.g. A, B, C, D) in the UI.
 *
 * Relationships:
 *   - ManyToOne -> AssessmentQuestion (owning side; holds question_id FK).
 */
@Entity
@Table(name = "assessment_options")
public class AssessmentOption {

    // -------------------------------------------------------------------------
    // Primary Key — BIGSERIAL in PostgreSQL maps to Long with GenerationType.IDENTITY
    // -------------------------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------------------------------------------------------------------------
    // question_id: BIGINT NOT NULL REFERENCES assessment_questions(id) ON DELETE CASCADE
    // Options belong to a specific assessment question.
    // -------------------------------------------------------------------------
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private AssessmentQuestion question;

    // -------------------------------------------------------------------------
    // option_text: TEXT NOT NULL
    // -------------------------------------------------------------------------
    @NotBlank
    @Column(name = "option_text", columnDefinition = "TEXT", nullable = false)
    private String optionText;

    // -------------------------------------------------------------------------
    // is_correct: BOOLEAN NOT NULL DEFAULT false
    // True if this option is the correct answer to the question.
    // -------------------------------------------------------------------------
    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect = false;

    // -------------------------------------------------------------------------
    // order_index: INTEGER NOT NULL DEFAULT 1
    // Controls deterministic ordering of choices (A, B, C, D) for the UI.
    // -------------------------------------------------------------------------
    @Min(1)
    @Column(name = "order_index", nullable = false)
    private int orderIndex = 1;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    protected AssessmentOption() {
        // Required by JPA spec
    }

    public AssessmentOption(AssessmentQuestion question, String optionText, boolean isCorrect) {
        this.question = question;
        this.optionText = optionText;
        this.isCorrect = isCorrect;
    }

    public AssessmentOption(AssessmentQuestion question, String optionText, boolean isCorrect, int orderIndex) {
        this.question = question;
        this.optionText = optionText;
        this.isCorrect = isCorrect;
        this.orderIndex = orderIndex;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------
    public Long getId() {
        return id;
    }

    public AssessmentQuestion getQuestion() {
        return question;
    }

    public void setQuestion(AssessmentQuestion question) {
        this.question = question;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        this.isCorrect = correct;
    }

    /** Alias setter for frameworks or DTO mappers */
    public void setIsCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    @Override
    public String toString() {
        return "AssessmentOption{id=" + id + ", questionId=" + (question != null ? question.getId() : null)
                + ", isCorrect=" + isCorrect + ", orderIndex=" + orderIndex + "}";
    }
}
