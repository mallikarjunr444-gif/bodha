package com.bodha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * JPA entity mapped to the `assessment_responses` table.
 *
 * Schema definition (exact):
 *   id                  BIGSERIAL  PRIMARY KEY
 *   attempt_id          BIGINT     NOT NULL REFERENCES assessment_attempts(id) ON DELETE CASCADE
 *   question_id         BIGINT     NOT NULL REFERENCES assessment_questions(id) ON DELETE CASCADE
 *   selected_option_id  BIGINT     NOT NULL REFERENCES assessment_options(id) ON DELETE CASCADE
 *   is_correct          BOOLEAN    NOT NULL
 *
 * Architectural Significance:
 *   - Represents an immutable item-level response recorded during an assessment attempt.
 *   - Preserves the exact choice the learner selected (`selected_option_id`) for each question (`question_id`).
 *   - The denormalized `is_correct` boolean snapshot ensures that historical audit trails remain valid
 *     even if question options or correct answers are updated in the curriculum catalog in the future.
 *   - Provides the granular telemetry that the Skill-Gap Engine (Module E) inspects: since each
 *     question is tagged to a `Skill`, incorrect responses directly inform which skills require remediation.
 *
 * Relationships:
 *   - ManyToOne -> AssessmentAttempt  (owning side; holds attempt_id FK).
 *   - ManyToOne -> AssessmentQuestion (owning side; holds question_id FK).
 *   - ManyToOne -> AssessmentOption   (owning side; holds selected_option_id FK).
 */
@Entity
@Table(name = "assessment_responses")
public class AssessmentResponse {

    // -------------------------------------------------------------------------
    // Primary Key — BIGSERIAL in PostgreSQL maps to Long with GenerationType.IDENTITY
    // -------------------------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------------------------------------------------------------------------
    // attempt_id: BIGINT NOT NULL REFERENCES assessment_attempts(id) ON DELETE CASCADE
    // Links this response to the specific attempt / submission session.
    // -------------------------------------------------------------------------
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private AssessmentAttempt attempt;

    // -------------------------------------------------------------------------
    // question_id: BIGINT NOT NULL REFERENCES assessment_questions(id) ON DELETE CASCADE
    // The question being answered.
    // -------------------------------------------------------------------------
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private AssessmentQuestion question;

    // -------------------------------------------------------------------------
    // selected_option_id: BIGINT NOT NULL REFERENCES assessment_options(id) ON DELETE CASCADE
    // The specific answer option selected by the learner.
    // -------------------------------------------------------------------------
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id", nullable = false)
    private AssessmentOption selectedOption;

    // -------------------------------------------------------------------------
    // is_correct: BOOLEAN NOT NULL
    // Snapshot of answer correctness at the time of submission.
    // -------------------------------------------------------------------------
    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    protected AssessmentResponse() {
        // Required by JPA spec
    }

    public AssessmentResponse(AssessmentAttempt attempt, AssessmentQuestion question,
                              AssessmentOption selectedOption, boolean isCorrect) {
        this.attempt = attempt;
        this.question = question;
        this.selectedOption = selectedOption;
        this.isCorrect = isCorrect;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------
    public Long getId() {
        return id;
    }

    public AssessmentAttempt getAttempt() {
        return attempt;
    }

    public void setAttempt(AssessmentAttempt attempt) {
        this.attempt = attempt;
    }

    public AssessmentQuestion getQuestion() {
        return question;
    }

    public void setQuestion(AssessmentQuestion question) {
        this.question = question;
    }

    public AssessmentOption getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(AssessmentOption selectedOption) {
        this.selectedOption = selectedOption;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        this.isCorrect = correct;
    }

    /** Alias setter for frameworks and DTO mappers */
    public void setIsCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    @Override
    public String toString() {
        return "AssessmentResponse{id=" + id
                + ", attemptId=" + (attempt != null ? attempt.getId() : null)
                + ", questionId=" + (question != null ? question.getId() : null)
                + ", selectedOptionId=" + (selectedOption != null ? selectedOption.getId() : null)
                + ", isCorrect=" + isCorrect + "}";
    }
}
