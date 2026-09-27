package com.bodha.service;

import com.bodha.model.Domain;
import com.bodha.model.Skill;
import com.bodha.model.Subject;
import com.bodha.repository.DomainRepository;
import com.bodha.repository.SkillRepository;
import com.bodha.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business service managing the universal Domain & Subject Catalog (Module B).
 *
 * Provides operations required by the frontend Choose Subject flow:
 * - Domain browsing and taxonomy discovery
 * - Subject filtering by domain and popularity
 * - Subject detail lookup and granular skill retrieval
 */
@Service
@Transactional(readOnly = true)
public class DomainSubjectService {

    private final DomainRepository domainRepository;
    private final SubjectRepository subjectRepository;
    private final SkillRepository skillRepository;

    public DomainSubjectService(DomainRepository domainRepository,
                                SubjectRepository subjectRepository,
                                SkillRepository skillRepository) {
        this.domainRepository = domainRepository;
        this.subjectRepository = subjectRepository;
        this.skillRepository = skillRepository;
    }

    /**
     * Retrieves all broad knowledge domains.
     *
     * @return list of all domains
     */
    public List<Domain> getAllDomains() {
        return domainRepository.findAll();
    }

    /**
     * Retrieves subjects associated with a given domain.
     * If domainId is null, blank, or "all", returns all subjects across domains.
     *
     * @param domainId domain slug identifier (e.g. 'programming', 'mathematics')
     * @return list of subjects in the domain
     * @throws IllegalArgumentException if the domainId does not exist
     */
    public List<Subject> getSubjectsByDomain(String domainId) {
        if (domainId == null || domainId.isBlank() || "all".equalsIgnoreCase(domainId.trim())) {
            return subjectRepository.findAll();
        }
        if (!domainRepository.existsById(domainId)) {
            throw new IllegalArgumentException("Domain not found with ID: " + domainId);
        }
        return subjectRepository.findByDomainId(domainId);
    }

    /**
     * Retrieves all subjects across all domains.
     *
     * @return list of all subjects
     */
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    /**
     * Retrieves a single subject curriculum by its unique slug ID.
     *
     * @param subjectId subject slug identifier (e.g. 'java-backend')
     * @return the Subject entity
     * @throws IllegalArgumentException if subject is not found
     */
    public Subject getSubjectById(String subjectId) {
        if (subjectId == null || subjectId.isBlank()) {
            throw new IllegalArgumentException("Subject ID must not be null or blank");
        }
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + subjectId));
    }

    /**
     * Retrieves all granular competencies/skills mapped to a specific subject.
     *
     * @param subjectId subject slug identifier (e.g. 'java-backend')
     * @return list of skills belonging to the subject
     * @throws IllegalArgumentException if the subject is not found
     */
    public List<Skill> getSkillsBySubjectId(String subjectId) {
        if (subjectId == null || subjectId.isBlank()) {
            throw new IllegalArgumentException("Subject ID must not be null or blank");
        }
        if (!subjectRepository.existsById(subjectId)) {
            throw new IllegalArgumentException("Subject not found with ID: " + subjectId);
        }
        return skillRepository.findBySubjectId(subjectId);
    }

    /**
     * Retrieves all curriculum subjects flagged as popular.
     *
     * @return list of popular subjects
     */
    public List<Subject> getPopularSubjects() {
        return subjectRepository.findByIsPopularTrue();
    }
}
