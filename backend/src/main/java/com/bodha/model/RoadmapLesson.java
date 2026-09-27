package com.bodha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * JPA entity mapped to the `roadmap_lessons` table.
 *
 * Schema definition (exact):
 *   id            BIGSERIAL     PRIMARY KEY
 *   module_id     BIGINT        NOT NULL REFERENCES roadmap_modules(id) ON DELETE CASCADE
 *   title         VARCHAR(200)  NOT NULL
 *   lesson_type   VARCHAR(50)   NOT NULL CHECK (lesson_type IN ('Theory + Code', 'Hands-on Lab', 'Best Practice', 'Quiz', 'Exercise', 'Capstone'))
 *   content_body  TEXT          (nullable)
 *   order_index   INTEGER       NOT NULL
 *   created_at    TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
 *
 * Architectural Significance:
 *   - The atomic learning unit within a personalized RoadmapModule.
 *   - `lesson_type` differentiates pedagogical delivery methods (theory, labs, best practices, quizzes).
 *   - `content_body` stores markdown/code snippet curriculum text for bite-sized learning.
 *   - Each lesson's completion is tracked individually in `lesson_progress` (Module F).
 *
 * Relationships:
 *   - ManyToOne -> RoadmapModule (owning side; holds module_id FK).
 *   - OneToMany -> LessonProgress (will be mapped once LessonProgress is created).
 */
@Entity
@Table(name = "roadmap_lessons")
public class RoadmapLesson {

    /**
     * Pedagogical delivery type for this lesson.
     * Schema: CHECK (lesson_type IN ('Theory + Code', 'Hands-on Lab', 'Best Practice', 'Quiz', 'Exercise', 'Capstone'))
     */
    public enum LessonType {
        THEORY_AND_CODE("Theory + Code"),
        HANDS_ON_LAB("Hands-on Lab"),
        BEST_PRACTICE("Best Practice"),
        QUIZ("Quiz"),
        EXERCISE("Exercise"),
        CAPSTONE("Capstone");

        private final String dbValue;

        LessonType(String dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return dbValue;
        }

        public static LessonType fromDbValue(String value) {
            for (LessonType type : values()) {
                if (type.dbValue.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown lesson_type: '" + value + "'");
        }
    }

    /**
     * JPA AttributeConverter to translate between LessonType enum and PostgreSQL VARCHAR string.
     */
    @Converter(autoApply = true)
    public static class LessonTypeConverter implements AttributeConverter<LessonType, String> {
        @Override
        public String convertToDatabaseColumn(LessonType attribute) {
            return attribute != null ? attribute.getDbValue() : null;
        }

        @Override
        public LessonType convertToEntityAttribute(String dbData) {
            return dbData != null ? LessonType.fromDbValue(dbData) : null;
        }
    }

    // -------------------------------------------------------------------------
    // Primary Key — BIGSERIAL in PostgreSQL maps to Long with GenerationType.IDENTITY
    // -------------------------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------------------------------------------------------------------------
    // module_id: BIGINT NOT NULL REFERENCES roadmap_modules(id) ON DELETE CASCADE
    // Lessons belong to a specific roadmap module.
    // -------------------------------------------------------------------------
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private RoadmapModule module;

    // -------------------------------------------------------------------------
    // title: VARCHAR(200) NOT NULL
    // e.g. "Spring Beans & ApplicationContext"
    // -------------------------------------------------------------------------
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    // -------------------------------------------------------------------------
    // lesson_type: VARCHAR(50) NOT NULL
    // CHECK (lesson_type IN ('Theory + Code', 'Hands-on Lab', 'Best Practice', 'Quiz', 'Exercise', 'Capstone'))
    // -------------------------------------------------------------------------
    @NotNull
    @Convert(converter = LessonTypeConverter.class)
    @Column(name = "lesson_type", nullable = false, length = 50)
    private LessonType lessonType;

    // -------------------------------------------------------------------------
    // content_body: TEXT (nullable)
    // Bite-sized conceptual explanation or runnable code example.
    // -------------------------------------------------------------------------
    @Column(name = "content_body", columnDefinition = "TEXT")
    private String contentBody;

    // -------------------------------------------------------------------------
    // order_index: INTEGER NOT NULL
    // Sequence order of this lesson inside the parent module.
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
    protected RoadmapLesson() {
        // Required by JPA spec
    }

    public RoadmapLesson(RoadmapModule module, String title, LessonType lessonType, int orderIndex) {
        this.module = module;
        this.title = title;
        this.lessonType = lessonType;
        this.orderIndex = orderIndex;
    }

    public RoadmapLesson(RoadmapModule module, String title, LessonType lessonType,
                         String contentBody, int orderIndex) {
        this.module = module;
        this.title = title;
        this.lessonType = lessonType;
        this.contentBody = contentBody;
        this.orderIndex = orderIndex;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------
    public Long getId() {
        return id;
    }

    public RoadmapModule getModule() {
        return module;
    }

    public void setModule(RoadmapModule module) {
        this.module = module;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LessonType getLessonType() {
        return lessonType;
    }

    public void setLessonType(LessonType lessonType) {
        this.lessonType = lessonType;
    }

    /** Convenience string setter for DTO mappers and JSON deserializers */
    public void setLessonType(String lessonType) {
        this.lessonType = lessonType != null ? LessonType.fromDbValue(lessonType) : null;
    }

    public String getContentBody() {
        return contentBody;
    }

    public void setContentBody(String contentBody) {
        this.contentBody = contentBody;
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
        return "RoadmapLesson{id=" + id
                + ", moduleId=" + (module != null ? module.getId() : null)
                + ", title='" + title + '\''
                + ", type=" + (lessonType != null ? lessonType.getDbValue() : null)
                + ", orderIndex=" + orderIndex + "}";
    }
}
