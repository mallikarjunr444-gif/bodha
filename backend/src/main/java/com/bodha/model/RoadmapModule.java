package com.bodha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * JPA entity mapped to the `roadmap_modules` table.
 *
 * Schema definition (exact):
 *   id                    BIGSERIAL     PRIMARY KEY
 *   roadmap_id            BIGINT        NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE
 *   title                 VARCHAR(200)  NOT NULL
 *   description           TEXT          (nullable)
 *   duration_label        VARCHAR(50)   NOT NULL  -- e.g. 'Week 1-2'
 *   order_index           INTEGER       NOT NULL
 *   status                VARCHAR(30)   NOT NULL DEFAULT 'UNLOCKED' CHECK (status IN ('UNLOCKED', 'LOCKED', 'COMPLETED'))
 *   is_current            BOOLEAN       NOT NULL DEFAULT false
 *   prerequisite_summary  VARCHAR(255)  (nullable)
 *   created_at            TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
 *
 * Architectural Significance:
 *   - Represents a sequenced curriculum milestone within a personalized Roadmap.
 *   - Directly drives the module timeline view on the Roadmap UI (/roadmap):
 *       * `status` (UNLOCKED / LOCKED / COMPLETED) governs learner progression gates.
 *       * `is_current` flags the active module the learner should currently be studying.
 *       * `duration_label` presents recommended pacing (e.g. "Week 1-2").
 *       * `prerequisite_summary` highlights dependencies satisfied before unlocking this milestone.
 *
 * Relationships:
 *   - ManyToOne -> Roadmap       (owning side; holds roadmap_id FK).
 *   - OneToMany -> RoadmapLesson (will be mapped once RoadmapLesson is created).
 */
@Entity
@Table(name = "roadmap_modules")
public class RoadmapModule {

    /**
     * Progression state of a roadmap milestone.
     * Schema: CHECK (status IN ('UNLOCKED', 'LOCKED', 'COMPLETED'))
     */
    public enum Status {
        UNLOCKED,
        LOCKED,
        COMPLETED
    }

    // -------------------------------------------------------------------------
    // Primary Key — BIGSERIAL in PostgreSQL maps to Long with GenerationType.IDENTITY
    // -------------------------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------------------------------------------------------------------------
    // roadmap_id: BIGINT NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE
    // Modules belong to a specific personalized roadmap.
    // -------------------------------------------------------------------------
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    // -------------------------------------------------------------------------
    // title: VARCHAR(200) NOT NULL
    // e.g. "Module 1: Advanced JPA & Relational Mapping"
    // -------------------------------------------------------------------------
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    // -------------------------------------------------------------------------
    // description: TEXT (nullable)
    // Detailed summary of what this module covers.
    // -------------------------------------------------------------------------
    @Column(columnDefinition = "TEXT")
    private String description;

    // -------------------------------------------------------------------------
    // duration_label: VARCHAR(50) NOT NULL
    // e.g. "Week 1-2", "Week 3", "Days 1-5"
    // -------------------------------------------------------------------------
    @NotBlank
    @Size(max = 50)
    @Column(name = "duration_label", nullable = false, length = 50)
    private String durationLabel;

    // -------------------------------------------------------------------------
    // order_index: INTEGER NOT NULL
    // Deterministic sequence order of this milestone inside the roadmap.
    // -------------------------------------------------------------------------
    @Min(1)
    @Column(name = "order_index", nullable = false)
    private int orderIndex = 1;

    // -------------------------------------------------------------------------
    // status: VARCHAR(30) NOT NULL DEFAULT 'UNLOCKED'
    // CHECK (status IN ('UNLOCKED', 'LOCKED', 'COMPLETED'))
    // -------------------------------------------------------------------------
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status = Status.UNLOCKED;

    // -------------------------------------------------------------------------
    // is_current: BOOLEAN NOT NULL DEFAULT false
    // Indicates if this is the learner's active milestone.
    // -------------------------------------------------------------------------
    @Column(name = "is_current", nullable = false)
    private boolean isCurrent = false;

    // -------------------------------------------------------------------------
    // prerequisite_summary: VARCHAR(255) (nullable)
    // Human-readable summary of prerequisites or baseline mastery needed.
    // -------------------------------------------------------------------------
    @Size(max = 255)
    @Column(name = "prerequisite_summary", length = 255)
    private String prerequisiteSummary;

    // -------------------------------------------------------------------------
    // created_at: TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    // -------------------------------------------------------------------------
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    protected RoadmapModule() {
        // Required by JPA spec
    }

    public RoadmapModule(Roadmap roadmap, String title, String durationLabel, int orderIndex) {
        this.roadmap = roadmap;
        this.title = title;
        this.durationLabel = durationLabel;
        this.orderIndex = orderIndex;
    }

    public RoadmapModule(Roadmap roadmap, String title, String description, String durationLabel,
                         int orderIndex, Status status, boolean isCurrent, String prerequisiteSummary) {
        this.roadmap = roadmap;
        this.title = title;
        this.description = description;
        this.durationLabel = durationLabel;
        this.orderIndex = orderIndex;
        this.status = status;
        this.isCurrent = isCurrent;
        this.prerequisiteSummary = prerequisiteSummary;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------
    public Long getId() {
        return id;
    }

    public Roadmap getRoadmap() {
        return roadmap;
    }

    public void setRoadmap(Roadmap roadmap) {
        this.roadmap = roadmap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDurationLabel() {
        return durationLabel;
    }

    public void setDurationLabel(String durationLabel) {
        this.durationLabel = durationLabel;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    /** Alias setter for DTO mappers and JSON deserializers */
    public void setIsCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public String getPrerequisiteSummary() {
        return prerequisiteSummary;
    }

    public void setPrerequisiteSummary(String prerequisiteSummary) {
        this.prerequisiteSummary = prerequisiteSummary;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "RoadmapModule{id=" + id
                + ", roadmapId=" + (roadmap != null ? roadmap.getId() : null)
                + ", title='" + title + '\''
                + ", orderIndex=" + orderIndex
                + ", status=" + status
                + ", isCurrent=" + isCurrent + "}";
    }
}
