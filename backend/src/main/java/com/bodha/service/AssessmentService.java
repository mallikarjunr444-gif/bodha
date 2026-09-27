package com.bodha.service;

import com.bodha.dto.*;
import com.bodha.exception.ResourceNotFoundException;
import com.bodha.model.*;
import com.bodha.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service managing diagnostic assessments, interactive test attempts,
 * response recording, deterministic server-side evaluation, and skill-level telemetry (Module D).
 */
@Service
@Transactional(readOnly = true)
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final AssessmentOptionRepository assessmentOptionRepository;
    private final AssessmentAttemptRepository assessmentAttemptRepository;
    private final AssessmentResponseRepository assessmentResponseRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final LearnerGoalRepository learnerGoalRepository;

    public AssessmentService(AssessmentRepository assessmentRepository,
                             AssessmentQuestionRepository assessmentQuestionRepository,
                             AssessmentOptionRepository assessmentOptionRepository,
                             AssessmentAttemptRepository assessmentAttemptRepository,
                             AssessmentResponseRepository assessmentResponseRepository,
                             UserRepository userRepository,
                             SubjectRepository subjectRepository,
                             LearnerGoalRepository learnerGoalRepository) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentQuestionRepository = assessmentQuestionRepository;
        this.assessmentOptionRepository = assessmentOptionRepository;
        this.assessmentAttemptRepository = assessmentAttemptRepository;
        this.assessmentResponseRepository = assessmentResponseRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.learnerGoalRepository = learnerGoalRepository;
    }

    /**
     * Retrieves high-level assessment summary by ID.
     */
    public AssessmentSummaryResponseDto getAssessmentById(Long assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId));
        int count = (int) assessmentQuestionRepository.countByAssessmentId(assessmentId);
        return AssessmentSummaryResponseDto.fromEntity(assessment, count);
    }

    /**
     * Retrieves all assessments associated with a specific subject track.
     */
    public List<AssessmentSummaryResponseDto> getAssessmentsBySubjectId(String subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("Subject not found with id: " + subjectId);
        }
        return assessmentRepository.findBySubjectId(subjectId).stream()
                .map(a -> {
                    int count = (int) assessmentQuestionRepository.countByAssessmentId(a.getId());
                    return AssessmentSummaryResponseDto.fromEntity(a, count);
                })
                .toList();
    }

    /**
     * Retrieves the primary diagnostic assessment for a subject.
     */
    public AssessmentSummaryResponseDto getDiagnosticAssessmentBySubjectId(String subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("Subject not found with id: " + subjectId);
        }
        Assessment assessment = assessmentRepository.findBySubjectIdAndAssessmentType(subjectId, AssessmentType.DIAGNOSTIC)
                .orElseThrow(() -> new ResourceNotFoundException("No diagnostic assessment found for subject: " + subjectId));
        int count = (int) assessmentQuestionRepository.countByAssessmentId(assessment.getId());
        return AssessmentSummaryResponseDto.fromEntity(assessment, count);
    }

    /**
     * Starts a new assessment attempt session for a learner.
     */
    @Transactional
    public AssessmentAttemptResponseDto startAttempt(Long assessmentId, StartAttemptRequestDto request) {
        if (request == null || request.userId() == null) {
            throw new IllegalArgumentException("User ID is required to start an assessment attempt");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId));

        LearnerGoal learnerGoal = null;
        if (request.learnerGoalId() != null) {
            learnerGoal = learnerGoalRepository.findById(request.learnerGoalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Learner goal not found with id: " + request.learnerGoalId()));

            if (!learnerGoal.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Learner goal ID " + request.learnerGoalId() + " does not belong to user ID " + user.getId());
            }
            if (!learnerGoal.getSubject().getId().equals(assessment.getSubject().getId())) {
                throw new IllegalArgumentException("Learner goal subject '" + learnerGoal.getSubject().getId()
                        + "' does not match assessment subject '" + assessment.getSubject().getId() + "'");
            }
        }

        List<AssessmentQuestion> questions = assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(assessmentId);
        if (questions.isEmpty()) {
            throw new IllegalStateException("Cannot start attempt: assessment has no questions configured");
        }

        // Initialize attempt in STARTED state: total_questions = 0 marks it in-progress
        AssessmentAttempt attempt = new AssessmentAttempt(user, assessment, learnerGoal, BigDecimal.ZERO, 0, 0);
        AssessmentAttempt saved = assessmentAttemptRepository.save(attempt);

        List<AssessmentQuestionDto> questionDtos = buildLearnerFacingQuestions(questions);
        return AssessmentAttemptResponseDto.fromEntity(saved, questions.size(), 0, questionDtos);
    }

    /**
     * Retrieves attempt session status and question items, validating ownership.
     */
    public AssessmentAttemptResponseDto getAttemptById(Long attemptId, Long userId) {
        AssessmentAttempt attempt = assessmentAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment attempt not found with id: " + attemptId));

        if (userId != null && !attempt.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Assessment attempt " + attemptId + " does not belong to user ID " + userId);
        }

        List<AssessmentQuestion> questions = assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(attempt.getAssessment().getId());
        long answeredCount = assessmentResponseRepository.countByAttemptId(attemptId);

        List<AssessmentQuestionDto> questionDtos = buildLearnerFacingQuestions(questions);
        return AssessmentAttemptResponseDto.fromEntity(attempt, questions.size(), (int) answeredCount, questionDtos);
    }

    /**
     * Submits or updates an answer for a question within an active attempt.
     */
    @Transactional
    public SubmitResponseResponseDto submitResponse(Long attemptId, SubmitResponseRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Submit response request body cannot be null");
        }

        AssessmentAttempt attempt = assessmentAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment attempt not found with id: " + attemptId));

        if (attempt.isCompleted()) {
            throw new IllegalStateException("Cannot submit response: Assessment attempt " + attemptId + " is already completed.");
        }

        if (!attempt.getUser().getId().equals(request.userId())) {
            throw new IllegalArgumentException("Unauthorized: Assessment attempt " + attemptId + " does not belong to user ID " + request.userId());
        }

        AssessmentQuestion question = assessmentQuestionRepository.findById(request.questionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + request.questionId()));

        if (!question.getAssessment().getId().equals(attempt.getAssessment().getId())) {
            throw new IllegalArgumentException("Question " + request.questionId() + " does not belong to assessment " + attempt.getAssessment().getId());
        }

        AssessmentOption option = assessmentOptionRepository.findById(request.selectedOptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Option not found with id: " + request.selectedOptionId()));

        if (!option.getQuestion().getId().equals(question.getId())) {
            throw new IllegalArgumentException("Option " + request.selectedOptionId() + " does not belong to question " + request.questionId());
        }

        boolean isCorrect = option.isCorrect();

        // Check if an existing response exists for this attempt and question (update support)
        Optional<AssessmentResponse> existingOpt = assessmentResponseRepository.findByAttemptIdAndQuestionId(attemptId, question.getId());
        if (existingOpt.isPresent()) {
            AssessmentResponse existing = existingOpt.get();
            existing.setSelectedOption(option);
            existing.setCorrect(isCorrect);
            assessmentResponseRepository.save(existing);
        } else {
            AssessmentResponse newResponse = new AssessmentResponse(attempt, question, option, isCorrect);
            assessmentResponseRepository.save(newResponse);
        }

        return new SubmitResponseResponseDto(attemptId, question.getId(), option.getId(), "Answer recorded successfully");
    }

    /**
     * Completes an assessment attempt, performing deterministic backend score calculation.
     */
    @Transactional
    public AssessmentResultResponseDto completeAssessment(Long attemptId, Long userId) {
        AssessmentAttempt attempt = assessmentAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment attempt not found with id: " + attemptId));

        if (userId != null && !attempt.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Assessment attempt " + attemptId + " does not belong to user ID " + userId);
        }

        if (attempt.isCompleted()) {
            throw new IllegalStateException("Assessment attempt " + attemptId + " is already completed.");
        }

        List<AssessmentQuestion> questions = assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(attempt.getAssessment().getId());
        int totalQuestions = questions.size();

        List<AssessmentResponse> responses = assessmentResponseRepository.findByAttemptId(attemptId);
        Map<Long, AssessmentResponse> responseMap = responses.stream()
                .collect(Collectors.toMap(r -> r.getQuestion().getId(), r -> r, (r1, r2) -> r1));

        int correctAnswers = 0;
        for (AssessmentQuestion q : questions) {
            AssessmentResponse resp = responseMap.get(q.getId());
            if (resp != null && resp.isCorrect()) {
                correctAnswers++;
            }
        }

        BigDecimal scorePercentage;
        if (totalQuestions > 0) {
            double pct = ((double) correctAnswers / totalQuestions) * 100.0;
            scorePercentage = BigDecimal.valueOf(pct).setScale(2, RoundingMode.HALF_UP);
        } else {
            scorePercentage = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        attempt.setTotalQuestions(totalQuestions);
        attempt.setCorrectAnswers(correctAnswers);
        attempt.setScorePercentage(scorePercentage);
        attempt.setCompletedAt(OffsetDateTime.now());

        assessmentAttemptRepository.save(attempt);

        return buildResultDto(attempt, questions, responses);
    }

    /**
     * Retrieves final assessment results, question-level verification, and skill telemetry.
     */
    public AssessmentResultResponseDto getAssessmentResult(Long attemptId, Long userId) {
        AssessmentAttempt attempt = assessmentAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment attempt not found with id: " + attemptId));

        if (userId != null && !attempt.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Assessment attempt " + attemptId + " does not belong to user ID " + userId);
        }

        if (!attempt.isCompleted()) {
            throw new IllegalStateException("Assessment attempt " + attemptId + " has not been completed yet.");
        }

        List<AssessmentQuestion> questions = assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(attempt.getAssessment().getId());
        List<AssessmentResponse> responses = assessmentResponseRepository.findByAttemptId(attemptId);

        return buildResultDto(attempt, questions, responses);
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    private List<AssessmentQuestionDto> buildLearnerFacingQuestions(List<AssessmentQuestion> questions) {
        return questions.stream().map(q -> {
            List<AssessmentOptionDto> options = assessmentOptionRepository
                    .findByQuestionIdOrderByOrderIndexAsc(q.getId())
                    .stream()
                    .map(AssessmentOptionDto::fromEntity)
                    .toList();
            return AssessmentQuestionDto.fromEntity(q, options);
        }).toList();
    }

    private AssessmentResultResponseDto buildResultDto(AssessmentAttempt attempt,
                                                       List<AssessmentQuestion> questions,
                                                       List<AssessmentResponse> responses) {
        Map<Long, AssessmentResponse> responseMap = responses.stream()
                .collect(Collectors.toMap(r -> r.getQuestion().getId(), r -> r, (r1, r2) -> r1));

        List<QuestionResultDto> questionResults = new ArrayList<>();
        Map<Long, int[]> skillStats = new LinkedHashMap<>(); // skillId -> [total, correct]
        Map<Long, Skill> skillMap = new LinkedHashMap<>();

        for (AssessmentQuestion q : questions) {
            List<AssessmentOption> options = assessmentOptionRepository.findByQuestionIdOrderByOrderIndexAsc(q.getId());
            AssessmentOption correctOption = options.stream().filter(AssessmentOption::isCorrect).findFirst().orElse(null);

            AssessmentResponse resp = responseMap.get(q.getId());
            Long selectedOptionId = resp != null ? resp.getSelectedOption().getId() : null;
            String selectedOptionText = resp != null ? resp.getSelectedOption().getOptionText() : "Not Answered";
            boolean isCorrect = resp != null && resp.isCorrect();

            Long skillId = q.getSkill() != null ? q.getSkill().getId() : null;
            String skillName = q.getSkill() != null ? q.getSkill().getName() : "General Concept";
            String skillCategory = q.getSkill() != null && q.getSkill().getCategory() != null
                    ? q.getSkill().getCategory().name() : null;

            if (skillId != null) {
                skillMap.putIfAbsent(skillId, q.getSkill());
                int[] stats = skillStats.computeIfAbsent(skillId, k -> new int[2]);
                stats[0]++; // total questions for this skill
                if (isCorrect) stats[1]++; // correct answers for this skill
            }

            questionResults.add(new QuestionResultDto(
                    q.getId(),
                    q.getQuestionText(),
                    skillId,
                    skillName,
                    skillCategory,
                    selectedOptionId,
                    selectedOptionText,
                    correctOption != null ? correctOption.getId() : null,
                    correctOption != null ? correctOption.getOptionText() : null,
                    isCorrect,
                    q.getExplanation()
            ));
        }

        List<SkillPerformanceDto> skillBreakdown = new ArrayList<>();
        for (Map.Entry<Long, int[]> entry : skillStats.entrySet()) {
            Long sId = entry.getKey();
            int[] stats = entry.getValue();
            Skill s = skillMap.get(sId);
            double pct = stats[0] > 0 ? ((double) stats[1] / stats[0]) * 100.0 : 0.0;

            skillBreakdown.add(new SkillPerformanceDto(
                    sId,
                    s.getName(),
                    s.getCategory() != null ? s.getCategory().name() : null,
                    stats[0],
                    stats[1],
                    Math.round(pct * 100.0) / 100.0
            ));
        }

        Long assessmentId = attempt.getAssessment() != null ? attempt.getAssessment().getId() : null;
        String assessmentTitle = attempt.getAssessment() != null ? attempt.getAssessment().getTitle() : null;
        String assessmentType = attempt.getAssessment() != null && attempt.getAssessment().getAssessmentType() != null
                ? attempt.getAssessment().getAssessmentType().name() : null;
        Long goalId = attempt.getLearnerGoal() != null ? attempt.getLearnerGoal().getId() : null;

        int total = attempt.getTotalQuestions();
        int answered = responses.size();
        int correct = attempt.getCorrectAnswers();
        int incorrect = total - correct;

        return new AssessmentResultResponseDto(
                attempt.getId(),
                attempt.getUser().getId(),
                assessmentId,
                assessmentTitle,
                assessmentType,
                goalId,
                "COMPLETED",
                total,
                answered,
                correct,
                incorrect,
                attempt.getScorePercentage(),
                attempt.getCompletedAt(),
                skillBreakdown,
                questionResults
        );
    }
}
