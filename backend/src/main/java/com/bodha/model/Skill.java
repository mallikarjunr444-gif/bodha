package com.bodha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * JPA entity mapped to the `skills` table.
 *
 * Schema definition (exact):
 *   id          BIGSERIAL     PRIMARY KEY
 *   subject_id  VARCHAR(80)   NOT NULL REFERENCES subjects(id) ON DELETE CASCADE
 *   name        VARCHAR(150)  NOT NULL
 *   category    VARCHAR(50)   NOT NULL DEFAULT 'core' CHECK ('prerequisite','core','advanced')
 *   description TEXT          (nullable)
 *   created_at  TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
 *
 * Skills are the granular competency units that power BODHA's diagnostic engine.
 * Each assessment question is tagged to a skill (assessment_questions.skill_id),
 * and each learner_skill_gaps row evaluates one skill as MASTERED or GAP.
 *
 * Relationships:
 *   - ManyToOne -> Subject   (this table holds subject_id FK)
 */
@Entity
@Table(name = "skills")
public class Skill {

    // -------------------------------------------------------------------------
    // Primary Key — BIGSERIAL: auto-incremented by PostgreSQL.
    // GenerationType.IDENTITY delegates sequence management to the DB.
    // -------------------------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------------------------------------------------------------------------
    // subject_id: VARCHAR(80) NOT NULL REFERENCES subjects(id) ON DELETE CASCADE
    //
    // @ManyToOne: many skills belong to one subject.
    // @JoinColumn: this table holds the FK column "subject_id".
    // ON DELETE CASCADE is enforced by schema.sql at the DB level.
    // fetch = LAZY: subject is not auto-loaded with every skill query.
    // -------------------------------------------------------------------------
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // -------------------------------------------------------------------------
    // name: VARCHAR(150) NOT NULL
    // e.g. "Spring Core & IoC", "Matrix Decomposition", "Verb Conjugations"
    // -------------------------------------------------------------------------
    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;

    // -------------------------------------------------------------------------
    // category: VARCHAR(50) NOT NULL DEFAULT 'core'
    //   CHECK (category IN ('prerequisite', 'core', 'advanced'))
    //
    // SkillCategoryConverter (autoApply = true) handles the mapping:
    //   Java: PREREQUISITE  <-->  PostgreSQL: 'prerequisite'
    //   Java: CORE          <-->  PostgreSQL: 'core'
    //   Java: ADVANCED      <-->  PostgreSQL: 'advanced'
    // Initialized to CORE to match schema DEFAULT 'core'.
    // -------------------------------------------------------------------------
    @NotNull
    @Column(nullable = false, length = 50)
    private SkillCategory category = SkillCategory.CORE;

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
    protected Skill() {
        // Required by JPA spec
    }

    public Skill(Subject subject, String name, SkillCategory category) {
        this.subject = subject;
        this.name = name;
        this.category = category;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------
    public Long getId() { return id; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public SkillCategory getCategory() { return category; }
    public void setCategory(SkillCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public OffsetDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "Skill{id=" + id + ", name='" + name + "', category=" + category + "}";
    }
}
