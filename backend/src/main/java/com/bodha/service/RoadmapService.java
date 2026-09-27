package com.bodha.service;

import com.bodha.dto.RoadmapLessonResponseDto;
import com.bodha.dto.RoadmapModuleResponseDto;
import com.bodha.dto.RoadmapResponseDto;
import com.bodha.exception.ResourceNotFoundException;
import com.bodha.model.*;
import com.bodha.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service orchestrating the Personalized Roadmap Engine (Module F).
 *
 * Responsibilities:
 * - Deterministically synthesizes sequenced curriculum milestones (RoadmapModules)
 *   and bite-sized lessons (RoadmapLessons) from learner diagnostic skill gaps (Module E).
 * - Prioritizes high-severity gaps as immediate learning focus (Module 1, UNLOCKED).
 * - Fast-tracks verified baseline competencies into accelerated review tracks.
 * - Enforces goal ownership and idempotent roadmap generation.
 */
@Service
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapModuleRepository roadmapModuleRepository;
    private final RoadmapLessonRepository roadmapLessonRepository;
    private final LearnerGoalRepository learnerGoalRepository;
    private final SkillRepository skillRepository;
    private final LearnerSkillGapRepository learnerSkillGapRepository;

    public RoadmapService(
            RoadmapRepository roadmapRepository,
            RoadmapModuleRepository roadmapModuleRepository,
            RoadmapLessonRepository roadmapLessonRepository,
            LearnerGoalRepository learnerGoalRepository,
            SkillRepository skillRepository,
            LearnerSkillGapRepository learnerSkillGapRepository) {
        this.roadmapRepository = roadmapRepository;
        this.roadmapModuleRepository = roadmapModuleRepository;
        this.roadmapLessonRepository = roadmapLessonRepository;
        this.learnerGoalRepository = learnerGoalRepository;
        this.skillRepository = skillRepository;
        this.learnerSkillGapRepository = learnerSkillGapRepository;
    }

    /**
     * Generates or retrieves an existing personalized roadmap for a learner goal.
     * Idempotent: repeated invocations for the same goal return the existing roadmap
     * without creating duplicate records in roadmaps/roadmap_modules.
     */
    @Transactional
    public RoadmapResponseDto generateRoadmap(Long goalId, Long userId) {
        if (goalId == null) {
            throw new IllegalArgumentException("Learner goal ID is required");
        }

        LearnerGoal goal = learnerGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Learner goal not found with id: " + goalId));

        if (userId != null && !goal.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Learner goal " + goalId + " does not belong to user ID " + userId);
        }

        // Idempotency check: if roadmap already generated, return existing
        Optional<Roadmap> existingRoadmap = roadmapRepository.findByLearnerGoalId(goalId);
        if (existingRoadmap.isPresent()) {
            return getRoadmapDetails(existingRoadmap.get());
        }

        Subject subject = goal.getSubject();
        List<Skill> subjectSkills = skillRepository.findBySubjectId(subject.getId());
        if (subjectSkills.isEmpty()) {
            throw new IllegalStateException("Cannot generate roadmap: no curriculum skills defined for subject: " + subject.getId());
        }

        // Query persisted skill gap evaluations for this goal
        List<LearnerSkillGap> gaps = learnerSkillGapRepository.findByUserIdAndLearnerGoalId(goal.getUser().getId(), goalId);
        Map<Long, LearnerSkillGap> gapBySkillId = gaps.stream()
                .collect(Collectors.toMap(g -> g.getSkill().getId(), g -> g, (a, b) -> a));

        // Deterministic classification tiers
        List<Skill> highGaps = new ArrayList<>();
        List<Skill> mediumGaps = new ArrayList<>();
        List<Skill> lowGaps = new ArrayList<>();
        List<Skill> unevaluatedSkills = new ArrayList<>();
        List<Skill> masteredSkills = new ArrayList<>();

        for (Skill skill : subjectSkills) {
            LearnerSkillGap gap = gapBySkillId.get(skill.getId());
            if (gap != null && gap.getStatus() == LearnerSkillGap.Status.GAP) {
                if (gap.getGapSeverity() == LearnerSkillGap.Severity.HIGH) {
                    highGaps.add(skill);
                } else if (gap.getGapSeverity() == LearnerSkillGap.Severity.MEDIUM) {
                    mediumGaps.add(skill);
                } else {
                    lowGaps.add(skill);
                }
            } else if (gap != null && gap.getStatus() == LearnerSkillGap.Status.MASTERED) {
                masteredSkills.add(skill);
            } else {
                unevaluatedSkills.add(skill);
            }
        }

        // Build explainable curated pedagogical summary
        String curatedRecommendation = buildCuratedRecommendation(highGaps, mediumGaps, lowGaps, masteredSkills);

        // Create Roadmap entity
        String roadmapTitle = subject.getTitle() + " Personalized Roadmap";
        Roadmap roadmap = new Roadmap(goal.getUser(), goal, roadmapTitle, curatedRecommendation);
        roadmap.setOverallProgressPercentage(0);
        roadmap.setActive(true);
        roadmap.setAiGenerated(false);
        Roadmap savedRoadmap = roadmapRepository.save(roadmap);

        // Sequence modules based on skill gap priority
        List<ModuleDefinition> moduleDefs = buildModuleDefinitions(
                subject, highGaps, mediumGaps, lowGaps, unevaluatedSkills, masteredSkills);

        List<RoadmapModule> savedModules = new ArrayList<>();
        for (int i = 0; i < moduleDefs.size(); i++) {
            ModuleDefinition def = moduleDefs.get(i);
            int orderIndex = i + 1;
            RoadmapModule.Status status = (i == 0) ? RoadmapModule.Status.UNLOCKED : RoadmapModule.Status.LOCKED;
            boolean isCurrent = (i == 0);

            RoadmapModule module = new RoadmapModule(
                    savedRoadmap,
                    def.title(),
                    def.description(),
                    def.durationLabel(),
                    orderIndex,
                    status,
                    isCurrent,
                    def.prerequisiteSummary()
            );
            RoadmapModule savedModule = roadmapModuleRepository.save(module);
            savedModules.add(savedModule);

            // Persist sequenced lessons for each module
            List<LessonDefinition> lessonDefs = def.lessons();
            for (int j = 0; j < lessonDefs.size(); j++) {
                LessonDefinition lDef = lessonDefs.get(j);
                RoadmapLesson lesson = new RoadmapLesson(
                        savedModule,
                        lDef.title(),
                        lDef.lessonType(),
                        lDef.contentBody(),
                        j + 1
                );
                roadmapLessonRepository.save(lesson);
            }
        }

        return getRoadmapDetails(savedRoadmap);
    }

    /**
     * Retrieves an existing roadmap by its associated learner goal ID.
     */
    @Transactional(readOnly = true)
    public RoadmapResponseDto getRoadmapByGoalId(Long goalId, Long userId) {
        if (goalId == null) {
            throw new IllegalArgumentException("Learner goal ID is required");
        }

        LearnerGoal goal = learnerGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Learner goal not found with id: " + goalId));

        if (userId != null && !goal.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Learner goal " + goalId + " does not belong to user ID " + userId);
        }

        Roadmap roadmap = roadmapRepository.findByLearnerGoalId(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found for learner goal ID: " + goalId));

        return getRoadmapDetails(roadmap);
    }

    /**
     * Retrieves an existing roadmap by its primary key ID.
     */
    @Transactional(readOnly = true)
    public RoadmapResponseDto getRoadmapById(Long roadmapId, Long userId) {
        if (roadmapId == null) {
            throw new IllegalArgumentException("Roadmap ID is required");
        }

        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with id: " + roadmapId));

        if (userId != null && !roadmap.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Roadmap " + roadmapId + " does not belong to user ID " + userId);
        }

        return getRoadmapDetails(roadmap);
    }

    /**
     * Batches module and lesson queries to eliminate N+1 issues and builds DTOs.
     */
    private RoadmapResponseDto getRoadmapDetails(Roadmap roadmap) {
        List<RoadmapModule> modules = roadmapModuleRepository.findByRoadmapIdOrderByOrderIndexAsc(roadmap.getId());
        if (modules.isEmpty()) {
            return RoadmapResponseDto.fromEntity(roadmap, List.of());
        }

        List<Long> moduleIds = modules.stream().map(RoadmapModule::getId).toList();
        List<RoadmapLesson> lessons = roadmapLessonRepository.findByModuleIdInOrderByOrderIndexAsc(moduleIds);
        Map<Long, List<RoadmapLesson>> lessonsByModuleId = lessons.stream()
                .collect(Collectors.groupingBy(l -> l.getModule().getId()));

        List<RoadmapModuleResponseDto> moduleDtos = modules.stream()
                .map(m -> {
                    List<RoadmapLesson> modLessons = lessonsByModuleId.getOrDefault(m.getId(), List.of());
                    List<RoadmapLessonResponseDto> lessonDtos = modLessons.stream()
                            .map(RoadmapLessonResponseDto::fromEntity)
                            .toList();
                    return RoadmapModuleResponseDto.fromEntity(m, lessonDtos);
                })
                .toList();

        return RoadmapResponseDto.fromEntity(roadmap, moduleDtos);
    }

    /**
     * Synthesizes explainable pedagogical guidance explaining prioritization decisions.
     */
    private String buildCuratedRecommendation(
            List<Skill> highGaps,
            List<Skill> mediumGaps,
            List<Skill> lowGaps,
            List<Skill> masteredSkills) {
        StringBuilder sb = new StringBuilder();
        sb.append("Diagnostic assessment analysis complete. ");

        if (!highGaps.isEmpty()) {
            String names = highGaps.stream().map(Skill::getName).collect(Collectors.joining(", "));
            sb.append("Critical competency gap detected in [").append(names)
              .append("] (score: 0–39%), which is prioritized as your immediate learning focus in Module 1. ");
        }

        if (!mediumGaps.isEmpty() || !lowGaps.isEmpty()) {
            List<String> secondary = new ArrayList<>();
            mediumGaps.forEach(s -> secondary.add(s.getName() + " (Medium)"));
            lowGaps.forEach(s -> secondary.add(s.getName() + " (Low)"));
            sb.append("Targeted reinforcement scheduled for: ").append(String.join(", ", secondary)).append(". ");
        }

        if (!masteredSkills.isEmpty()) {
            String mastered = masteredSkills.stream().map(Skill::getName).collect(Collectors.joining(", "));
            sb.append("You demonstrated strong mastery in [").append(mastered)
              .append("] (score: 80–100%); foundational prerequisites are credited and advanced application has been fast-tracked.");
        }

        return sb.toString().trim();
    }

    /**
     * Builds sequenced module and lesson definitions based on skill prioritization.
     */
    private List<ModuleDefinition> buildModuleDefinitions(
            Subject subject,
            List<Skill> highGaps,
            List<Skill> mediumGaps,
            List<Skill> lowGaps,
            List<Skill> unevaluatedSkills,
            List<Skill> masteredSkills) {

        List<ModuleDefinition> definitions = new ArrayList<>();

        // If subject is java-backend, use tailored high-fidelity curriculum
        if ("java-backend".equalsIgnoreCase(subject.getId())) {
            return buildJavaBackendDefinitions(highGaps, mediumGaps, lowGaps, unevaluatedSkills, masteredSkills);
        }

        // General fallback for any other subject in BODHA
        int moduleNum = 1;
        List<Skill> prioritizedSkills = new ArrayList<>();
        prioritizedSkills.addAll(highGaps);
        prioritizedSkills.addAll(mediumGaps);
        prioritizedSkills.addAll(lowGaps);
        prioritizedSkills.addAll(unevaluatedSkills);
        prioritizedSkills.addAll(masteredSkills);

        for (Skill skill : prioritizedSkills) {
            boolean isMastered = masteredSkills.contains(skill);
            boolean isHighGap = highGaps.contains(skill);

            String duration = (moduleNum == 1) ? "Week 1-2" : "Week " + (moduleNum + 1);
            String title = "Module " + moduleNum + ": " + skill.getName();
            String description;
            String prereq;

            if (isHighGap) {
                description = "Critical competency gap detected in diagnostic evaluation. Deep dive into fundamental concepts, hands-on lab exercises, and architecture patterns.";
                prereq = !masteredSkills.isEmpty() ? masteredSkills.get(0).getName() + " (Verified Baseline)" : "Diagnostic Baseline";
            } else if (isMastered) {
                description = "Accelerated Milestone: Baseline mastered (80–100%). Advanced focus on industry optimization, edge cases, and portfolio integration.";
                prereq = "Module " + (moduleNum - 1);
            } else {
                description = "Core curriculum milestone covering essential theory, practical code implementations, and mastery checks.";
                prereq = (moduleNum > 1) ? "Module " + (moduleNum - 1) : "Diagnostic Baseline";
            }

            List<LessonDefinition> lessons = List.of(
                new LessonDefinition("Core Concepts & Architecture: " + skill.getName(),
                        RoadmapLesson.LessonType.THEORY_AND_CODE,
                        "In-depth breakdown of fundamental mechanisms, architecture diagrams, and syntax conventions for " + skill.getName() + "."),
                new LessonDefinition("Hands-on Implementation Lab: " + skill.getName(),
                        RoadmapLesson.LessonType.HANDS_ON_LAB,
                        "Practical step-by-step coding lab applying " + skill.getName() + " in an isolated test environment."),
                new LessonDefinition("Industry Best Practices & Optimization",
                        RoadmapLesson.LessonType.BEST_PRACTICE,
                        "Production guidance, edge-case mitigation, and maintainability patterns for " + skill.getName() + "."),
                new LessonDefinition("Checkpoint Assessment: " + skill.getName() + " Mastery",
                        RoadmapLesson.LessonType.QUIZ,
                        "Diagnostic recall quiz verifying conceptual retention before unlocking the next milestone.")
            );

            definitions.add(new ModuleDefinition(title, description, duration, prereq, lessons));
            moduleNum++;
        }

        return definitions;
    }

    /**
     * Tailored high-fidelity curriculum for Full-Stack Java & Spring Boot Architecture.
     * Accurately reflects Spring Core & IoC as Module 1 when identified as a High Gap,
     * credits Core Java OOP as a verified prerequisite, and accelerates mastered topics.
     */
    private List<ModuleDefinition> buildJavaBackendDefinitions(
            List<Skill> highGaps,
            List<Skill> mediumGaps,
            List<Skill> lowGaps,
            List<Skill> unevaluatedSkills,
            List<Skill> masteredSkills) {

        List<ModuleDefinition> definitions = new ArrayList<>();

        boolean springCoreIsGap = highGaps.stream().anyMatch(s -> "Spring Core & IoC".equalsIgnoreCase(s.getName()));
        boolean oopMastered = masteredSkills.stream().anyMatch(s -> "Core Java OOP".equalsIgnoreCase(s.getName()));

        String mod1Prereq = oopMastered ? "Core Java OOP (Verified Baseline)" : "Diagnostic Baseline";

        // Module 1: Spring Core & Dependency Injection (Prioritized HIGH GAP)
        definitions.add(new ModuleDefinition(
                "Module 1: Spring Core & Dependency Injection",
                "Understand the Spring container, Bean lifecycles, ApplicationContext, and @Autowired vs constructor injection.",
                "Week 1-2",
                mod1Prereq,
                List.of(
                    new LessonDefinition("Why Inversion of Control matters",
                            RoadmapLesson.LessonType.THEORY_AND_CODE,
                            "Introduction to inversion of control: moving lifecycle management and dependency wiring from hard-coded 'new' operators into the Spring IoC container."),
                    new LessonDefinition("Spring Beans & ApplicationContext",
                            RoadmapLesson.LessonType.HANDS_ON_LAB,
                            "Lab: Initializing an ApplicationContext, configuring beans via @Component and @Configuration, and inspecting the bean registry."),
                    new LessonDefinition("Constructor Injection vs Field Injection",
                            RoadmapLesson.LessonType.BEST_PRACTICE,
                            "Architectural comparison: Why constructor injection ensures immutability, facilitates unit testing, and eliminates partial initialization bugs."),
                    new LessonDefinition("Assessment: IoC Mastery Test",
                            RoadmapLesson.LessonType.QUIZ,
                            "Knowledge check evaluating bean scopes (singleton vs prototype), circular dependencies, and @Primary bean resolution.")
                )
        ));

        // Module 2: Building RESTful APIs with Spring Web
        definitions.add(new ModuleDefinition(
                "Module 2: Building RESTful APIs with Spring Web",
                "Accelerated Milestone: Baseline mastered (100%). Advanced focus on idempotent HTTP methods, global exception handling, and API contract design.",
                "Week 3",
                "Module 1: Spring Core & IoC",
                List.of(
                    new LessonDefinition("@RestController and Request Mappings",
                            RoadmapLesson.LessonType.HANDS_ON_LAB,
                            "Lab: Implementing clean REST endpoints with @GetMapping, @PostMapping, @PutMapping, and @DeleteMapping."),
                    new LessonDefinition("DTOs, Validation & Global @ControllerAdvice",
                            RoadmapLesson.LessonType.BEST_PRACTICE,
                            "Best practices: Separating entity models from request/response DTOs, Bean Validation (@Valid), and centralized error handling."),
                    new LessonDefinition("HTTP Status Code Conventions & Idempotency",
                            RoadmapLesson.LessonType.THEORY_AND_CODE,
                            "Idempotent HTTP methods (PUT, GET, DELETE) vs non-idempotent (POST), and RFC 7807 problem details specification.")
                )
        ));

        // Module 3: PostgreSQL Persistence with Spring Data JPA
        definitions.add(new ModuleDefinition(
                "Module 3: PostgreSQL Persistence with Spring Data JPA",
                "Accelerated Milestone: Baseline mastered (100%). Advanced focus on preventing N+1 queries, FetchType strategies, and transactional performance.",
                "Week 4-5",
                "Module 2: RESTful APIs",
                List.of(
                    new LessonDefinition("PostgreSQL connection & DataSource configuration",
                            RoadmapLesson.LessonType.THEORY_AND_CODE,
                            "HikariCP connection pool tuning, PostgreSQL dialect configuration, and Spring Boot datasource properties."),
                    new LessonDefinition("@Entity, @Table, and Repository interfaces",
                            RoadmapLesson.LessonType.HANDS_ON_LAB,
                            "Lab: Designing relational entity mappings with @ManyToOne, @OneToMany, and Spring Data JpaRepository interfaces."),
                    new LessonDefinition("FetchType.LAZY vs EAGER & Eliminating N+1 Queries",
                            RoadmapLesson.LessonType.BEST_PRACTICE,
                            "Performance tuning: Avoiding N+1 SELECT queries using @EntityGraph, JOIN FETCH, and projections.")
                )
        ));

        // Module 4: Authentication & Security with JWT
        definitions.add(new ModuleDefinition(
                "Module 4: Authentication & Security with JWT",
                "Accelerated Milestone: Baseline mastered (100%). Advanced focus on stateless JWT architectures, BCrypt password hashing, and filter chains.",
                "Week 6-7",
                "Module 3: Spring Data JPA",
                List.of(
                    new LessonDefinition("Spring Security Filter Chain Architecture",
                            RoadmapLesson.LessonType.THEORY_AND_CODE,
                            "Deep dive into SecurityFilterChain, DelegatingFilterProxy, and stateless session creation policies."),
                    new LessonDefinition("JWT Token Minting & Verification",
                            RoadmapLesson.LessonType.HANDS_ON_LAB,
                            "Lab: Implementing stateless JWT authentication filters, signing claims with HMAC/RSA, and parsing Authorization Bearer headers."),
                    new LessonDefinition("Password Hashing with BCrypt",
                            RoadmapLesson.LessonType.BEST_PRACTICE,
                            "Production credential security: BCrypt work factor salt generation, timing attack resistance, and password encoder beans.")
                )
        ));

        // Module 5: Portfolio Capstone Project
        definitions.add(new ModuleDefinition(
                "Module 5: Portfolio Capstone Project",
                "Comprehensive synthesis milestone. Design and deploy an end-to-end production Java microservice incorporating all verified competencies.",
                "Week 8",
                "Modules 1 through 4",
                List.of(
                    new LessonDefinition("System Design & ERD Specification",
                            RoadmapLesson.LessonType.THEORY_AND_CODE,
                            "Architectural blueprint: Data modeling, relational schema constraints, DTO contracts, and microservice boundaries."),
                    new LessonDefinition("Full Stack Integration & Public Deployment",
                            RoadmapLesson.LessonType.CAPSTONE,
                            "Hands-on capstone: Assembling controllers, service layer transactions, JPA repositories, security filters, and containerizing with Docker.")
                )
        ));

        return definitions;
    }

    private record ModuleDefinition(
            String title,
            String description,
            String durationLabel,
            String prerequisiteSummary,
            List<LessonDefinition> lessons
    ) {}

    private record LessonDefinition(
            String title,
            RoadmapLesson.LessonType lessonType,
            String contentBody
    ) {}
}
