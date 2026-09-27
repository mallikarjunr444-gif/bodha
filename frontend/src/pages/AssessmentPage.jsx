import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  CheckSquare,
  ArrowRight,
  ArrowLeft,
  Sparkles,
  HelpCircle,
  CheckCircle2,
  BookOpen,
} from 'lucide-react';
import { useLearner } from '../context/LearnerContext';
import { assessmentApi } from '../services/api';
import JourneyStepper from '../components/JourneyStepper';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import EmptyState from '../components/common/EmptyState';
import SectionHeader from '../components/common/SectionHeader';
import Card from '../components/common/Card';
import Badge from '../components/common/Badge';
import Button from '../components/common/Button';
import ProgressBar from '../components/common/ProgressBar';

export default function AssessmentPage() {
  const {
    user,
    selectedSubject,
    selectSubject,
    activeGoal,
    activeAttempt,
    setActiveAttempt,
    answers,
    recordAnswer,
    submitAssessment,
  } = useLearner();

  const navigate = useNavigate();

  const [questions, setQuestions] = useState([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [noDiagnosticFound, setNoDiagnosticFound] = useState(false);

  // Initialize or resume assessment attempt
  const initAssessment = useCallback(async () => {
    if (!selectedSubject?.id) return;
    setLoading(true);
    setError(null);
    setNoDiagnosticFound(false);

    try {
      // 1. Fetch diagnostic assessment metadata
      let diagnosticMeta;
      try {
        diagnosticMeta = await assessmentApi.getDiagnosticAssessment(selectedSubject.id);
      } catch (err) {
        if (err.status === 404) {
          setNoDiagnosticFound(true);
          setLoading(false);
          return;
        }
        throw err;
      }

      // 2. Start a fresh attempt or use existing active attempt
      const attemptRes = await assessmentApi.startAttempt(diagnosticMeta.id, {
        userId: user?.id,
        learnerGoalId: activeGoal?.id,
      });

      setActiveAttempt(attemptRes);
      if (attemptRes.questions && attemptRes.questions.length > 0) {
        setQuestions(attemptRes.questions);
      } else {
        // Fallback: fetch attempt with questions
        const fullAttempt = await assessmentApi.getAttempt(attemptRes.id, user?.id);
        setActiveAttempt(fullAttempt);
        setQuestions(fullAttempt.questions || []);
      }
    } catch (err) {
      setError(err.message || 'Failed to initialize diagnostic assessment.');
    } finally {
      setLoading(false);
    }
  }, [selectedSubject?.id, user?.id, activeGoal?.id, setActiveAttempt]);

  useEffect(() => {
    initAssessment();
  }, [initAssessment]);

  const currentQ = questions[currentIndex];

  const handleSelectOption = async (optionId) => {
    if (!currentQ || !activeAttempt?.id) return;
    recordAnswer(currentQ.id, optionId);

    // Asynchronously record response on backend (safe, does not reveal correctness)
    try {
      await assessmentApi.submitResponse(activeAttempt.id, {
        userId: user?.id,
        questionId: currentQ.id,
        selectedOptionId: optionId,
      });
    } catch (err) {
      console.warn('Could not record response immediately to backend:', err);
    }
  };

  const handleSubmit = async () => {
    if (!activeAttempt?.id) return;
    setSubmitting(true);
    setError(null);

    try {
      // Authoritative backend completion: calculates score on server & analyzes skill gaps
      await submitAssessment(activeAttempt.id);
      navigate('/skill-gap');
    } catch (err) {
      setError(err.message || 'Failed to finalize assessment on server.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleSwitchToJava = () => {
    selectSubject({
      id: 'java-backend',
      domainId: 'programming',
      domain: 'programming',
      domainName: 'Software & Engineering',
      title: 'Full-Stack Java & Spring Boot Architecture',
      tagline: 'Modern backend engineering, REST services, JPA, and Spring Security',
      difficulty: 'Intermediate',
      estimatedWeeks: 8,
      popular: true,
      skillsCovered: [
        'Core Java OOP',
        'Spring Core & IoC',
        'REST API Design',
        'JPA & Database Performance',
        'Security & Authentication',
      ],
    });
  };

  const progressPercent = questions.length > 0 ? Math.round(((currentIndex + 1) / questions.length) * 100) : 0;

  return (
    <div className="min-h-[calc(100vh-4rem)] flex flex-col bg-[var(--bg-canvas)]">
      <JourneyStepper />

      <main className="flex-1 max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10 w-full space-y-8">
        {/* Header */}
        <SectionHeader
          eyebrow="Diagnostic Benchmark"
          eyebrowIcon={CheckSquare}
          title="Prerequisite & Skill-Gap Evaluation"
          description={`Evaluating knowledge baseline for ${selectedSubject?.title || 'Selected Track'}. Answering candidly ensures foundational gaps are addressed in your customized roadmap.`}
          action={
            questions.length > 0 && (
              <Badge variant="primary" size="md">
                Question {currentIndex + 1} of {questions.length}
              </Badge>
            )
          }
        />

        {/* Error notification */}
        {error && (
          <ErrorMessage
            title="Assessment Engine Error"
            message={error}
            onRetry={initAssessment}
          />
        )}

        {/* Loading state */}
        {loading ? (
          <div className="py-20">
            <LoadingSpinner
              message="Connecting to Jnanora Assessment Engine & loading secure question items..."
              size="lg"
            />
          </div>
        ) : noDiagnosticFound ? (
          <div className="py-8">
            <EmptyState
              icon={BookOpen}
              title="Diagnostic Test Not Yet Available for this Track"
              description={`The subject "${selectedSubject?.title}" does not have a seeded diagnostic evaluation in the database yet. The Full-Stack Java & Spring Boot track is fully configured with verified diagnostic questions.`}
              action={
                <div className="flex items-center gap-3">
                  <Button
                    variant="primary"
                    size="sm"
                    onClick={handleSwitchToJava}
                  >
                    Switch to Java & Spring Boot Track
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => navigate('/choose-subject')}
                  >
                    Back to Catalog
                  </Button>
                </div>
              }
            />
          </div>
        ) : currentQ ? (
          <>
            {/* Progress Bar */}
            <div className="space-y-1.5">
              <div className="flex items-center justify-between text-xs text-[var(--text-muted)] font-mono">
                <span>Progress: {progressPercent}%</span>
                <span>{Object.keys(answers).length} of {questions.length} Answered</span>
              </div>
              <ProgressBar
                progress={progressPercent}
                variant="primary"
                size="md"
              />
            </div>

            {/* Question Card */}
            <Card variant="elevated" className="p-6 sm:p-8 space-y-6">
              <div className="flex items-center justify-between pb-4 border-b border-[var(--border-subtle)]">
                <Badge variant="ai" size="sm">
                  Target Skill: {currentQ.skillName || currentQ.skill || 'Core Knowledge'}
                </Badge>
                <span className="text-xs text-[var(--text-muted)] flex items-center gap-1.5">
                  <HelpCircle className="w-3.5 h-3.5 text-blue-400" />
                  <span>1 Point Diagnostic Weight</span>
                </span>
              </div>

              <div className="text-lg sm:text-xl font-bold text-white leading-relaxed tracking-tight">
                {currentQ.questionText || currentQ.question}
              </div>

              {/* Options */}
              <div className="space-y-3 pt-2">
                {currentQ.options?.map((opt, optIdx) => {
                  const optId = opt.id ?? optIdx;
                  const isSelected = answers[currentQ.id] === optId;
                  const letter = String.fromCharCode(65 + optIdx);

                  return (
                    <div
                      key={optId}
                      onClick={() => handleSelectOption(optId)}
                      className={`p-4 rounded-xl border transition-all duration-200 cursor-pointer flex items-start gap-4 ${
                        isSelected
                          ? 'bg-blue-500/10 border-blue-500/80 shadow-lg shadow-blue-500/15 ring-1 ring-blue-500/50 text-white'
                          : 'bg-[var(--bg-canvas)]/80 border-[var(--border-subtle)] hover:border-[var(--border-default)] hover:bg-[var(--bg-surface)] text-[var(--text-secondary)] hover:text-white'
                      }`}
                    >
                      <div
                        className={`w-7 h-7 rounded-lg flex items-center justify-center text-xs font-mono font-bold shrink-0 mt-0.5 transition-colors ${
                          isSelected
                            ? 'bg-blue-600 text-white shadow-md shadow-blue-600/30'
                            : 'bg-[var(--bg-surface-elevated)] text-[var(--text-muted)] border border-[var(--border-subtle)]'
                        }`}
                      >
                        {letter}
                      </div>
                      <div className="text-sm leading-relaxed flex-1 pt-0.5 font-medium">
                        {opt.optionText || opt}
                      </div>
                      {isSelected && (
                        <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0 mt-0.5 animate-in fade-in zoom-in-75 duration-200" />
                      )}
                    </div>
                  );
                })}
              </div>
            </Card>

            {/* Stepper Navigation Controls */}
            <div className="flex items-center justify-between pt-2">
              <Button
                variant="outline"
                size="md"
                disabled={currentIndex === 0 || submitting}
                onClick={() => setCurrentIndex((prev) => prev - 1)}
                icon={ArrowLeft}
              >
                Previous
              </Button>

              <div className="flex items-center gap-3">
                {currentIndex < questions.length - 1 ? (
                  <Button
                    variant="primary"
                    size="md"
                    disabled={submitting}
                    onClick={() => setCurrentIndex((prev) => prev + 1)}
                    iconRight={ArrowRight}
                  >
                    Next Question
                  </Button>
                ) : (
                  <Button
                    variant="ai"
                    size="md"
                    loading={submitting}
                    disabled={submitting}
                    onClick={handleSubmit}
                    icon={Sparkles}
                    iconRight={ArrowRight}
                  >
                    {submitting ? 'Evaluating Responses on Backend...' : 'Analyze Skill Gaps'}
                  </Button>
                )}
              </div>
            </div>

            {/* Question Selector Quick Bar */}
            <div className="pt-6 border-t border-[var(--border-subtle)] flex flex-wrap items-center justify-center gap-2">
              {questions.map((q, idx) => {
                const isAnswered = answers[q.id] !== undefined;
                const isCurrent = currentIndex === idx;

                return (
                  <button
                    key={q.id}
                    type="button"
                    onClick={() => setCurrentIndex(idx)}
                    className={`w-9 h-9 rounded-xl text-xs font-mono font-bold transition-all cursor-pointer ${
                      isCurrent
                        ? 'bg-blue-600 text-white shadow-md shadow-blue-600/40 ring-2 ring-blue-400/50'
                        : isAnswered
                        ? 'bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 hover:bg-emerald-500/20'
                        : 'bg-[var(--bg-surface)] border border-[var(--border-subtle)] text-[var(--text-muted)] hover:text-white hover:border-[var(--border-default)]'
                    }`}
                  >
                    {idx + 1}
                  </button>
                );
              })}
            </div>
          </>
        ) : null}
      </main>
    </div>
  );
}
