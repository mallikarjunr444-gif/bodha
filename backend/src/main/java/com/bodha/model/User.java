package com.bodha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * JPA entity mapped to the `users` table.
 *
 * Schema definition (exact):
 *   id             BIGSERIAL PRIMARY KEY
 *   email          VARCHAR(255) UNIQUE NOT NULL
 *   password_hash  VARCHAR(255) NOT NULL
 *   full_name      VARCHAR(150) NOT NULL
 *   role           VARCHAR(30)  NOT NULL DEFAULT 'LEARNER'
 *   created_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
 *   updated_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
 *
 * Relationship:
 *   - OneToOne  -> LearnerProfile (mappedBy = "user")
 */
@Entity
@Table(name = "users")
public class User {

    // -------------------------------------------------------------------------
    // Primary Key
    // BIGSERIAL in PostgreSQL maps to Long with GenerationType.IDENTITY
    // -------------------------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------------------------------------------------------------------------
    // email: VARCHAR(255) UNIQUE NOT NULL
    // @Column(unique=true) enforces the UNIQUE constraint at the JPA layer.
    // @Email validates format; @NotBlank ensures it is not empty.
    // -------------------------------------------------------------------------
    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    // -------------------------------------------------------------------------
    // password_hash: VARCHAR(255) NOT NULL
    // Stores BCrypt hashed password — never the plain-text password.
    // Column name explicitly set because Java naming convention uses camelCase
    // but the DB column uses snake_case.
    // -------------------------------------------------------------------------
    @NotBlank
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    // -------------------------------------------------------------------------
    // full_name: VARCHAR(150) NOT NULL
    // -------------------------------------------------------------------------
    @NotBlank
    @Size(max = 150)
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    // -------------------------------------------------------------------------
    // role: VARCHAR(30) NOT NULL DEFAULT 'LEARNER'
    // @Enumerated(EnumType.STRING) stores "LEARNER"/"MENTOR"/"ADMIN" as a
    // VARCHAR string in PostgreSQL — NOT as numeric ordinals (0, 1, 2).
    // columnDefinition matches the schema default.
    // -------------------------------------------------------------------------
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, columnDefinition = "VARCHAR(30) DEFAULT 'LEARNER'")
    private UserRole role = UserRole.LEARNER;

    // -------------------------------------------------------------------------
    // created_at: TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    // @CreationTimestamp: Hibernate sets this automatically on INSERT.
    // updatable = false prevents Hibernate from touching it on UPDATE.
    // OffsetDateTime maps correctly to PostgreSQL TIMESTAMPTZ.
    // -------------------------------------------------------------------------
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // -------------------------------------------------------------------------
    // updated_at: TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    // @UpdateTimestamp: Hibernate refreshes this automatically on every UPDATE.
    // -------------------------------------------------------------------------
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // -------------------------------------------------------------------------
    // Relationship: User -> LearnerProfile (One-to-One, bidirectional)
    // "mappedBy = 'user'" means the FK lives in the learner_profiles table
    // (user_id column), NOT in the users table — so users stays clean.
    // cascade = ALL: saving/deleting User propagates to LearnerProfile.
    // fetch = LAZY: profile is NOT loaded unless explicitly accessed.
    // -------------------------------------------------------------------------
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private LearnerProfile learnerProfile;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    protected User() {
        // Required by JPA spec — do not use directly
    }

    public User(String email, String passwordHash, String fullName, UserRole role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------
    public Long getId() { return id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public LearnerProfile getLearnerProfile() { return learnerProfile; }
    public void setLearnerProfile(LearnerProfile learnerProfile) { this.learnerProfile = learnerProfile; }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', role=" + role + "}";
    }
}
