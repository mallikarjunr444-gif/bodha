# BODHA (बोध) — AI Personalization Architecture

## 1. Pedagogical Vision: Moving Beyond "ChatGPT Wrappers"

Most consumer "AI education apps" simply act as conversational chat interfaces around an LLM. While conversational interfaces can explain concepts, they suffer from fundamental pedagogical flaws:
1. **No Persistent State:** The model does not retain structured memory of what the learner has already mastered, failed, or skipped over weeks of study.
2. **Illusion of Competency:** Reading an AI-generated explanation gives learners a false sense of mastery without diagnostic evaluation or spaced reinforcement.
3. **Unanchored Hallucination:** The model invents arbitrary curriculum paths disconnected from the learner's actual time budget and assessment metrics.

**BODHA is architected differently:**
BODHA uses an **AI-Assisted, Database-Grounded Architecture**. The relational database (PostgreSQL) is the authoritative source of truth for the learner's journey. The AI engine acts as an analytical copilot that operates on strict, verified telemetry:

```
[Diagnostic Assessment] ──► [Relational Skill Gaps] ──► [Personalized Roadmap] 
                                                               │
                                                               ▼
[Structured Recommendations] ◄── [Prompt Builder] ◄── [Learner Context Builder]
```

---

## 2. Core Architectural Components

### 2.1 `LearnerAiContextBuilder` (Telemetry Grounding)
Located in `com.bodha.service.ai.LearnerAiContextBuilder`.  
Before invoking any AI capability, this service queries the relational database and synthesizes a structured snapshot of the learner's state:
- **Active Goal:** Track name, baseline level (`beginner`, `intermediate`, `advanced`), target deadline, and daily time commitment (e.g., 30 mins).
- **Assessment History:** Baseline diagnostic test score, total attempts, completion timestamp.
- **Skill Gaps:** Granular classification of each evaluated skill into `MASTERED`, `NEEDS_REVIEW`, or `CRITICAL_GAP`.
- **Roadmap Position:** Active module, current completed lessons count, next uncompleted lesson in sequence.
- **Gamification & Consistency:** Current streak days, total XP earned, recent activity logs.

Crucially, **no private credentials or sensitive personal identifiers** (passwords, tokens, emails) are ever injected into the AI context.

### 2.2 `AiPromptBuilder` (Constrained Pedagogical Prompts)
Located in `com.bodha.service.ai.AiPromptBuilder`.  
Transforms the telemetry snapshot into strict system and user prompts:
- Enforces an educational role (pedagogical mentor, Socratic guide).
- Enforces structured JSON output matching strict Java DTO records.
- Forbids trivial answer disclosure: when tutoring in lesson assistance, the AI must use Socratic inquiry rather than solving coding problems directly for the student.

### 2.3 `AiProvider` Interface (Decoupled Provider Architecture)
Located in `com.bodha.service.ai.AiProvider`.  
Defines the clean contract for AI synthesis:
```java
public interface AiProvider {
    String generateRecommendation(String prompt);
    String generateLessonAssistance(String prompt);
    String generateNextStep(String prompt);
    String getProviderName();
}
```

This clean abstraction allows swapping or chaining AI models without modifying business logic.

### 2.4 `MockAiProvider` (Deterministic Local & Fallback Provider)
Located in `com.bodha.service.ai.provider.MockAiProvider`.
- Default provider for local development, CI/CD automated testing, and safe offline demonstrations.
- Evaluates skill gaps and roadmap states using deterministic heuristic rules.
- Guarantees zero latency, zero API costs, and 100% test reproducibility.

### 2.5 `GeminiAiProvider` (Google AI Studio Production Integration)
Located in `com.bodha.service.ai.provider.GeminiAiProvider`.
- Connects directly to Google Generative Language API using Google Gemini models (`gemini-1.5-flash`, `gemini-1.5-pro`).
- Uses standard Spring `RestClient` with configured timeouts (`AI_TIMEOUT_MS`, default 5000ms).
- Authenticates securely via backend environment variable `AI_API_KEY`. The API key is strictly backend-only and never exposed to the client or browser.

### 2.6 `DeterministicAiFallback` (Fault-Tolerant Resilience)
Located in `com.bodha.service.ai.DeterministicAiFallback`.  
Even in production with live Gemini enabled, external LLM calls can fail due to upstream rate limits, network timeouts, or malformed JSON responses.  
If any exception occurs in `GeminiAiProvider`, `AiPersonalizationService` instantly activates `DeterministicAiFallback`:
- It catches the exception, logs it internally, and returns a high-fidelity pedagogical response derived deterministically from the database state.
- The UI displays the recommendation with a `fallbackUsed: true` metadata flag.
- The user **never experiences an error, blank screen, or broken learning flow**.

---

## 3. Supported AI Endpoints & DTOs

### 3.1 Immediate Next Step (`GET /api/ai/next-step?goalId={id}`)
Synthesizes the single most effective action the student should take right now.
- Considers active module, uncompleted lessons, and highest-severity skill gap.
- Returns `AiNextStepResponseDto`:
  - `title`: e.g., "Next Step: Spring Beans & ApplicationContext"
  - `summary`: Concise motivational context
  - `nextLessonId`: Direct foreign key to `roadmap_lessons.id`
  - `suggestedAction`: Concrete instruction (e.g. "Complete hands-on exercise on dependency injection")
  - `reason`: Pedagogical explanation of why this step is sequenced now

### 3.2 Personalized Remediation Recommendation (`GET /api/ai/recommendations?goalId={id}`)
Deep diagnostic remediation plan targeting identified skill gaps.
- Prioritizes skills marked `CRITICAL_GAP` or `NEEDS_REVIEW`.
- Returns `AiRecommendationResponseDto`:
  - `priority`: `HIGH`, `MEDIUM`, or `LOW`
  - `relatedSkill`: Exact skill competency
  - `learningApproach`: Recommended study method (`hands-on`, `conceptual`, `spaced-repetition`)
  - `practiceSuggestions`: Array of targeted practice activities

### 3.3 Socratic Lesson Tutoring (`POST /api/ai/lessons/{id}/assist`)
Real-time pedagogical assistant for an active lesson.
- Injects lesson title, module scope, and related skill competency.
- Employs Socratic inquiry to guide the learner through difficult concepts without giving away solutions.

---

## 4. Privacy, Safety & Cost Control

1. **Zero Secret Leakage:** Prompts only contain curriculum identifiers, skill names, and progress percentages. Passwords, hashes, and private tokens are excluded by design.
2. **Token Bucket Rate Limiting:** `/api/ai/**` endpoints are throttled to 20 requests/minute per authenticated user, preventing malicious or accidental resource exhaustion.
3. **Cost Optimization:** Prompts are tightly structured (typically <600 tokens) with strict max token limits on generation (`maxOutputTokens: 1024`).
4. **Environment Controlled:** Toggled via `AI_PROVIDER=mock` vs `AI_PROVIDER=gemini`.
