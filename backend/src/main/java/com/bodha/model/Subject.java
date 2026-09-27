package com.bodha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity mapped to the `subjects` table.
 *
 * Schema definition (exact):
 *   id                  VARCHAR(80)   PRIMARY KEY  -- slug, manually assigned
 *   domain_id           VARCHAR(50)   NOT NULL REFERENCES domains(id) ON DELETE RESTRICT
 *   title               VARCHAR(200)  NOT NULL
 *   tagline             VARCHAR(300)  (nullable)
 *   difficulty_level    VARCHAR(50)   NOT NULL CHECK ('Beginner','Intermediate','Advanced','All Levels')
 *   estimated_weeks     INTEGER       NOT NULL DEFAULT 6 CHECK (> 0)
 *   is_popular          BOOLEAN       NOT NULL DEFAULT false
 *   is_custom           BOOLEAN       NOT NULL DEFAULT false
 *   created_by_user_id  BIGINT        REFERENCES users(id) ON DELETE SET NULL (nullable)
 *   created_at          TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
 *
 * IMPORTANT — No @GeneratedValue here:
 *   id is a VARCHAR(80) slug (e.g. 'java-backend', 'linear-algebra', 'spanish').
 *   Assigned in code/seed data, not auto-incremented.
 *
 * Relationships:
 *   - ManyToOne -> Domain   (this table holds domain_id FK)
 *   - ManyToOne -> User     (optional created_by_user_id FK, NULL for system tracks)
 *   - OneToMany -> Skill    (skill.subject_id FK lives in skills table)
 */
@Entity
@Table(name = "subjects")
public class Subject {

    // -------------------------------------------------------------------------
    // Primary Key — VARCHAR(80) slug. No @GeneratedValue.
    // e.g. "java-backend", "linear-algebra", "spanish", "product-mgmt"
    // -------------------------------------------------------------------------
    @Id
    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String id;

    // -------------------------------------------------------------------------
    // domain_id: VARCHAR(50) NOT NULL REFERENCES domains(id) ON DELETE RESTRICT
    //
    // @ManyToOne: many subjects belong to one domain.
    // @JoinColumn: declares that THIS table holds the FK column "domain_id".
    // ON DELETE RESTRICT is enforced at the DB level by schema.sql.
    // fetch = LAZY: domain is not loaded unless accessed (avoids N+1 queries).
    // -------------------------------------------------------------------------
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", nullable = false)
    private Domain domain;

    // -------------------------------------------------------------------------
    // title: VARCHAR(200) NOT NULL
    // -------------------------------------------------------------------------
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    // -------------------------------------------------------------------------
    // tagline: VARCHAR(300) (nullable)
    // -------------------------------------------------------------------------
    @Size(max = 300)
    @Column(length = 300)
    private String tagline;

    // -------------------------------------------------------------------------
    // difficulty_level: VARCHAR(50) NOT NULL
    //   CHECK (difficulty_level IN ('Beginner', 'Intermediate', 'Advanced', 'All Levels'))
    //
    // DifficultyLevelConverter (autoApply = true) handles the mapping:
    //   Java: ALL_LEVELS  <-->  PostgreSQL: 'All Levels'
    // No explicit @Convert annotation needed here because autoApply = true.
    // -------------------------------------------------------------------------
    @NotNull
    @Column(name = "difficulty_level", nullable = false, length = 50)
    private DifficultyLevel difficultyLevel;

    // -------------------------------------------------------------------------
    // estimated_weeks: INTEGER NOT NULL DEFAULT 6 CHECK (> 0)
    // @Min(1) validates at the bean-validation layer (CHECK constraint is in DB).
    // -------------------------------------------------------------------------
    @Min(1)
    @Column(name = "estimated_weeks", nullable = false)
    private int estimatedWeeks = 6;

    // -------------------------------------------------------------------------
    // is_popular: BOOLEAN NOT NULL DEFAULT false
    // -------------------------------------------------------------------------
    @Column(name = "is_popular", nullable = false)
    private boolean isPopular = false;

    // -------------------------------------------------------------------------
    // is_custom: BOOLEAN NOT NULL DEFAULT false
    // -------------------------------------------------------------------------
    @Column(name = "is_custom", nullable = false)
    private boolean isCustom = false;

    // -------------------------------------------------------------------------
    // created_by_user_id: BIGINT REFERENCES users(id) ON DELETE SET NULL (nullable)
    //
    // NULL for all platform-curated tracks (the 6 seeded subjects).
    // Populated only when a learner creates a custom topic (is_custom = true).
    // @ManyToOne(optional = true): nullable FK, so the join column is nullable.
    // -------------------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "created_by_user_id", nullable = true)
    private User createdByUser;

    // -------------------------------------------------------------------------
    // created_at: TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    // -------------------------------------------------------------------------
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // -------------------------------------------------------------------------
    // Relationship: Subject -> Skill (One-to-Many, bidirectional)
    // The FK (subject_id) lives in the skills table — inverse side here.
    // cascade = ALL: saving a Subject auto-saves its Skills.
    // -------------------------------------------------------------------------
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Skill> skills = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    protected Subject() {
        // Required by JPA spec
    }

    public Subject(String id, Domain domain, String title, DifficultyLevel difficultyLevel) {
        this.id = id;
        this.domain = domain;
        this.title = title;
        this.difficultyLevel = difficultyLevel;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Domain getDomain() { return domain; }
    public void setDomain(Domain domain) { this.domain = domain; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }

    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public int getEstimatedWeeks() { return estimatedWeeks; }
    public void setEstimatedWeeks(int estimatedWeeks) { this.estimatedWeeks = estimatedWeeks; }

    public boolean isPopular() { return isPopular; }
    public void setPopular(boolean popular) { isPopular = popular; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    public User getCreatedByUser() { return createdByUser; }
    public void setCreatedByUser(User createdByUser) { this.createdByUser = createdByUser; }

    public OffsetDateTime getCreatedAt() { return createdAt; }

    public List<Skill> getSkills() { return skills; }

    /** Convenience helper: maintains both sides of the bidirectional link. */
    public void addSkill(Skill skill) {
        skills.add(skill);
        skill.setSubject(this);
    }

    @Override
    public String toString() {
        return "Subject{id='" + id + "', title='" + title + "', difficulty=" + difficultyLevel + "}";
    }
}
