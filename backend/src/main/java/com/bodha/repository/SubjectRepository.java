package com.bodha.repository;

import com.bodha.model.Subject;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, String> {

    @Override
    @EntityGraph(attributePaths = {"domain"})
    List<Subject> findAll();

    @Override
    @EntityGraph(attributePaths = {"domain"})
    Optional<Subject> findById(String id);

    @EntityGraph(attributePaths = {"domain"})
    List<Subject> findByDomainId(String domainId);

    @EntityGraph(attributePaths = {"domain"})
    List<Subject> findByIsPopularTrue();

    @EntityGraph(attributePaths = {"domain"})
    List<Subject> findByCreatedByUserId(Long userId);
}
