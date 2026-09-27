import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { LearnerProvider } from './context/LearnerContext';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import JnanoraSplash from './components/common/JnanoraSplash';

// 8 Core Journey Pages
import LandingPage from './pages/LandingPage';
import AuthPage from './pages/AuthPage';
import ChooseSubjectPage from './pages/ChooseSubjectPage';
import GoalSettingPage from './pages/GoalSettingPage';
import AssessmentPage from './pages/AssessmentPage';
import SkillGapPage from './pages/SkillGapPage';
import RoadmapPage from './pages/RoadmapPage';
import DashboardPage from './pages/DashboardPage';

export default function App() {
  const [showSplash, setShowSplash] = useState(true);

  return (
    <LearnerProvider>
      <BrowserRouter>
        {showSplash && (
          <JnanoraSplash onFinish={() => setShowSplash(false)} />
        )}
        <div className="min-h-screen bg-[#080d14] text-slate-100 flex flex-col font-sans selection:bg-sky-600 selection:text-white">
          <Navbar />
          <div className="flex-1 flex flex-col">
            <Routes>
              {/* 1. Landing Page */}
              <Route path="/" element={<LandingPage />} />

              {/* 2. Sign Up / Login */}
              <Route path="/auth" element={<AuthPage />} />

              {/* 3. Choose Subject or Skill */}
              <Route path="/choose-subject" element={<ChooseSubjectPage />} />

              {/* 4. Set Learning Goal, Level, and Available Time */}
              <Route path="/goal-setting" element={<GoalSettingPage />} />

              {/* 5. Initial Diagnostic Assessment */}
              <Route path="/assessment" element={<AssessmentPage />} />

              {/* 6. Skill-Gap Analysis */}
              <Route path="/skill-gap" element={<SkillGapPage />} />

              {/* 7. Personalized Learning Roadmap */}
              <Route path="/roadmap" element={<RoadmapPage />} />

              {/* 8. Learning Dashboard */}
              <Route path="/dashboard" element={<DashboardPage />} />

              {/* Fallback */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </div>
          <Footer />
        </div>
      </BrowserRouter>
    </LearnerProvider>
  );
}
