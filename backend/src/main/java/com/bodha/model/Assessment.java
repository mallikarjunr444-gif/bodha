package com.bodha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * JPA entity mapped to the `assessments` table.
 *
 * Schema definition (exact):
 *   id               BIGSERIAL     PRIMARY KEY
 *   subject_id       VARCHAR(80)   NOT NULL REFERENCES subjects(id) ON DELETE CASCADE
 *   title            VARCHAR(200)  NOT NULL
 *   assessment_type  VARCHAR(50)   NOT NULL CHECK (assessment_type IN ('DIAGNOSTIC', 'MODULE_QUIZ', 'CHECKPOINT', 'FINAL_CAPSTONE'))
 *   description      TEXT          (nullable)
 *   created_at       TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
 *
 * Architectural Note on Relationships:
 *   - Belongs to: Subject (Many-to-One via subject_id). Tests are defined at the subject/curriculum level.
 *   - Relationship to LearnerGoal: Based on database/schema.sql, the `assessments` table
 *     does NOT contain a direct foreign key to `learner_goals`. Instead, assessments are
 *     subject-wide evaluative tests. The connection to a specific learner's goal is realized
 *     when an assessment is taken, recorded in the `assessment_attempts` table
 *     (which holds both `assessment_id` and `learner_goal_id`).
 *   - Questions: Will be linked via OneToMany -> AssessmentQuestion (once created in Module D).
 *   - Attempts: Will be linked via OneToMany -> AssessmentAttempt (once created in Module D).
 */
@Entity
@Table(name = "assessments")
public class Assessment {

    // -------------------------------------------------------------------------
    // Primary Key — BIGSERIAL in PostgreSQL maps to Long with GenerationType.IDENTITY
    // -------------------------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------------------------------------------------------------------------
    // subject_id: VARCHAR(80) NOT NULL REFERENCES subjects(id) ON DELETE CASCADE
    // Many assessments belong to one subject curriculum.
    // -------------------------------------------------------------------------
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // -------------------------------------------------------------------------
    // title: VARCHAR(200) NOT NULL
    // e.g. "Full-Stack Java Baseline Diagnostic", "Concurrency & Threads Quiz"
    // -------------------------------------------------------------------------
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    // -------------------------------------------------------------------------
    // assessment_type: VARCHAR(50) NOT NULL
    // CHECK (assessment_type IN ('DIAGNOSTIC', 'MODULE_QUIZ', 'CHECKPOINT', 'FINAL_CAPSTONE'))
    // Stored as UPPERCASE string matching AssessmentType enum constants.
    // -------------------------------------------------------------------------
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_type", nullable = false, length = 50)
    private AssessmentType assessmentType;

    // -------------------------------------------------------------------------
    // description: TEXT (nullable)
    // -------------------------------------------------------------------------
    @Column(columnDefinition = "TEXT")
    private String description;

    // -------------------------------------------------------------------------
    // created_at: TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    // -------------------------------------------------------------------------
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    protected Assessment() {
        // Required by JPA spec
    }

    public Assessment(Subject subject, String title, AssessmentType assessmentType) {
        this.subject = subject;
        this.title = title;
        this.assessmentType = assessmentType;
    }

    public Assessment(Subject subject, String title, AssessmentType assessmentType, String description) {
        this.subject = subject;
        this.title = title;
        this.assessmentType = assessmentType;
        this.description = description;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------
    public Long getId() {
        return id;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public AssessmentType getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(AssessmentType assessmentType) {
        this.assessmentType = assessmentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Assessment{id=" + id + ", title='" + title + "', type=" + assessmentType + "}";
    }
}
