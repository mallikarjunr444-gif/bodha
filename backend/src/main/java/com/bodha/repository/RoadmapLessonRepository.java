package com.bodha.repository;

import com.bodha.model.RoadmapLesson;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoadmapLessonRepository extends JpaRepository<RoadmapLesson, Long> {

    @Override
    @EntityGraph(attributePaths = {"module", "module.roadmap", "module.roadmap.user", "module.roadmap.learnerGoal"})
    Optional<RoadmapLesson> findById(Long id);

    @EntityGraph(attributePaths = {"module", "module.roadmap"})
    List<RoadmapLesson> findByModuleIdOrderByOrderIndexAsc(Long moduleId);

    @EntityGraph(attributePaths = {"module", "module.roadmap"})
    List<RoadmapLesson> findByModuleIdInOrderByOrderIndexAsc(List<Long> moduleIds);
}
