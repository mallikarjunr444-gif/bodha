package com.bodha.repository;

import com.bodha.model.UserActivityLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<UserActivityLog> findByUserIdAndActivityDate(Long userId, LocalDate activityDate);

    @EntityGraph(attributePaths = {"user"})
    List<UserActivityLog> findByUserIdOrderByActivityDateDesc(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<UserActivityLog> findByUserIdAndActivityDateBetweenOrderByActivityDateAsc(Long userId, LocalDate startDate, LocalDate endDate);
}
