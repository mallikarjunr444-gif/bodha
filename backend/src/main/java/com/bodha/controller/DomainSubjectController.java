package com.bodha.controller;

import com.bodha.dto.DomainResponseDto;
import com.bodha.dto.SkillResponseDto;
import com.bodha.dto.SubjectResponseDto;
import com.bodha.model.Skill;
import com.bodha.model.Subject;
import com.bodha.service.DomainSubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Universal Domain & Subject Catalog operations (Module B).
 *
 * Endpoints:
 * - GET /api/domains
 * - GET /api/subjects
 * - GET /api/subjects?domainId={domainId}
 * - GET /api/subjects/popular
 * - GET /api/subjects/{subjectId}
 * - GET /api/subjects/{subjectId}/skills
 */
@RestController
@RequestMapping("/api")
public class DomainSubjectController {

    private final DomainSubjectService domainSubjectService;

    public DomainSubjectController(DomainSubjectService domainSubjectService) {
        this.domainSubjectService = domainSubjectService;
    }

    /**
     * GET /api/domains
     * Retrieves all broad knowledge domain categories.
     */
    @GetMapping("/domains")
    public ResponseEntity<List<DomainResponseDto>> getAllDomains() {
        List<DomainResponseDto> domains = domainSubjectService.getAllDomains().stream()
                .map(DomainResponseDto::fromEntity)
                .toList();
        return ResponseEntity.ok(domains);
    }

    /**
     * GET /api/subjects/popular
     * Retrieves all curated subjects flagged as popular.
     * Note: Placed before /{subjectId} to guarantee literal path routing precedence.
     */
    @GetMapping("/subjects/popular")
    public ResponseEntity<List<SubjectResponseDto>> getPopularSubjects() {
        List<Subject> popularSubjects = domainSubjectService.getPopularSubjects();
        List<SubjectResponseDto> response = popularSubjects.stream()
                .map(this::mapSubjectWithSkills)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/subjects
     * GET /api/subjects?domainId={domainId}
     * Retrieves all subjects, optionally filtered by domain slug.
     */
    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectResponseDto>> getSubjects(
            @RequestParam(required = false) String domainId) {
        List<Subject> subjects = (domainId != null && !domainId.isBlank())
                ? domainSubjectService.getSubjectsByDomain(domainId)
                : domainSubjectService.getAllSubjects();

        List<SubjectResponseDto> response = subjects.stream()
                .map(this::mapSubjectWithSkills)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/subjects/{subjectId}
     * Retrieves details for a single curriculum subject.
     */
    @GetMapping("/subjects/{subjectId}")
    public ResponseEntity<SubjectResponseDto> getSubjectById(@PathVariable String subjectId) {
        Subject subject = domainSubjectService.getSubjectById(subjectId);
        return ResponseEntity.ok(mapSubjectWithSkills(subject));
    }

    /**
     * GET /api/subjects/{subjectId}/skills
     * Retrieves all granular competency units associated with a subject.
     */
    @GetMapping("/subjects/{subjectId}/skills")
    public ResponseEntity<List<SkillResponseDto>> getSkillsBySubjectId(@PathVariable String subjectId) {
        List<Skill> skills = domainSubjectService.getSkillsBySubjectId(subjectId);
        List<SkillResponseDto> response = skills.stream()
                .map(SkillResponseDto::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Handles missing entities or invalid query arguments with a 404 response.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundOrBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    /**
     * Helper to map a Subject entity to a SubjectResponseDto including its covered skill names.
     */
    private SubjectResponseDto mapSubjectWithSkills(Subject subject) {
        List<String> skillNames = domainSubjectService.getSkillsBySubjectId(subject.getId())
                .stream()
                .map(Skill::getName)
                .toList();
        return SubjectResponseDto.fromEntity(subject, skillNames);
    }
}
