/**
 * Centralized API Client Layer for Jnanora
 *
 * Provides typed, clean REST invocation abstractions across all 8 modules.
 * Configured via Vite environment variable VITE_API_BASE_URL.
 *
 * Module N: All authenticated requests automatically carry the signed JWT
 * token retrieved from localStorage. Auth endpoints (login/register) are
 * public and do not send a token.
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/** localStorage key for the signed JWT token */
const TOKEN_STORAGE_KEY = 'bodha_jwt_token';

export class ApiError extends Error {
  constructor(message, status, details = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.details = details;
  }
}

// -----------------------------------------------------------------------
// Token management helpers — used by LearnerContext for login/logout
// -----------------------------------------------------------------------
export function storeToken(token) {
  try {
    if (token) {
      localStorage.setItem(TOKEN_STORAGE_KEY, token);
    } else {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
    }
  } catch {
    // ignore storage errors
  }
}

export function getStoredToken() {
  try {
    return localStorage.getItem(TOKEN_STORAGE_KEY);
  } catch {
    return null;
  }
}

export function clearToken() {
  try {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
  } catch {
    // ignore
  }
}

// -----------------------------------------------------------------------
// Optional 401 callback — LearnerContext registers this so the API layer
// can trigger logout when the server rejects a token.
// -----------------------------------------------------------------------
let _on401 = null;
export function registerOn401Handler(handler) {
  _on401 = handler;
}

/**
 * Universal fetch wrapper that standardizes headers, error handling, and JSON serialization.
 * Automatically attaches `Authorization: Bearer <token>` for authenticated requests.
 * Pass `options.skipAuth = true` to suppress token injection (login/register endpoints).
 */
async function apiFetch(endpoint, options = {}) {
  const url = endpoint.startsWith('http') ? endpoint : `${API_BASE_URL}${endpoint}`;

  const headers = {
    'Content-Type': 'application/json',
    Accept: 'application/json',
    ...options.headers,
  };

  // Inject JWT for all authenticated requests
  if (!options.skipAuth) {
    const token = getStoredToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
  }

  const { skipAuth: _skip, ...restOptions } = options;
  const config = {
    ...restOptions,
    headers,
  };

  if (config.body && typeof config.body === 'object' && !(config.body instanceof FormData)) {
    config.body = JSON.stringify(config.body);
  }

  let response;
  try {
    response = await fetch(url, config);
  } catch (networkError) {
    throw new ApiError(
      `Network connection failed. Unable to connect to Jnanora API at ${API_BASE_URL}. Ensure the backend server is running.`,
      0,
      networkError
    );
  }

  // Handle 204 No Content
  if (response.status === 204) {
    return null;
  }

  // Handle 401 Unauthorized — token expired or invalid
  if (response.status === 401 && !options.skipAuth) {
    clearToken();
    if (_on401) _on401();
    throw new ApiError('Your session has expired. Please log in again.', 401, null);
  }

  const contentType = response.headers.get('content-type');
  const isJson = contentType && contentType.includes('application/json');

  let data = null;
  if (isJson) {
    try {
      data = await response.json();
    } catch {
      data = null;
    }
  } else {
    try {
      data = await response.text();
    } catch {
      data = null;
    }
  }

  if (!response.ok) {
    let errorMessage = `Request failed with status ${response.status}`;

    if (data && typeof data === 'object') {
      if (data.message) {
        errorMessage = data.message;
      } else if (data.errors && typeof data.errors === 'object') {
        const fieldErrors = Object.entries(data.errors)
          .map(([field, msg]) => `${field}: ${msg}`)
          .join(', ');
        errorMessage = `Validation error: ${fieldErrors}`;
      } else if (data.error) {
        errorMessage = data.error;
      }
    } else if (typeof data === 'string' && data.length > 0) {
      errorMessage = data;
    }

    throw new ApiError(errorMessage, response.status, data);
  }

  return data;
}

