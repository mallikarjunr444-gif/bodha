import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { LayoutDashboard, LogOut, RotateCcw, ArrowRight, Flame } from 'lucide-react';
import { useLearner } from '../context/LearnerContext';
import BrandLogo from './common/BrandLogo';
import Button from './common/Button';

export default function Navbar() {
  const location = useLocation();
  const { user, streakDays, resetJourney, logout } = useLearner();
  const isLanding = location.pathname === '/';

  const navLinks = [
    { name: 'Overview', path: '/' },
    { name: 'Choose Skill', path: '/choose-subject' },
    { name: 'Roadmap', path: '/roadmap' },
    { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
  ];

  return (
    <header className="sticky top-0 z-50 backdrop-blur-xl bg-[#080b11]/85 border-b border-slate-800/80 transition-colors">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between gap-4">
        {/* Brand Container with reserved logo slot */}
        <div className="flex items-center gap-6">
          <BrandLogo size="md" showWordmark showTagline={false} />
        </div>

        {/* Dynamic Center Navigation */}
        <nav className="hidden md:flex items-center gap-1 bg-[#0e1526]/80 p-1 rounded-xl border border-slate-800/90 text-xs font-semibold">
          {navLinks.map((item) => {
            const isActive = location.pathname === item.path;
            const Icon = item.icon;

            return (
              <Link
                key={item.path}
                to={item.path}
                className={`px-3.5 py-1.5 rounded-lg transition-all duration-150 flex items-center gap-1.5 ${
                  isActive
                    ? 'bg-blue-600 text-white shadow-sm shadow-blue-500/20 font-bold'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                }`}
              >
                {Icon && <Icon className="w-3.5 h-3.5" />}
                <span>{item.name}</span>
              </Link>
            );
          })}
        </nav>

        {/* Right Actions & Learner Profile */}
        <div className="flex items-center gap-3">
          {!isLanding && (
            <button
              type="button"
              onClick={resetJourney}
              title="Reset journey state to defaults"
              className="hidden sm:flex items-center gap-1.5 text-xs text-slate-400 hover:text-slate-200 px-2.5 py-1.5 rounded-lg border border-slate-800 hover:bg-slate-900 transition-colors cursor-pointer"
            >
              <RotateCcw className="w-3.5 h-3.5 text-slate-400" />
              <span>Reset State</span>
            </button>
          )}

          {user?.isAuthenticated ? (
            <>
              <Link
              to="/dashboard"
              className="flex items-center gap-2.5 text-xs font-semibold bg-[#0e1526] border border-slate-700/80 hover:border-blue-500/60 text-slate-200 px-3 py-1.5 rounded-xl transition-all shadow-sm hover:shadow-blue-500/10"
            >
              <div className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              <span className="hidden sm:inline font-medium">
                {user.fullName || user.name || 'Learner'}
              </span>
              {streakDays > 0 && (
                <span className="flex items-center gap-0.5 text-[10px] font-mono text-amber-400 bg-amber-500/10 px-1.5 py-0.5 rounded border border-amber-500/20">
                  <Flame className="w-3 h-3 fill-amber-400 text-amber-400" />
                  <span>{streakDays}d</span>
                </span>
              )}
            </Link>
            {user?.isAuthenticated && (
              <button
                type="button"
                onClick={logout}
                title="Sign out of current session"
                className="hidden sm:flex items-center gap-1.5 text-xs text-slate-400 hover:text-rose-400 px-2.5 py-1.5 rounded-lg border border-slate-800 hover:bg-slate-900 transition-colors cursor-pointer"
              >
                <LogOut className="w-3.5 h-3.5" />
                <span>Sign Out</span>
              </button>
            )}
          </>
          ) : (
            <Link
              to="/auth"
              className="text-xs sm:text-sm font-medium text-slate-300 hover:text-white px-3 py-1.5 rounded-lg transition-colors"
            >
              Sign In
            </Link>
          )}

          <Button
            size="sm"
            variant="primary"
            onClick={() => (window.location.href = isLanding ? '/auth' : '/dashboard')}
            rightIcon={<ArrowRight className="w-3.5 h-3.5" />}
          >
            {isLanding ? 'Start Journey' : 'Dashboard'}
          </Button>
        </div>
      </div>
    </header>
  );
}
