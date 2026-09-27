import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Sparkles, ArrowRight, ShieldCheck, UserCheck, Lock, Mail, User } from 'lucide-react';
import { useLearner } from '../context/LearnerContext';
import JourneyStepper from '../components/JourneyStepper';
import ErrorMessage from '../components/common/ErrorMessage';
import Button from '../components/common/Button';
import Card from '../components/common/Card';
import Badge from '../components/common/Badge';
import BrandLogo from '../components/common/BrandLogo';

export default function AuthPage() {
  // Demo access is enabled in development (VITE_ENABLE_DEMO_ACCESS=true).
  // Set VITE_ENABLE_DEMO_ACCESS=false in production to hide demo shortcuts.
  const demoEnabled = import.meta.env.VITE_ENABLE_DEMO_ACCESS !== 'false';

  const [isSignUp, setIsSignUp] = useState(false);
  const [fullName, setFullName] = useState(demoEnabled ? 'Alex Learner' : '');
  const [email, setEmail] = useState(demoEnabled ? 'alex@jnanora.ai' : '');
  const [password, setPassword] = useState(demoEnabled ? 'Password123!' : '');
  const [loading, setLoading] = useState(false);
  const [formError, setFormError] = useState(null);

  const { login, register, loginDemoUser } = useLearner();
  const navigate = useNavigate();

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setFormError(null);
    setLoading(true);

    try {
      if (isSignUp) {
        if (!fullName.trim()) {
          throw new Error('Please enter your full name.');
        }
        await register(fullName.trim(), email.trim(), password);
      } else {
        await login(email.trim(), password);
      }
      navigate('/choose-subject');
    } catch (err) {
      setFormError(err.message || 'Authentication failed. Please verify your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleDemoQuickAccess = async () => {
    setLoading(true);
    try {
      await loginDemoUser('Arjun K. Patel');
      navigate('/choose-subject');
    } catch (err) {
      setFormError(err.message || 'Unable to connect to demo account.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] flex flex-col bg-[#080b11]">
      <JourneyStepper />

      <div className="flex-1 flex items-center justify-center px-4 py-12">
        <div className="w-full max-w-md space-y-6">
          {/* Header Card */}
          <div className="text-center space-y-3">
            <div className="flex justify-center mb-2">
              <BrandLogo size="lg" showWordmark={true} asLink={true} />
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              {isSignUp ? 'Create Learner Profile' : 'Welcome Back to Jnanora'}
            </h1>
            <p className="text-xs sm:text-sm text-slate-400 max-w-xs mx-auto">
              {isSignUp
                ? 'Sign up to build your persistent competency graph and adaptive roadmap'
                : 'Sign in to access your customized learning roadmaps and history'}
            </p>
          </div>

          {/* Quick Demo Login Banner — only shown when VITE_ENABLE_DEMO_ACCESS != 'false' */}
          {demoEnabled && (
          <Card variant="ai" className="p-4 sm:p-5 space-y-3">
            <div className="flex items-center justify-between">
              <Badge variant="primary" size="xs" icon={<Sparkles className="w-3 h-3" />}>
                One-Click Evaluation
              </Badge>
              <Badge variant="ai" size="xs">
                PostgreSQL Seeded
              </Badge>
            </div>
            <p className="text-xs text-slate-300 leading-relaxed">
              Explore the full learning pipeline as verified learner <strong>Arjun K. Patel</strong> (User ID: 1).
            </p>
            <Button
              size="sm"
              variant="primary"
              className="w-full"
              isLoading={loading}
              onClick={handleDemoQuickAccess}
              leftIcon={<UserCheck className="w-4 h-4" />}
              rightIcon={<ArrowRight className="w-3.5 h-3.5" />}
            >
              Continue as Demo Learner (Arjun Patel)
            </Button>
          </Card>
          )}

          {/* Error Message */}
          {formError && (
            <ErrorMessage
              title={isSignUp ? 'Registration Error' : 'Sign In Error'}
              message={formError}
            />
          )}

          {/* Form Card */}
          <Card variant="elevated" className="p-6 sm:p-8 space-y-6 shadow-2xl">
            {/* Toggle Tabs */}
            <div className="grid grid-cols-2 p-1 bg-[#0a0f1d] rounded-xl border border-slate-800 text-xs font-semibold">
              <button
                type="button"
                onClick={() => {
                  setIsSignUp(false);
                  setFormError(null);
                }}
                className={`py-2 rounded-lg transition-all cursor-pointer ${
                  !isSignUp
                    ? 'bg-blue-600 text-white shadow-sm font-bold'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                Sign In
              </button>
              <button
                type="button"
                onClick={() => {
                  setIsSignUp(true);
                  setFormError(null);
                }}
                className={`py-2 rounded-lg transition-all cursor-pointer ${
                  isSignUp
                    ? 'bg-blue-600 text-white shadow-sm font-bold'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                Create Account
              </button>
            </div>

            <form onSubmit={handleFormSubmit} className="space-y-4">
              {isSignUp && (
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                    Full Name
                  </label>
                  <div className="relative">
                    <User className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
                    <input
                      type="text"
                      required
                      value={fullName}
                      onChange={(e) => setFullName(e.target.value)}
                      placeholder="e.g. Maya Chen"
                      className="w-full pl-10 pr-3.5 py-2.5 rounded-xl bg-[#0a0f1d] border border-slate-800 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-colors"
                    />
                  </div>
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  Email Address
                </label>
                <div className="relative">
                  <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="name@example.com"
                    className="w-full pl-10 pr-3.5 py-2.5 rounded-xl bg-[#0a0f1d] border border-slate-800 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-colors"
                  />
                </div>
              </div>

              <div>
                <div className="flex items-center justify-between mb-1.5">
                  <label className="block text-xs font-semibold text-slate-300">Password</label>
                  {!isSignUp && (
                    <span className="text-[11px] text-blue-400 cursor-pointer hover:underline">
                      Forgot password?
                    </span>
                  )}
                </div>
                <div className="relative">
                  <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
                  <input
                    type="password"
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••"
                    className="w-full pl-10 pr-3.5 py-2.5 rounded-xl bg-[#0a0f1d] border border-slate-800 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-colors"
                  />
                </div>
              </div>

              <div className="pt-2">
                <Button
                  type="submit"
                  size="md"
                  variant="primary"
                  className="w-full"
                  isLoading={loading}
                  rightIcon={<ArrowRight className="w-4 h-4" />}
                >
                  {isSignUp ? 'Create Profile & Continue' : 'Sign In & Continue'}
                </Button>
              </div>
            </form>

            <div className="pt-2 border-t border-slate-800/80 text-center text-xs text-slate-500 flex items-center justify-center gap-1.5">
              <ShieldCheck className="w-3.5 h-3.5 text-blue-400" />
              <span>Spring Boot REST Authentication Active</span>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
