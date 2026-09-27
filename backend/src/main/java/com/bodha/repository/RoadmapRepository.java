package com.bodha.repository;

import com.bodha.model.Roadmap;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    @EntityGraph(attributePaths = {"user", "learnerGoal", "learnerGoal.subject"})
    Optional<Roadmap> findByLearnerGoalId(Long learnerGoalId);

    @EntityGraph(attributePaths = {"user", "learnerGoal", "learnerGoal.subject"})
    List<Roadmap> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "learnerGoal", "learnerGoal.subject"})
    Optional<Roadmap> findByUserIdAndIsActiveTrue(Long userId);

    @Override
    @EntityGraph(attributePaths = {"user", "learnerGoal", "learnerGoal.subject"})
    Optional<Roadmap> findById(Long id);
}
