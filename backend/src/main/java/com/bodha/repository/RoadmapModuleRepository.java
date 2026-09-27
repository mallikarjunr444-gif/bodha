package com.bodha.repository;

import com.bodha.model.RoadmapModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoadmapModuleRepository extends JpaRepository<RoadmapModule, Long> {

    List<RoadmapModule> findByRoadmapIdOrderByOrderIndexAsc(Long roadmapId);

    Optional<RoadmapModule> findByRoadmapIdAndIsCurrentTrue(Long roadmapId);
}
