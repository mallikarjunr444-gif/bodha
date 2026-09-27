package com.bodha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity mapped to the `domains` table.
 *
 * Schema definition (exact):
 *   id           VARCHAR(50)   PRIMARY KEY  -- human-readable slug, manually assigned
 *   name         VARCHAR(100)  NOT NULL
 *   description  TEXT          (nullable)
 *   created_at   TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
 *
 * IMPORTANT — No @GeneratedValue here:
 *   Unlike `users` and `learner_profiles`, the `domains` primary key is a
 *   VARCHAR(50) slug (e.g. 'programming', 'mathematics').
 *   It is assigned explicitly in code/seed data — NOT auto-incremented by the DB.
 *
 * Relationships:
 *   - OneToMany -> Subject (domain_id FK lives in the subjects table)
 */
@Entity
@Table(name = "domains")
public class Domain {

    // -------------------------------------------------------------------------
    // Primary Key — VARCHAR(50) slug. No @GeneratedValue: caller assigns it.
    // e.g. "programming", "mathematics", "languages", "business"
    // -------------------------------------------------------------------------
    @Id
    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String id;

    // -------------------------------------------------------------------------
    // name: VARCHAR(100) NOT NULL
    // -------------------------------------------------------------------------
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    // -------------------------------------------------------------------------
    // description: TEXT (nullable)
    // columnDefinition = "TEXT" maps to PostgreSQL TEXT instead of VARCHAR.
    // -------------------------------------------------------------------------
    @Column(columnDefinition = "TEXT")
    private String description;

    // -------------------------------------------------------------------------
    // created_at: TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    // @CreationTimestamp: Hibernate sets on INSERT, never touches again.
    // -------------------------------------------------------------------------
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // -------------------------------------------------------------------------
    // Relationship: Domain -> Subject (One-to-Many, bidirectional)
    // The FK (domain_id) lives in the subjects table, so this is the inverse
    // side, declared with mappedBy = "domain".
    //
    // cascade = ALL: saving a Domain auto-saves its Subjects (useful for seeding).
    // fetch = LAZY: subjects list is NOT loaded unless explicitly accessed.
    // orphanRemoval = true: removing a Subject from this list deletes it from DB.
    // -------------------------------------------------------------------------
    @OneToMany(mappedBy = "domain", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Subject> subjects = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    protected Domain() {
        // Required by JPA spec
    }

    public Domain(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public OffsetDateTime getCreatedAt() { return createdAt; }

    public List<Subject> getSubjects() { return subjects; }

    /** Convenience helper: maintains both sides of the bidirectional link. */
    public void addSubject(Subject subject) {
        subjects.add(subject);
        subject.setDomain(this);
    }

    @Override
    public String toString() {
        return "Domain{id='" + id + "', name='" + name + "'}";
    }
}