// ----------------------------------------------------
// 1. Authentication API (Module C)
// ----------------------------------------------------
export const authApi = {
  register: (payload) =>
    apiFetch('/api/auth/register', {
      method: 'POST',
      body: payload,
      skipAuth: true,
    }),

  login: (payload) =>
    apiFetch('/api/auth/login', {
      method: 'POST',
      body: payload,
      skipAuth: true,
    }),
};

// ----------------------------------------------------
// 2. Domain & Subject Catalog API (Module B)
// ----------------------------------------------------
export const catalogApi = {
  getDomains: () => apiFetch('/api/domains', { skipAuth: true }),

  getSubjects: (domainId) => {
    const query = domainId && domainId !== 'all' ? `?domainId=${encodeURIComponent(domainId)}` : '';
    return apiFetch(`/api/subjects${query}`, { skipAuth: true });
  },

  getPopularSubjects: () => apiFetch('/api/subjects/popular', { skipAuth: true }),

  getSubjectById: (subjectId) =>
    apiFetch(`/api/subjects/${encodeURIComponent(subjectId)}`, { skipAuth: true }),

  getSubjectSkills: (subjectId) =>
    apiFetch(`/api/subjects/${encodeURIComponent(subjectId)}/skills`, { skipAuth: true }),
};

// ----------------------------------------------------
// 2b. Health Telemetry API
// ----------------------------------------------------
export const healthApi = {
  getHealth: () => apiFetch('/api/health', { skipAuth: true }),
};

// ----------------------------------------------------
// 3. Learner Profile API (Module C)
// ----------------------------------------------------
export const profileApi = {
  getProfile: (userId) => apiFetch(`/api/profile/${userId}`),

  updateProfile: (userId, payload) =>
    apiFetch(`/api/profile/${userId}`, {
      method: 'PUT',
      body: payload,
    }),
};

// ----------------------------------------------------
// 4. Learner Goal API (Module D)
// ----------------------------------------------------
export const goalApi = {
  createGoal: (payload) =>
    apiFetch('/api/goals', {
      method: 'POST',
      body: payload,
    }),

  getGoalsByUser: (userId) => apiFetch(`/api/goals/user/${userId}`),

  getActiveGoalByUser: (userId) => apiFetch(`/api/goals/user/${userId}/active`),

  getGoalById: (goalId) => apiFetch(`/api/goals/${goalId}`),

  updateGoal: (goalId, payload) =>
    apiFetch(`/api/goals/${goalId}`, {
      method: 'PUT',
      body: payload,
    }),
};

// ----------------------------------------------------
// 5. Assessment Engine API (Module D)
// ----------------------------------------------------
export const assessmentApi = {
  getAssessmentsBySubject: (subjectId) =>
    apiFetch(`/api/assessments/subject/${encodeURIComponent(subjectId)}`),

  getDiagnosticAssessment: (subjectId) =>
    apiFetch(`/api/assessments/subject/${encodeURIComponent(subjectId)}/diagnostic`),

  getAssessmentById: (assessmentId) => apiFetch(`/api/assessments/${assessmentId}`),

  startAttempt: (assessmentId, { userId, learnerGoalId }) =>
    apiFetch(`/api/assessments/${assessmentId}/attempts`, {
      method: 'POST',
      body: { userId, learnerGoalId },
    }),

  getAttempt: (attemptId, userId) => {
    const query = userId ? `?userId=${userId}` : '';
    return apiFetch(`/api/attempts/${attemptId}${query}`);
  },

  submitResponse: (attemptId, { userId, questionId, selectedOptionId }) =>
    apiFetch(`/api/attempts/${attemptId}/responses`, {
      method: 'POST',
      body: { userId, questionId, selectedOptionId },
    }),

  completeAssessment: (attemptId, userId) => {
    const query = userId ? `?userId=${userId}` : '';
    return apiFetch(`/api/attempts/${attemptId}/complete${query}`, {
      method: 'POST',
    });
  },

  getAssessmentResult: (attemptId, userId) => {
    const query = userId ? `?userId=${userId}` : '';
    return apiFetch(`/api/attempts/${attemptId}/result${query}`);
  },
};

