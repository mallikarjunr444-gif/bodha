package com.bodha.service;

import com.bodha.dto.SkillGapAnalysisResponseDto;
import com.bodha.dto.SkillGapResponseDto;
import com.bodha.exception.ResourceNotFoundException;
import com.bodha.model.*;
import com.bodha.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service managing deterministic, evidence-based skill gap detection and analysis (Module E).
 *
 * Evaluates completed assessment attempts against granular skills and translates performance
 * into persistent learner skill-gap records (MASTERED vs GAP with HIGH, MEDIUM, LOW severity).
 */
@Service
@Transactional(readOnly = true)
public class SkillGapService {

    private final LearnerSkillGapRepository learnerSkillGapRepository;
    private final AssessmentAttemptRepository assessmentAttemptRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final AssessmentResponseRepository assessmentResponseRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final LearnerGoalRepository learnerGoalRepository;

    public SkillGapService(LearnerSkillGapRepository learnerSkillGapRepository,
                           AssessmentAttemptRepository assessmentAttemptRepository,
                           AssessmentQuestionRepository assessmentQuestionRepository,
                           AssessmentResponseRepository assessmentResponseRepository,
                           UserRepository userRepository,
                           SubjectRepository subjectRepository,
                           LearnerGoalRepository learnerGoalRepository) {
        this.learnerSkillGapRepository = learnerSkillGapRepository;
        this.assessmentAttemptRepository = assessmentAttemptRepository;
        this.assessmentQuestionRepository = assessmentQuestionRepository;
        this.assessmentResponseRepository = assessmentResponseRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.learnerGoalRepository = learnerGoalRepository;
    }

