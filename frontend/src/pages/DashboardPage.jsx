import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import {
  Brain,
  Flame,
  CheckCircle2,
  Sparkles,
  BookOpen,
  X,
  ArrowRight,
  TrendingUp,
  Target,
  ChevronRight,
  Zap,
  Lightbulb,
  AlertTriangle,
  Award,
  Terminal,
} from 'lucide-react';
import { useLearner } from '../context/LearnerContext';
import { aiApi } from '../services/api';
import JourneyStepper from '../components/JourneyStepper';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import Card from '../components/common/Card';
import Badge from '../components/common/Badge';
import Button from '../components/common/Button';

export default function DashboardPage() {
  const {
    user,
    selectedSubject,
    completedLessons,
    toggleLessonComplete,
    streakDays,
    totalXp,
    roadmap,
    roadmapProgress,
    aiRecommendation,
    aiNextStep,
    skillGaps,
    refreshDashboardData,
  } = useLearner();

  const [_loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Lesson AI Assistant Modal State
  const [isAssistModalOpen, setIsAssistModalOpen] = useState(false);
  const [assistData, setAssistData] = useState(null);
  const [assistLoading, setAssistLoading] = useState(false);
  const [assistError, setAssistError] = useState(null);

  const initDashboard = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      await refreshDashboardData();
    } catch (err) {
      setError(err.message || 'Unable to refresh dashboard telemetry.');
    } finally {
      setLoading(false);
    }
  }, [refreshDashboardData]);

  useEffect(() => {
    initDashboard();
  }, [initDashboard]);

  // Derive active module & lesson from real roadmap
  const modules = roadmap?.modules || [];
  const activeModule = modules.find((m) => m.isCurrent) || modules[0];
  const activeLesson =
    activeModule?.lessons?.find((l) => !completedLessons.includes(l.id)) ||
    activeModule?.lessons?.[0];

  const totalLessons = roadmap?.totalLessons ||
    modules.reduce((acc, m) => acc + (m.lessons?.length || 0), 0) || 15;

  const totalCompleted =
    completedLessons.length ||
    roadmapProgress?.completedLessons ||
    modules.reduce(
      (acc, m) => acc + (m.lessons?.filter((l) => completedLessons.includes(l.id))?.length || 0),
      0
    );

  const progressPercentage =
    roadmap?.overallProgressPercentage ??
    roadmapProgress?.overallProgressPercentage ??
    Math.min(100, Math.round((totalCompleted / (totalLessons || 1)) * 100));

  // Request lesson-specific AI assistance (Module H)
  const handleOpenLessonAssist = async () => {
    if (!activeLesson?.id || !user?.id) return;
    setIsAssistModalOpen(true);
    setAssistLoading(true);
    setAssistError(null);

    try {
      const data = await aiApi.getLessonAssistance(activeLesson.id, user.id);
      setAssistData(data);
    } catch (err) {
      setAssistError(err.message || 'Could not retrieve AI lesson assistance.');
    } finally {
      setAssistLoading(false);
    }
  };

  const handleCompleteCurrentLesson = async () => {
    if (activeLesson) {
      await toggleLessonComplete(activeLesson.id);
    }
  };

  const displayedGaps = Array.isArray(skillGaps) ? skillGaps.slice(0, 4) : [];

  return (
    <div className="min-h-[calc(100vh-4rem)] flex flex-col bg-[var(--bg-canvas)]">
      <JourneyStepper />

      <main className="flex-1 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 w-full space-y-8">
        {/* Welcome Header */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 pb-6 border-b border-[var(--border-subtle)]">
          <div className="space-y-1.5">
            <div className="flex items-center gap-2">
              <Badge variant="primary" size="sm">
                Command Center
              </Badge>
              <span className="text-xs text-[var(--text-muted)] font-mono">
                PostgreSQL Live Telemetry • ID #{user?.id || 1}
              </span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              Welcome back, {user?.fullName || user?.name || 'Learner'}
            </h1>
            <p className="text-xs sm:text-sm text-[var(--text-secondary)]">
              Active Track: <strong className="text-white">{selectedSubject?.title || 'Selected Subject'}</strong>
            </p>
          </div>

          <div className="flex items-center gap-3">
            <Link to="/choose-subject">
              <Button variant="outline" size="sm" icon={BookOpen}>
                Change Subject
              </Button>
            </Link>
            <Link to="/roadmap">
              <Button variant="primary" size="sm" iconRight={ChevronRight}>
                View Full Roadmap
              </Button>
            </Link>
          </div>
        </div>

        {error && (
          <ErrorMessage
            title="Telemetry Sync Notice"
            message={error}
            onRetry={initDashboard}
          />
        )}

        {/* Telemetry Stats Row */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {/* Streak */}
          <Card variant="default" className="p-5 flex items-center gap-4 hover:border-amber-500/30 transition-colors">
            <div className="w-12 h-12 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-400 flex items-center justify-center shrink-0">
              <Flame className="w-6 h-6 text-amber-400" />
            </div>
            <div>
              <div className="text-2xl font-black text-white">{streakDays} Day{streakDays === 1 ? '' : 's'}</div>
              <div className="text-xs text-[var(--text-muted)] font-medium">Consistency Streak</div>
            </div>
          </Card>

          {/* Curriculum Progress */}
          <Card variant="default" className="p-5 flex items-center gap-4 hover:border-blue-500/30 transition-colors">
            <div className="w-12 h-12 rounded-xl bg-blue-500/10 border border-blue-500/20 text-blue-400 flex items-center justify-center shrink-0">
              <TrendingUp className="w-6 h-6 text-blue-400" />
            </div>
            <div className="flex-1 min-w-0">
              <div className="text-2xl font-black text-white">{progressPercentage}%</div>
              <div className="text-xs text-[var(--text-muted)] font-medium">Roadmap Completed</div>
            </div>
          </Card>

          {/* Gamification Total XP */}
          <Card variant="default" className="p-5 flex items-center gap-4 hover:border-emerald-500/30 transition-colors">
            <div className="w-12 h-12 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0">
              <Award className="w-6 h-6 text-emerald-400" />
            </div>
            <div>
              <div className="text-2xl font-black text-white">{totalXp} XP</div>
              <div className="text-xs text-[var(--text-muted)] font-medium">Mastery Experience</div>
            </div>
          </Card>

          {/* Mastered Lessons */}
          <Card variant="default" className="p-5 flex items-center gap-4 hover:border-cyan-500/30 transition-colors">
            <div className="w-12 h-12 rounded-xl bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 flex items-center justify-center shrink-0">
              <CheckCircle2 className="w-6 h-6 text-cyan-400" />
            </div>
            <div>
              <div className="text-2xl font-black text-white">
                {totalCompleted}/{totalLessons}
              </div>
              <div className="text-xs text-[var(--text-muted)] font-medium">Lessons Verified</div>
            </div>
          </Card>
        </div>

        {/* AI Recommendations & Next-Step Highlights (Module H) */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {/* AI Recommendation Card */}
          <Card variant="ai" className="p-6 flex items-start gap-4">
            <div className="w-10 h-10 rounded-xl bg-cyan-500/10 border border-cyan-500/30 text-cyan-400 flex items-center justify-center shrink-0 mt-0.5 shadow-sm shadow-cyan-500/15">
              <Sparkles className="w-5 h-5 text-cyan-400" />
            </div>
            <div className="flex-1 space-y-2 text-xs sm:text-sm">
              <div className="font-bold text-white flex items-center justify-between gap-2">
                <span className="text-sm tracking-tight">{aiRecommendation?.title || 'Personalized AI Guidance'}</span>
                {aiRecommendation?.priority && (
                  <Badge variant="ai" size="xs">
                    {aiRecommendation.priority} Priority
                  </Badge>
                )}
              </div>
              <p className="text-[var(--text-secondary)] leading-relaxed text-xs">
                {aiRecommendation?.summary ||
                  'Your diagnostic gaps are continuously cross-referenced with your active learning velocity.'}
              </p>
              {aiRecommendation?.suggestedAction && (
                <div className="pt-1 flex items-center gap-2 text-cyan-300 font-medium text-xs">
                  <Zap className="w-3.5 h-3.5 text-cyan-400 shrink-0" />
                  <span>Action: {aiRecommendation.suggestedAction}</span>
                </div>
              )}
            </div>
          </Card>

          {/* AI Next-Step Guidance Card */}
          <Card variant="glow" className="p-6 flex items-start gap-4">
            <div className="w-10 h-10 rounded-xl bg-blue-500/10 border border-blue-500/30 text-blue-400 flex items-center justify-center shrink-0 mt-0.5 shadow-sm shadow-blue-500/15">
              <Lightbulb className="w-5 h-5 text-blue-400" />
            </div>
            <div className="flex-1 space-y-2 text-xs sm:text-sm">
              <div className="font-bold text-white flex items-center justify-between gap-2">
                <span className="text-sm tracking-tight">{aiNextStep?.title || 'Next Learning Action'}</span>
                {aiNextStep?.remainingLessonsInModule != null && (
                  <Badge variant="primary" size="xs">
                    {aiNextStep.remainingLessonsInModule} Lessons Left
                  </Badge>
                )}
              </div>
              <p className="text-[var(--text-secondary)] leading-relaxed text-xs">
                {aiNextStep?.summary ||
                  'Advance along your sequenced roadmap milestone to unlock subsequent learning modules.'}
              </p>
              {aiNextStep?.nextLessonTitle && (
                <div className="pt-1 flex items-center gap-2 text-blue-300 font-medium text-xs">
                  <ArrowRight className="w-3.5 h-3.5 text-blue-400 shrink-0" />
                  <span>Next Lesson: {aiNextStep.nextLessonTitle}</span>
                </div>
              )}
            </div>
          </Card>
        </div>

        {/* Main Content Grid: Active Lesson Focus + Sidebar */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Active Lesson Card (2 Cols) - Visual Focal Point */}
          <div className="lg:col-span-2 space-y-6">
            <Card variant="glow" className="p-6 sm:p-8 space-y-6">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-[var(--border-subtle)] gap-2">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-xs font-mono uppercase tracking-wider text-blue-400 font-semibold">
                      Current Milestone • {activeModule?.title || 'Active Module'}
                    </span>
                  </div>
                  <h2 className="text-xl sm:text-2xl font-bold text-white tracking-tight">
                    {activeLesson?.title || 'Spring Core & REST Architecture'}
                  </h2>
                </div>
                <Badge variant="primary" size="sm" className="self-start sm:self-center">
                  {activeLesson?.type || activeLesson?.lessonType || 'Hands-on Lab'}
                </Badge>
              </div>

              {/* Lesson Concept & Code Sandbox */}
              <div className="space-y-4 text-xs sm:text-sm text-[var(--text-secondary)] leading-relaxed">
                <p>
                  {activeLesson?.contentBody ||
                    'In the Spring Framework, the ApplicationContext acts as the IoC Container, responsible for instantiating, configuring, and assembling beans using constructor injection.'}
                </p>

                <div className="p-4 rounded-xl bg-[#050810] border border-[var(--border-subtle)] font-mono text-xs text-slate-300 overflow-x-auto shadow-inner">
                  <div className="flex items-center justify-between pb-2 mb-3 border-b border-slate-800/80 text-[10px] text-[var(--text-muted)]">
                    <span className="flex items-center gap-1.5">
                      <Terminal className="w-3 h-3 text-blue-400" />
                      <span>Enterprise Service Implementation Pattern</span>
                    </span>
                    <span>Java 21</span>
                  </div>
                  <span className="text-blue-400">@RestController</span>
                  <br />
                  <span className="text-blue-400">@RequestMapping</span>(<span className="text-emerald-300">"/api/learning"</span>)
                  <br />
                  <span className="text-cyan-400">public class</span> <span className="text-amber-300">LearningController</span> {'{'}
                  <br />
                  &nbsp;&nbsp;<span className="text-cyan-400">private final</span> LearningService service;
                  <br />
                  &nbsp;&nbsp;<span className="text-cyan-400">public</span> LearningController(LearningService service) {'{'}
                  <br />
                  &nbsp;&nbsp;&nbsp;&nbsp;<span className="text-cyan-400">this</span>.service = service;
                  <br />
                  &nbsp;&nbsp;{'}'}
                  <br />
                  {'}'}
                </div>
              </div>

              {/* Action Buttons */}
              <div className="pt-2 flex flex-col sm:flex-row items-center gap-3">
                <Button
                  variant="ai"
                  size="md"
                  onClick={handleOpenLessonAssist}
                  icon={Brain}
                  className="w-full sm:w-auto"
                >
                  Ask AI Tutor for Lesson Assistance
                </Button>

                <Button
                  variant="outline"
                  size="md"
                  onClick={handleCompleteCurrentLesson}
                  icon={CheckCircle2}
                  className="w-full sm:w-auto"
                >
                  {activeLesson && completedLessons.includes(activeLesson.id)
                    ? 'Mark Incomplete'
                    : 'Mark Concept Understood (+50 XP)'}
                </Button>
              </div>
            </Card>
          </div>

          {/* Sidebar: Module Checklist & Top Skill Gaps */}
          <div className="space-y-6">
            {/* Module Checklist */}
            <Card variant="default" className="p-6 space-y-4">
              <div className="flex items-center justify-between pb-3 border-b border-[var(--border-subtle)]">
                <h3 className="text-sm font-bold text-white flex items-center gap-2 tracking-tight">
                  <Target className="w-4 h-4 text-blue-400" />
                  <span>Module Checklist</span>
                </h3>
                <span className="text-xs text-[var(--text-muted)] font-mono">
                  {activeModule?.lessons?.filter((l) => completedLessons.includes(l.id))?.length || 0}/
                  {activeModule?.lessons?.length || 0}
                </span>
              </div>

              <div className="space-y-2">
                {activeModule?.lessons?.map((lesson) => {
                  const isDone = completedLessons.includes(lesson.id);

                  return (
                    <div
                      key={lesson.id}
                      onClick={() => toggleLessonComplete(lesson.id)}
                      className={`p-3 rounded-xl border text-xs flex items-center justify-between cursor-pointer transition-all ${
                        isDone
                          ? 'bg-[var(--bg-canvas)]/70 border-emerald-500/30 text-[var(--text-secondary)]'
                          : 'bg-[var(--bg-canvas)]/40 border-[var(--border-subtle)] text-[var(--text-secondary)] hover:border-[var(--border-default)] hover:text-white'
                      }`}
                    >
                      <div className="flex items-center gap-2.5 flex-1 pr-2 min-w-0">
                        <CheckCircle2
                          className={`w-4 h-4 shrink-0 ${
                            isDone ? 'text-emerald-400' : 'text-slate-600'
                          }`}
                        />
                        <span className={`truncate ${isDone ? 'line-through text-[var(--text-muted)]' : 'text-white'}`}>
                          {lesson.title}
                        </span>
                      </div>
                      <Badge variant="neutral" size="xs">
                        {lesson.type || lesson.lessonType || 'Lesson'}
                      </Badge>
                    </div>
                  );
                })}
              </div>

              <div className="pt-2 border-t border-[var(--border-subtle)]">
                <Link
                  to="/roadmap"
                  className="text-xs font-semibold text-blue-400 hover:text-blue-300 flex items-center justify-center gap-1.5 w-full text-center py-1 transition-colors"
                >
                  <span>See Next Modules in Roadmap</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </div>
            </Card>

            {/* Diagnostic Skill Gaps Snapshot */}
            {displayedGaps.length > 0 && (
              <Card variant="default" className="p-6 space-y-3.5">
                <div className="flex items-center justify-between pb-2 border-b border-[var(--border-subtle)]">
                  <h3 className="text-sm font-bold text-white flex items-center gap-2 tracking-tight">
                    <AlertTriangle className="w-4 h-4 text-amber-400" />
                    <span>Targeted Skill Gaps</span>
                  </h3>
                  <Badge variant="warning" size="xs">
                    {displayedGaps.length} Active
                  </Badge>
                </div>
                <div className="space-y-2">
                  {displayedGaps.map((gap, i) => (
                    <div
                      key={gap.id || i}
                      className="p-2.5 rounded-xl bg-[var(--bg-canvas)]/60 border border-[var(--border-subtle)] flex items-center justify-between text-xs"
                    >
                      <span className="text-slate-300 font-medium truncate max-w-[150px]">
                        {gap.skillName || gap.name}
                      </span>
                      <Badge
                        variant={
                          gap.gapSeverity === 'HIGH'
                            ? 'danger'
                            : gap.gapSeverity === 'MEDIUM'
                            ? 'warning'
                            : 'success'
                        }
                        size="xs"
                      >
                        {gap.gapSeverity || 'MASTERED'}
                      </Badge>
                    </div>
                  ))}
                </div>
              </Card>
            )}
          </div>
        </div>

        {/* AI Lesson Assistance Modal (Module H) */}
        {isAssistModalOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-md p-4 animate-in fade-in duration-200">
            <Card variant="elevated" className="w-full max-w-2xl p-6 sm:p-8 space-y-6 max-h-[90vh] overflow-y-auto border-blue-500/30">
              <div className="flex items-center justify-between pb-4 border-b border-[var(--border-subtle)]">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-cyan-500/10 border border-cyan-500/30 text-cyan-400 flex items-center justify-center">
                    <Brain className="w-5 h-5 text-cyan-400" />
                  </div>
                  <div>
                    <h3 className="text-base font-bold text-white tracking-tight">
                      AI Tutor • {assistData?.lessonTitle || activeLesson?.title}
                    </h3>
                    <span className="text-xs text-[var(--text-muted)]">
                      Context-Aware Tutoring Grounded in Learner Baseline
                    </span>
                  </div>
                </div>
                <button
                  type="button"
                  onClick={() => setIsAssistModalOpen(false)}
                  className="w-8 h-8 rounded-lg bg-[var(--bg-surface)] text-[var(--text-muted)] hover:text-white flex items-center justify-center transition-colors cursor-pointer border border-[var(--border-subtle)]"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              {assistLoading ? (
                <div className="py-12">
                  <LoadingSpinner
                    message="Synthesizing context-aware lesson tutoring..."
                    size="md"
                  />
                </div>
              ) : assistError ? (
                <ErrorMessage
                  title="Assistance Error"
                  message={assistError}
                  onRetry={handleOpenLessonAssist}
                />
              ) : assistData ? (
                <div className="space-y-4 text-xs sm:text-sm">
                  {/* Explanation */}
                  <div className="p-4 rounded-xl bg-[var(--bg-canvas)] border border-[var(--border-subtle)] space-y-2">
                    <span className="text-xs font-bold text-blue-400 uppercase tracking-wider block">
                      Core Concept Explanation:
                    </span>
                    <p className="text-slate-200 leading-relaxed">{assistData.explanation}</p>
                  </div>

                  {/* Learning Tips */}
                  {assistData.learningTips?.length > 0 && (
                    <div className="space-y-2">
                      <span className="text-xs font-bold text-emerald-400 uppercase tracking-wider block">
                        Pedagogical Tips:
                      </span>
                      <ul className="space-y-1.5 pl-4 list-disc text-[var(--text-secondary)]">
                        {assistData.learningTips.map((tip, i) => (
                          <li key={i}>{tip}</li>
                        ))}
                      </ul>
                    </div>
                  )}

                  {/* Common Mistakes */}
                  {assistData.commonMistakes?.length > 0 && (
                    <div className="space-y-2">
                      <span className="text-xs font-bold text-amber-400 uppercase tracking-wider block">
                        Common Anti-Patterns to Avoid:
                      </span>
                      <ul className="space-y-1.5 pl-4 list-disc text-[var(--text-secondary)]">
                        {assistData.commonMistakes.map((mistake, i) => (
                          <li key={i}>{mistake}</li>
                        ))}
                      </ul>
                    </div>
                  )}

                  {/* Practice Suggestions */}
                  {assistData.practiceSuggestions?.length > 0 && (
                    <div className="space-y-2">
                      <span className="text-xs font-bold text-cyan-400 uppercase tracking-wider block">
                        Hands-on Practice Tasks:
                      </span>
                      <ul className="space-y-1.5 pl-4 list-disc text-[var(--text-secondary)]">
                        {assistData.practiceSuggestions.map((prac, i) => (
                          <li key={i}>{prac}</li>
                        ))}
                      </ul>
                    </div>
                  )}
                </div>
              ) : null}

              <div className="pt-3 border-t border-[var(--border-subtle)] flex justify-end">
                <Button
                  variant="primary"
                  size="md"
                  onClick={() => setIsAssistModalOpen(false)}
                >
                  Got It, Continue Studying
                </Button>
              </div>
            </Card>
          </div>
        )}
      </main>
    </div>
  );
}
