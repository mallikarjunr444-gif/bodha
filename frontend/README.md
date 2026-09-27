# Jnanora — Frontend Client

The frontend client for **Jnanora**, an AI-powered personalized learning and skill-development platform.

## Tech Stack
- **Framework:** [React 19](https://react.dev/)
- **Build Tool:** [Vite 8](https://vite.dev/)
- **Client Routing:** [React Router v7](https://reactrouter.com/) (`react-router-dom`)
- **Styling:** [Tailwind CSS v4](https://tailwindcss.com/)
- **Icons:** [Lucide React](https://lucide.dev/)
- **Linter:** [Oxlint](https://oxc.rs/)

## Navigation & 8-Step Core Journey Routes

| Step | Screen | Route | Description |
| :--- | :--- | :--- | :--- |
| **1** | **Landing Page** | `/` | Hero value proposition, 8-step journey overview, and "Why Jnanora vs ChatGPT" differentiator comparison. |
| **2** | **Sign Up / Login** | `/auth` | Mock authentication with tab toggle and 1-click "Continue as Demo Learner". |
| **3** | **Choose Subject or Skill** | `/choose-subject` | Multi-domain filter tabs (Software, Math, Languages, Business), search, and custom arbitrary skill entry. |
| **4** | **Goal, Level & Time** | `/goal-setting` | Target calibration (Career, Academic, Project, Mastery), baseline experience level, and daily commitment. |
| **5** | **Diagnostic Assessment** | `/assessment` | Interactive 5-question test tailored to the chosen discipline with skill tagging and scoring. |
| **6** | **Skill-Gap Analysis** | `/skill-gap` | Baseline readiness score, Mastered Prerequisites vs Identified Skill Gaps, and curriculum recommendations. |
| **7** | **Personalized Roadmap** | `/roadmap` | Sequenced milestones with prerequisite checks, time estimates, and lesson breakdowns. |
| **8** | **Learning Dashboard** | `/dashboard` | Persistent learner view with streak tracking, active lesson focus, interactive practice quiz modal, and adaptive recommendation alert. |

## Project Structure
```
frontend/
├── index.html                  # HTML entry point with metadata and fonts
├── package.json                # Dependencies and npm scripts
├── vite.config.js              # Vite bundler configuration
├── public/                     # Static public assets
└── src/
    ├── components/             # Reusable UI components
    │   ├── Navbar.jsx          # Dynamic navigation with reset controls
    │   ├── Footer.jsx          # Platform footer and stack overview
    │   └── JourneyStepper.jsx  # Interactive breadcrumb stepper for stages 1-7
    ├── context/
    │   └── LearnerContext.jsx  # Persistent mock learner state with localStorage sync
    ├── data/
    │   └── mockData.js         # Multi-domain subjects, diagnostic tests, skill gaps, and roadmaps
    ├── pages/                  # View-level page components (8 journey screens)
    │   ├── LandingPage.jsx
    │   ├── AuthPage.jsx
    │   ├── ChooseSubjectPage.jsx
    │   ├── GoalSettingPage.jsx
    │   ├── AssessmentPage.jsx
    │   ├── SkillGapPage.jsx
    │   ├── RoadmapPage.jsx
    │   └── DashboardPage.jsx
    ├── App.jsx                 # Route orchestrator with BrowserRouter & LearnerProvider
    ├── main.jsx                # Application bootstrap entry point
    └── index.css               # Global stylesheet and Tailwind CSS layer
```

## Running Locally

1. **Navigate to the frontend directory:**
   ```bash
   cd frontend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Start the development server:**
   ```bash
   npm run dev
   ```
   Open your browser at `http://localhost:5173`.

4. **Lint the code (0 errors, 0 warnings):**
   ```bash
   npm run lint
   ```

5. **Build for production:**
   ```bash
   npm run build
   ```