    /**
     * Evaluates a completed assessment attempt and persists/updates learner skill-gap records.
     * Idempotent: repeated calls for the same attempt update records without creating duplicates.
     */
    @Transactional
    public SkillGapAnalysisResponseDto analyzeAttempt(Long attemptId, Long userId) {
        AssessmentAttempt attempt = assessmentAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment attempt not found with id: " + attemptId));

        if (userId != null && !attempt.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Assessment attempt " + attemptId + " does not belong to user ID " + userId);
        }

        if (!attempt.isCompleted()) {
            throw new IllegalStateException("Cannot analyze skill gaps: Assessment attempt " + attemptId + " has not been completed yet.");
        }

        LearnerGoal learnerGoal = attempt.getLearnerGoal();
        if (learnerGoal == null) {
            learnerGoal = learnerGoalRepository.findByUserIdAndSubjectId(attempt.getUser().getId(), attempt.getAssessment().getSubject().getId())
                    .orElseThrow(() -> new IllegalStateException("Cannot analyze skill gaps: No active learner goal found for user ID "
                            + attempt.getUser().getId() + " and subject '" + attempt.getAssessment().getSubject().getId() + "'"));
        }

        List<AssessmentQuestion> questions = assessmentQuestionRepository
                .findByAssessmentIdOrderByOrderIndexAsc(attempt.getAssessment().getId());
        List<AssessmentResponse> responses = assessmentResponseRepository.findByAttemptId(attemptId);
        Map<Long, AssessmentResponse> responseMap = responses.stream()
                .collect(Collectors.toMap(r -> r.getQuestion().getId(), r -> r, (r1, r2) -> r1));

        Map<Long, Skill> skillMap = new LinkedHashMap<>();
        Map<Long, int[]> skillStats = new LinkedHashMap<>(); // skillId -> [totalQuestions, correctAnswers]

        for (AssessmentQuestion q : questions) {
            if (q.getSkill() != null) {
                Skill skill = q.getSkill();
                skillMap.putIfAbsent(skill.getId(), skill);
                int[] stats = skillStats.computeIfAbsent(skill.getId(), k -> new int[2]);
                stats[0]++; // total questions for skill
                AssessmentResponse resp = responseMap.get(q.getId());
                if (resp != null && resp.isCorrect()) {
                    stats[1]++; // correct answers for skill
                }
            }
        }

        List<SkillGapResponseDto> dtoList = new ArrayList<>();
        int masteredCount = 0;
        int gapCount = 0;
        int highSeverityCount = 0;

        for (Map.Entry<Long, int[]> entry : skillStats.entrySet()) {
            Long skillId = entry.getKey();
            int[] stats = entry.getValue();
            Skill skill = skillMap.get(skillId);

            int total = stats[0];
            int correct = stats[1];
            double percentage = total > 0 ? ((double) correct / total) * 100.0 : 0.0;

            LearnerSkillGap.Status status;
            LearnerSkillGap.Severity severity;
            String reason;

            if (percentage >= 80.0) {
                status = LearnerSkillGap.Status.MASTERED;
                severity = null;
                reason = "Demonstrated strong mastery (" + Math.round(percentage) + "%) on diagnostic assessment. Can be accelerated in roadmap.";
                masteredCount++;
            } else if (percentage >= 60.0) {
                status = LearnerSkillGap.Status.GAP;
                severity = LearnerSkillGap.Severity.LOW;
                reason = "Minor deficiency observed (" + Math.round(percentage) + "%). Recommended for targeted reinforcement.";
                gapCount++;
            } else if (percentage >= 40.0) {
                status = LearnerSkillGap.Status.GAP;
                severity = LearnerSkillGap.Severity.MEDIUM;
                reason = "Moderate conceptual gap identified (" + Math.round(percentage) + "%). Needs focused study and hands-on practice.";
                gapCount++;
            } else {
                status = LearnerSkillGap.Status.GAP;
                severity = LearnerSkillGap.Severity.HIGH;
                reason = "Critical competency gap detected (" + Math.round(percentage) + "%). Must be prioritized as a core learning milestone.";
                gapCount++;
                highSeverityCount++;
            }

            // Check if record exists for (user, goal, skill) — adhere to uq_user_goal_skill unique constraint
            Optional<LearnerSkillGap> existingOpt = learnerSkillGapRepository
                    .findByUserIdAndLearnerGoalIdAndSkillId(attempt.getUser().getId(), learnerGoal.getId(), skill.getId());

            LearnerSkillGap record;
            if (existingOpt.isPresent()) {
                record = existingOpt.get();
                record.setStatus(status);
                record.setGapSeverity(severity);
                record.setReason(reason);
                record.setEvaluatedFromAttempt(attempt);
                record.setUpdatedAt(OffsetDateTime.now());
            } else {
                record = new LearnerSkillGap(attempt.getUser(), learnerGoal, skill, status, severity, reason, attempt);
            }

            LearnerSkillGap saved = learnerSkillGapRepository.save(record);
            dtoList.add(SkillGapResponseDto.fromEntity(saved));
        }

        String subjectId = learnerGoal.getSubject() != null ? learnerGoal.getSubject().getId() : null;
        String subjectTitle = learnerGoal.getSubject() != null ? learnerGoal.getSubject().getTitle() : null;

        return new SkillGapAnalysisResponseDto(
                attempt.getId(),
                attempt.getUser().getId(),
                learnerGoal.getId(),
                subjectId,
                subjectTitle,
                skillStats.size(),
                masteredCount,
                gapCount,
                highSeverityCount,
                dtoList
        );
    }

    /**
     * Retrieves all skill gaps recorded for a specific user.
     */
    public List<SkillGapResponseDto> getSkillGapsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return learnerSkillGapRepository.findByUserId(userId).stream()
                .map(SkillGapResponseDto::fromEntity)
                .toList();
    }

    /**
     * Retrieves skill gaps recorded for a user within a specific subject track.
     */
    public List<SkillGapResponseDto> getSkillGapsByUserIdAndSubjectId(Long userId, String subjectId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        if (!subjectRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("Subject not found with id: " + subjectId);
        }
        return learnerSkillGapRepository.findByUserIdAndLearnerGoalSubjectId(userId, subjectId).stream()
                .map(SkillGapResponseDto::fromEntity)
                .toList();
    }

    /**
     * Retrieves skill gaps recorded for a specific learner goal.
     */
    public List<SkillGapResponseDto> getSkillGapsByUserIdAndGoalId(Long userId, Long goalId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        if (!learnerGoalRepository.existsById(goalId)) {
            throw new ResourceNotFoundException("Learner goal not found with id: " + goalId);
        }
        return learnerSkillGapRepository.findByUserIdAndLearnerGoalId(userId, goalId).stream()
                .map(SkillGapResponseDto::fromEntity)
                .toList();
    }
}
