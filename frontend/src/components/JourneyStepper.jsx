import React from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { Check } from 'lucide-react';

const STEPS = [
  { id: 1, name: 'Auth', path: '/auth' },
  { id: 2, name: 'Subject', path: '/choose-subject' },
  { id: 3, name: 'Goal & Time', path: '/goal-setting' },
  { id: 4, name: 'Assessment', path: '/assessment' },
  { id: 5, name: 'Skill-Gap', path: '/skill-gap' },
  { id: 6, name: 'Roadmap', path: '/roadmap' },
  { id: 7, name: 'Dashboard', path: '/dashboard' },
];

export default function JourneyStepper() {
  const location = useLocation();
  const currentStepIndex = STEPS.findIndex((s) => s.path === location.pathname);

  return (
    <div className="bg-[#0a0f1d]/90 border-b border-slate-800/80 sticky top-16 z-40 backdrop-blur-xl transition-all">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-2.5">
        {/* Desktop Stepper */}
        <div className="hidden md:flex items-center justify-between gap-2 overflow-x-auto py-0.5">
          {STEPS.map((step, idx) => {
            const isCompleted = currentStepIndex > idx;
            const isCurrent = currentStepIndex === idx;

            return (
              <React.Fragment key={step.id}>
                <NavLink
                  to={step.path}
                  className={`flex items-center gap-2 text-xs font-semibold px-3 py-1.5 rounded-xl transition-all duration-150 whitespace-nowrap cursor-pointer select-none ${
                    isCurrent
                      ? 'bg-blue-600 text-white shadow-md shadow-blue-500/30 ring-1 ring-blue-400'
                      : isCompleted
                      ? 'text-emerald-400 hover:bg-slate-800/60'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
                  }`}
                >
                  <span
                    className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold font-mono transition-colors ${
                      isCurrent
                        ? 'bg-white text-blue-700 shadow-sm'
                        : isCompleted
                        ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/40'
                        : 'bg-slate-800 text-slate-400 border border-slate-700'
                    }`}
                  >
                    {isCompleted ? <Check className="w-3 h-3 stroke-[3]" /> : step.id}
                  </span>
                  <span>{step.name}</span>
                </NavLink>

                {idx < STEPS.length - 1 && (
                  <div
                    className={`h-0.5 flex-1 min-w-4 rounded-full transition-colors ${
                      currentStepIndex > idx
                        ? 'bg-gradient-to-r from-emerald-500/60 to-blue-500/60'
                        : 'bg-slate-800/80'
                    }`}
                  />
                )}
              </React.Fragment>
            );
          })}
        </div>

        {/* Mobile Stepper (Compact view) */}
        <div className="flex md:hidden items-center justify-between text-xs">
          <div className="flex items-center gap-2">
            <span className="px-2 py-0.5 rounded-md bg-blue-500/20 text-blue-400 font-mono font-bold border border-blue-500/30 text-[11px]">
              Step {currentStepIndex >= 0 ? currentStepIndex + 1 : 1} of {STEPS.length}
            </span>
            <span className="font-semibold text-white">
              {STEPS[currentStepIndex]?.name || 'Learning Pipeline'}
            </span>
          </div>
          <div className="flex items-center gap-3 font-medium">
            {currentStepIndex > 0 && (
              <NavLink
                to={STEPS[currentStepIndex - 1]?.path || '/'}
                className="text-xs text-slate-400 hover:text-white transition-colors"
              >
                Back
              </NavLink>
            )}
            {currentStepIndex < STEPS.length - 1 && (
              <NavLink
                to={STEPS[currentStepIndex + 1]?.path || '/dashboard'}
                className="text-xs font-semibold text-blue-400 hover:text-blue-300 transition-colors"
              >
                Next
              </NavLink>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