// ----------------------------------------------------
// 6. Skill Gap Engine API (Module E)
// ----------------------------------------------------
export const skillGapApi = {
  analyzeAttempt: (attemptId, userId) => {
    const query = userId ? `?userId=${userId}` : '';
    return apiFetch(`/api/attempts/${attemptId}/skill-gaps/analyze${query}`, {
      method: 'POST',
    });
  },

  getSkillGapsByUser: (userId) => apiFetch(`/api/skill-gaps/user/${userId}`),

  getSkillGapsByUserAndSubject: (userId, subjectId) =>
    apiFetch(`/api/skill-gaps/user/${userId}/subject/${encodeURIComponent(subjectId)}`),

  getSkillGapsByUserAndGoal: (userId, goalId) =>
    apiFetch(`/api/skill-gaps/user/${userId}/goal/${goalId}`),
};

// ----------------------------------------------------
// 7. Personalized Roadmap API (Module F)
// ----------------------------------------------------
export const roadmapApi = {
  generateRoadmap: (goalId, userId) => {
    const query = userId ? `?userId=${userId}` : '';
    return apiFetch(`/api/goals/${goalId}/roadmap/generate${query}`, {
      method: 'POST',
    });
  },

  getRoadmapByGoal: (goalId, userId) => {
    const query = userId ? `?userId=${userId}` : '';
    return apiFetch(`/api/goals/${goalId}/roadmap${query}`);
  },

  getRoadmapById: (roadmapId, userId) => {
    const query = userId ? `?userId=${userId}` : '';
    return apiFetch(`/api/roadmaps/${roadmapId}${query}`);
  },
};

// ----------------------------------------------------
// 8. Lesson Progress & Study Tracking API (Module G)
// ----------------------------------------------------
export const progressApi = {
  startLesson: (lessonId, userId) => {
    const query = userId ? `?userId=${userId}` : '';
    return apiFetch(`/api/lessons/${lessonId}/start${query}`, {
      method: 'POST',
    });
  },

  updateProgress: (lessonId, userId) => {
    const query = userId ? `?userId=${userId}` : '';
    return apiFetch(`/api/lessons/${lessonId}/progress${query}`, {
      method: 'POST',
    });
  },

  completeLesson: (lessonId, userId) => {
    const query = userId ? `?userId=${userId}` : '';
    return apiFetch(`/api/lessons/${lessonId}/complete${query}`, {
      method: 'POST',
    });
  },

  getLessonProgress: (lessonId, userId) => {
    const query = userId ? `?userId=${userId}` : '';
    return apiFetch(`/api/lessons/${lessonId}/progress${query}`);
  },

  getRoadmapProgress: (roadmapId, userId) => {
    const query = userId ? `?userId=${userId}` : '';
    return apiFetch(`/api/roadmaps/${roadmapId}/progress${query}`);
  },

  getUserProgressSummary: (userId) => apiFetch(`/api/progress/user/${userId}`),
};

// ----------------------------------------------------
// 9. AI Personalization Engine API (Module H)
// ----------------------------------------------------
export const aiApi = {
  getRecommendations: (userId, goalId) =>
    apiFetch(`/api/ai/recommendations?userId=${userId}&goalId=${goalId}`, {
      method: 'POST',
    }),

  getLessonAssistance: (lessonId, userId, payload = {}) =>
    apiFetch(`/api/ai/lessons/${lessonId}/assist?userId=${userId}`, {
      method: 'POST',
      body: payload,
    }),

  getNextStep: (userId, goalId) =>
    apiFetch(`/api/ai/next-step?userId=${userId}&goalId=${goalId}`),
};

export default {
  auth: authApi,
  catalog: catalogApi,
  health: healthApi,
  profile: profileApi,
  goal: goalApi,
  assessment: assessmentApi,
  skillGap: skillGapApi,
  roadmap: roadmapApi,
  progress: progressApi,
  ai: aiApi,
};
