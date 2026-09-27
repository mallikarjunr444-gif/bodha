import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import {
  authApi,
  profileApi,
  goalApi,
  assessmentApi,
  skillGapApi,
  roadmapApi,
  progressApi,
  aiApi,
  storeToken,
  clearToken,
  registerOn401Handler,
} from '../services/api';

const LearnerContext = createContext(null);

const STORAGE_SESSION_KEY = 'bodha_learner_session_v1';
const STORAGE_STATE_KEY = 'bodha_learner_state_v1';

// Default subject fallback if catalog has not loaded yet
const DEFAULT_FALLBACK_SUBJECT = {
  id: 'java-backend',
  domainId: 'programming',
  domain: 'programming',
  domainName: 'Software & Engineering',
  title: 'Full-Stack Java & Spring Boot Architecture',
  tagline: 'Modern backend engineering, REST services, JPA, and Spring Security',
  difficultyLevel: 'Intermediate',
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
};

export function LearnerProvider({ children }) {
  // Session storage loader
  const getInitialUser = () => {
    try {
      const token = getStoredToken();
      const saved = localStorage.getItem(STORAGE_SESSION_KEY);
      if (token && saved) {
        const parsed = JSON.parse(saved);
        if (parsed && parsed.isAuthenticated) return parsed;
      }
    } catch {
      // storage unavailable
    }
    return {
      id: null,
      name: '',
      fullName: '',
      email: '',
      role: '',
      isAuthenticated: false,
    };
  };

  const getSavedState = () => {
    try {
      const saved = localStorage.getItem(STORAGE_STATE_KEY);
      if (saved) return JSON.parse(saved);
    } catch {
      // ignore
    }
    return null;
  };

  const savedState = getSavedState();

  // Core learner identity
  const [user, setUser] = useState(getInitialUser);
  const [selectedSubject, setSelectedSubject] = useState(
    savedState?.selectedSubject || DEFAULT_FALLBACK_SUBJECT
  );

  // Goal & Calibration
  const [goal, setGoal] = useState(savedState?.goal || 'CAREER');
  const [level, setLevel] = useState(savedState?.level || 'INTERMEDIATE');
  const [dailyTime, setDailyTime] = useState(savedState?.dailyTime || '45m');
  const [activeGoal, setActiveGoal] = useState(savedState?.activeGoal || null);

  // Assessment flow
  const [activeAttempt, setActiveAttempt] = useState(savedState?.activeAttempt || null);
  const [answers, setAnswers] = useState(savedState?.answers || {});
  const [diagnosticScore, setDiagnosticScore] = useState(savedState?.diagnosticScore || 0);
  const [assessmentResult, setAssessmentResult] = useState(savedState?.assessmentResult || null);

  // Skill Gap & Curriculum
  const [skillGaps, setSkillGaps] = useState(savedState?.skillGaps || []);
  const [roadmap, setRoadmap] = useState(savedState?.roadmap || null);
  const [roadmapProgress, setRoadmapProgress] = useState(null);
  const [completedLessons, setCompletedLessons] = useState(savedState?.completedLessons || []);
  const [streakDays, setStreakDays] = useState(1);
  const [totalXp, setTotalXp] = useState(0);

  // AI Insights
  const [aiRecommendation, setAiRecommendation] = useState(null);
  const [aiNextStep, setAiNextStep] = useState(null);

  // UI helpers
  const [isQuizModalOpen, setIsQuizModalOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Synchronize minimal state to localStorage
  useEffect(() => {
    try {
      localStorage.setItem(
        STORAGE_STATE_KEY,
        JSON.stringify({
          selectedSubject,
          goal,
          level,
          dailyTime,
          activeGoal,
          activeAttempt,
          answers,
          diagnosticScore,
          assessmentResult,
          skillGaps,
          roadmap,
          completedLessons,
        })
      );
    } catch {
      // ignore
    }
  }, [
    selectedSubject,
    goal,
    level,
    dailyTime,
    activeGoal,
    activeAttempt,
    answers,
    diagnosticScore,
    assessmentResult,
    skillGaps,
    roadmap,
    completedLessons,
  ]);

  // Synchronize user session
  useEffect(() => {
    try {
      if (user && user.isAuthenticated) {
        localStorage.setItem(STORAGE_SESSION_KEY, JSON.stringify(user));
      } else {
        localStorage.removeItem(STORAGE_SESSION_KEY);
      }
    } catch {
      // ignore
    }
  }, [user]);

  // Refresh user profile and telemetry from backend
  const refreshUserData = useCallback(async (userId) => {
    const targetUserId = userId || user?.id;
    if (!targetUserId || !getStoredToken()) return;

    try {
      const [profileRes, progressRes, activeGoalRes] = await Promise.allSettled([
        profileApi.getProfile(targetUserId),
        progressApi.getUserProgressSummary(targetUserId),
        goalApi.getActiveGoalByUser(targetUserId),
      ]);

      if (profileRes.status === 'fulfilled' && profileRes.value) {
        const p = profileRes.value;
        setUser((prev) => ({
          ...prev,
          id: p.userId,
          name: p.fullName || prev.name,
          fullName: p.fullName || prev.fullName,
          email: p.email || prev.email,
          role: p.role || prev.role,
          bio: p.bio,
          avatarUrl: p.avatarUrl,
          currentStreakDays: p.currentStreakDays,
          totalXp: p.totalXp,
          isAuthenticated: true,
        }));
        setStreakDays(p.currentStreakDays || 1);
        setTotalXp(p.totalXp || 0);
      }

      if (progressRes.status === 'fulfilled' && progressRes.value) {
        const prog = progressRes.value;
        if (prog.currentStreakDays) setStreakDays(prog.currentStreakDays);
        if (prog.totalXp) setTotalXp(prog.totalXp);
        if (prog.activeRoadmapProgress) {
          setRoadmapProgress(prog.activeRoadmapProgress);
        }
      }

      if (activeGoalRes.status === 'fulfilled' && activeGoalRes.value) {
        const g = activeGoalRes.value;
        setActiveGoal(g);
        if (g.goalType) setGoal(g.goalType.toUpperCase());
        if (g.baselineLevel) setLevel(g.baselineLevel.toUpperCase());
        if (g.dailyTime) setDailyTime(g.dailyTime);
      }
    } catch (err) {
      console.warn('Could not refresh full user telemetry:', err);
    }
  }, [user?.id]);

  // Initial telemetry load on mount
  useEffect(() => {
    if (user?.id && user?.isAuthenticated && getStoredToken()) {
      refreshUserData(user.id);
    }
  }, [user?.id, user?.isAuthenticated, refreshUserData]);

  // Auth: Login
  const login = async (email, password) => {
    setLoading(true);
    setError(null);
    try {
      const res = await authApi.login({ email, password });
      // Store signed JWT so subsequent requests carry authentication
      storeToken(res.token);
      const authUser = {
        id: res.id,
        name: res.fullName,
        fullName: res.fullName,
        email: res.email,
        role: res.role,
        isAuthenticated: true,
      };
      setUser(authUser);
      await refreshUserData(res.id);
      return authUser;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Auth: Register
  const register = async (fullName, email, password) => {
    setLoading(true);
    setError(null);
    try {
      const res = await authApi.register({ fullName, email, password });
      // Store signed JWT so subsequent requests carry authentication
      storeToken(res.token);
      const authUser = {
        id: res.id,
        name: res.fullName,
        fullName: res.fullName,
        email: res.email,
        role: res.role,
        isAuthenticated: true,
      };
      setUser(authUser);
      await refreshUserData(res.id);
      return authUser;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Auth: Quick Demo login — authenticates against the real backend using configured demo credentials
  const loginDemoUser = async (customName) => {
    const demoEmail = import.meta.env.VITE_DEMO_EMAIL || 'arjun.patel@bodha.ai';
    const demoPassword = import.meta.env.VITE_DEMO_PASSWORD;

    if (!demoPassword) {
      const msg = `Demo password for ${demoEmail} is not configured in this environment. Please sign in or create an account with your credentials, or set VITE_DEMO_PASSWORD.`;
      setError(msg);
      throw new Error(msg);
    }

    setLoading(true);
    setError(null);
    try {
      const res = await authApi.login({
        email: demoEmail,
        password: demoPassword,
      });
      storeToken(res.token);
      const demoUser = {
        id: res.id,
        name: customName || res.fullName || demoEmail,
        fullName: customName || res.fullName || demoEmail,
        email: res.email,
        role: res.role,
        isDemo: true,
        isAuthenticated: true,
      };
      setUser(demoUser);
      await refreshUserData(res.id);
      return demoUser;
    } catch (err) {
      setError(err.message || 'Demo login failed');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const logout = useCallback(() => {
    // Clear the JWT and all session state
    clearToken();
    try {
      localStorage.removeItem(STORAGE_SESSION_KEY);
      localStorage.removeItem(STORAGE_STATE_KEY);
    } catch {
      // ignore
    }
    setUser({
      id: null,
      name: '',
      fullName: '',
      email: '',
      role: '',
      isAuthenticated: false,
    });
    setActiveGoal(null);
    setActiveAttempt(null);
    setAnswers({});
    setDiagnosticScore(0);
    setAssessmentResult(null);
    setSkillGaps([]);
    setRoadmap(null);
    setRoadmapProgress(null);
  }, []);

  // Register the auto-logout callback with the API layer.
  // When any authenticated request returns 401 (expired/invalid JWT),
  // the API layer clears the token and calls logout automatically.
  useEffect(() => {
    registerOn401Handler(logout);
  }, [logout]);

  // Subject Selection
  const selectSubject = (subj) => {
    setSelectedSubject(subj);
    // Reset transient attempt and answers for new subject calibration
    setActiveAttempt(null);
    setAnswers({});
    setAssessmentResult(null);
    setSkillGaps([]);
    setRoadmap(null);
  };

  // Goal & Calibration
  const saveGoalAndPace = async ({ goal: g, level: l, dailyTime: t }) => {
    const normGoal = (g || goal || 'CAREER').toUpperCase();
    const normLevel = (l || level || 'INTERMEDIATE').toUpperCase();
    const normTime = t || dailyTime || '45m';

    setGoal(normGoal);
    setLevel(normLevel);
    setDailyTime(normTime);

    if (!user?.id || !selectedSubject?.id) {
      return null;
    }

    setLoading(true);
    setError(null);
    try {
      const payload = {
        userId: user.id,
        subjectId: selectedSubject.id,
        goalType: normGoal,
        baselineLevel: normLevel,
        dailyTime: normTime,
      };

      const createdGoal = await goalApi.createGoal(payload);
      setActiveGoal(createdGoal);
      return createdGoal;
    } catch (err) {
      // If goal already exists for this subject, fetch active goal
      if (err.status === 409) {
        try {
          const active = await goalApi.getActiveGoalByUser(user.id);
          setActiveGoal(active);
          return active;
        } catch {
          // ignore
        }
      }
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Assessment Answer Recording
  const recordAnswer = (questionId, optionId) => {
    setAnswers((prev) => ({
      ...prev,
      [questionId]: optionId,
    }));
  };

  // Finalize Assessment & Trigger Skill-Gap Analysis on Backend
  const submitAssessment = async (attemptIdToComplete = null) => {
    const targetAttemptId = attemptIdToComplete || activeAttempt?.id;
    if (!targetAttemptId || !user?.id) {
      throw new Error('No active assessment session found to finalize.');
    }

    setLoading(true);
    setError(null);
    try {
      // 1. Finalize attempt on backend (computes score without exposing answers to React)
      const resultDto = await assessmentApi.completeAssessment(targetAttemptId, user.id);
      setAssessmentResult(resultDto);
      const scorePct = resultDto.scorePercentage != null ? Math.round(Number(resultDto.scorePercentage)) : 0;
      setDiagnosticScore(scorePct);

      // 2. Trigger backend Skill Gap Analysis
      const analysisRes = await skillGapApi.analyzeAttempt(targetAttemptId, user.id);
      if (analysisRes && analysisRes.skillGaps) {
        setSkillGaps(analysisRes.skillGaps);
      } else {
        // Fallback: fetch persisted gaps
        const persisted = await skillGapApi.getUserGoalSkillGaps(user.id, activeGoal?.id || resultDto.learnerGoalId);
        setSkillGaps(persisted || []);
      }

      // 3. Refresh user XP/activity from completion
      await refreshUserData(user.id);
      return resultDto;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Roadmap: Retrieve or Generate
  const fetchOrGenerateRoadmap = async (goalIdToUse = null) => {
    const targetGoalId = goalIdToUse || activeGoal?.id;
    if (!targetGoalId || !user?.id) return null;

    setLoading(true);
    setError(null);
    try {
      let rm = null;
      try {
        // First attempt: fetch existing roadmap for goal
        rm = await roadmapApi.getRoadmapByGoal(targetGoalId, user.id);
      } catch (fetchErr) {
        if (fetchErr.status === 404) {
          // Not found: generate new roadmap from skill gaps
          rm = await roadmapApi.generateRoadmap(targetGoalId, user.id);
        } else {
          throw fetchErr;
        }
      }

      setRoadmap(rm);

      // Sync completed lessons
      if (rm && rm.modules) {
        const completedIds = [];
        rm.modules.forEach((mod) => {
          if (mod.lessons) {
            mod.lessons.forEach((l) => {
              if (l.completed) completedIds.push(l.id);
            });
          }
        });
        if (completedIds.length > 0) {
          setCompletedLessons(completedIds);
        }
      }

      return rm;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Lesson: Start or Complete
  const startLesson = async (lessonId) => {
    if (!user?.id || !lessonId) return;
    try {
      await progressApi.startLesson(lessonId, user.id);
    } catch (err) {
      console.warn('Could not start lesson on backend:', err);
    }
  };

  const completeLesson = async (lessonId) => {
    if (!user?.id || !lessonId) return;
    setLoading(true);
    try {
      await progressApi.completeLesson(lessonId, user.id);

      // Update completed lessons in frontend state
      setCompletedLessons((prev) => (prev.includes(lessonId) ? prev : [...prev, lessonId]));

      // Refresh roadmap and telemetry from backend
      if (activeGoal?.id) {
        await fetchOrGenerateRoadmap(activeGoal.id);
      }
      await refreshUserData(user.id);
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const toggleLessonComplete = async (lessonId) => {
    // If not completed, complete it on backend
    if (!completedLessons.includes(lessonId)) {
      await completeLesson(lessonId);
    } else {
      // Local optimistic toggle for revision
      setCompletedLessons((prev) => prev.filter((id) => id !== lessonId));
    }
  };

  // Dashboard Telemetry & AI Fetch
  const refreshDashboardData = async () => {
    if (!user?.id) return;
    setLoading(true);
    try {
      await refreshUserData(user.id);

      const targetGoalId = activeGoal?.id;
      if (targetGoalId) {
        // Fetch AI recommendations & Next-step in parallel
        const [nextStepRes, recRes, gapsRes] = await Promise.allSettled([
          aiApi.getNextStep(user.id, targetGoalId),
          aiApi.getRecommendations(user.id, targetGoalId),
          skillGapApi.getUserGoalSkillGaps(user.id, targetGoalId),
        ]);

        if (nextStepRes.status === 'fulfilled' && nextStepRes.value) {
          setAiNextStep(nextStepRes.value);
        }
        if (recRes.status === 'fulfilled' && recRes.value) {
          setAiRecommendation(recRes.value);
        }
        if (gapsRes.status === 'fulfilled' && gapsRes.value) {
          setSkillGaps(gapsRes.value);
        }

        // Fetch roadmap
        await fetchOrGenerateRoadmap(targetGoalId);
      }
    } catch (err) {
      console.warn('Error refreshing dashboard data:', err);
    } finally {
      setLoading(false);
    }
  };

  const resetJourney = () => {
    try {
      localStorage.removeItem(STORAGE_STATE_KEY);
    } catch {
      // ignore
    }
    setSelectedSubject(DEFAULT_FALLBACK_SUBJECT);
    setGoal('CAREER');
    setLevel('INTERMEDIATE');
    setDailyTime('45m');
    setActiveGoal(null);
    setActiveAttempt(null);
    setAnswers({});
    setDiagnosticScore(0);
    setAssessmentResult(null);
    setSkillGaps([]);
    setRoadmap(null);
    setRoadmapProgress(null);
    setCompletedLessons([]);
    setStreakDays(1);
    setTotalXp(0);
    setAiRecommendation(null);
    setAiNextStep(null);
  };

  return (
    <LearnerContext.Provider
      value={{
        user,
        setUser,
        login,
        register,
        logout,
        loginDemoUser,
        refreshUserData,
        selectedSubject,
        selectSubject,
        goal,
        level,
        dailyTime,
        activeGoal,
        saveGoalAndPace,
        activeAttempt,
        setActiveAttempt,
        answers,
        recordAnswer,
        diagnosticScore,
        setDiagnosticScore,
        assessmentResult,
        submitAssessment,
        skillGaps,
        setSkillGaps,
        roadmap,
        setRoadmap,
        roadmapProgress,
        fetchOrGenerateRoadmap,
        completedLessons,
        startLesson,
        completeLesson,
        toggleLessonComplete,
        streakDays,
        totalXp,
        aiRecommendation,
        aiNextStep,
        refreshDashboardData,
        isQuizModalOpen,
        setIsQuizModalOpen,
        resetJourney,
        loading,
        error,
        setError,
      }}
    >
      {children}
    </LearnerContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useLearner() {
  const context = useContext(LearnerContext);
  if (!context) {
    throw new Error('useLearner must be used within a LearnerProvider');
  }
  return context;
}
