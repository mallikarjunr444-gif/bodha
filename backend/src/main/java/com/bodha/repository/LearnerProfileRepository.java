package com.bodha.repository;

import com.bodha.model.LearnerProfile;
import com.bodha.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LearnerProfileRepository extends JpaRepository<LearnerProfile, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<LearnerProfile> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    Optional<LearnerProfile> findByUser(User user);
}
