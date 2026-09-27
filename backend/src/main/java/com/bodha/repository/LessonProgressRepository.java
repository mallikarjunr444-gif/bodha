package com.bodha.repository;

import com.bodha.model.LessonProgress;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    @EntityGraph(attributePaths = {"user", "lesson", "lesson.module", "lesson.module.roadmap"})
    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);

    @EntityGraph(attributePaths = {"user", "lesson", "lesson.module", "lesson.module.roadmap"})
    List<LessonProgress> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "lesson", "lesson.module", "lesson.module.roadmap"})
    List<LessonProgress> findByUserIdAndLessonIdIn(Long userId, List<Long> lessonIds);

    boolean existsByUserIdAndLessonIdAndIsCompletedTrue(Long userId, Long lessonId);

    long countByUserIdAndIsCompletedTrue(Long userId);
}
