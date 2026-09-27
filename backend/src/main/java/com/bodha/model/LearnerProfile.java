package com.bodha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * JPA entity mapped to the `learner_profiles` table.
 *
 * Schema definition (exact):
 *   id                   BIGSERIAL  PRIMARY KEY
 *   user_id              BIGINT     UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE
 *   bio                  TEXT
 *   avatar_url           VARCHAR(500)
 *   current_streak_days  INTEGER    NOT NULL DEFAULT 0 CHECK (>= 0)
 *   total_xp             INTEGER    NOT NULL DEFAULT 0 CHECK (>= 0)
 *   last_active_at       TIMESTAMPTZ (nullable)
 *   created_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
 *   updated_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
 *
 * Relationship:
 *   - OneToOne -> User (this table holds the FK: user_id)
 */
@Entity
@Table(name = "learner_profiles")
public class LearnerProfile {

    // -------------------------------------------------------------------------
    // Primary Key
    // -------------------------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------------------------------------------------------------------------
    // user_id: BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE
    //
    // @OneToOne: exactly one profile per user.
    // @JoinColumn: declares that THIS table (learner_profiles) holds the FK
    //              column "user_id" pointing to users(id).
    // unique = true: enforces the UNIQUE constraint on user_id, mirroring the
    //                schema's UNIQUE NOT NULL declaration.
    // nullable = false: mirrors NOT NULL.
    // ON DELETE CASCADE is handled at the DB level by schema.sql — Hibernate
    // does not need to replicate it since ddl-auto = none.
    // -------------------------------------------------------------------------
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // -------------------------------------------------------------------------
    // bio: TEXT (nullable)
    // columnDefinition = "TEXT" instructs Hibernate to use PostgreSQL TEXT type
    // rather than the default VARCHAR when generating DDL (irrelevant here since
    // ddl-auto = none, but documents the intent clearly).
    // -------------------------------------------------------------------------
    @Column(columnDefinition = "TEXT")
    private String bio;

    // -------------------------------------------------------------------------
    // avatar_url: VARCHAR(500) (nullable)
    // -------------------------------------------------------------------------
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    // -------------------------------------------------------------------------
    // current_streak_days: INTEGER NOT NULL DEFAULT 0 CHECK (>= 0)
    // @Min(0) validates at the Java/bean-validation layer.
    // Initialized to 0 to match the schema DEFAULT.
    // -------------------------------------------------------------------------
    @Min(0)
    @Column(name = "current_streak_days", nullable = false)
    private int currentStreakDays = 0;

    // -------------------------------------------------------------------------
    // total_xp: INTEGER NOT NULL DEFAULT 0 CHECK (>= 0)
    // -------------------------------------------------------------------------
    @Min(0)
    @Column(name = "total_xp", nullable = false)
    private int totalXp = 0;

    // -------------------------------------------------------------------------
    // last_active_at: TIMESTAMPTZ (nullable)
    // Null means the learner has never had an activity recorded yet.
    // -------------------------------------------------------------------------
    @Column(name = "last_active_at")
    private OffsetDateTime lastActiveAt;

    // -------------------------------------------------------------------------
    // created_at / updated_at: TIMESTAMPTZ NOT NULL
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
    protected LearnerProfile() {
        // Required by JPA spec
    }

    public LearnerProfile(User user) {
        this.user = user;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------
    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public int getCurrentStreakDays() { return currentStreakDays; }
    public void setCurrentStreakDays(int currentStreakDays) { this.currentStreakDays = currentStreakDays; }

    public int getTotalXp() { return totalXp; }
    public void setTotalXp(int totalXp) { this.totalXp = totalXp; }

    public OffsetDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(OffsetDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() {
        return "LearnerProfile{id=" + id + ", userId=" + (user != null ? user.getId() : null)
                + ", streak=" + currentStreakDays + ", xp=" + totalXp + "}";
    }
}
