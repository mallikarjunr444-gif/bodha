import React from 'react';
import { Loader2 } from 'lucide-react';

export default function Button({
  children,
  type = 'button',
  variant = 'primary',
  size = 'md',
  isLoading = false,
  disabled = false,
  leftIcon = null,
  rightIcon = null,
  className = '',
  onClick = undefined,
  ...props
}) {
  const baseStyles =
    'relative inline-flex items-center justify-center font-semibold transition-all duration-200 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed select-none rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500/40';

  const sizeStyles = {
    sm: 'text-xs px-3 py-1.5 gap-1.5',
    md: 'text-xs sm:text-sm px-4 sm:px-5 py-2.5 gap-2',
    lg: 'text-sm sm:text-base px-6 py-3 gap-2.5 shadow-lg',
  };

  const variantStyles = {
    primary:
      'bg-gradient-to-r from-blue-600 via-blue-600 to-cyan-500 hover:from-blue-500 hover:to-cyan-400 text-white shadow-md shadow-blue-600/25 border border-blue-400/30 hover:shadow-blue-500/40 active:scale-[0.99]',
    secondary:
      'bg-[#121c33] hover:bg-[#1a2846] text-slate-200 border border-slate-700/80 hover:border-slate-600 shadow-sm active:scale-[0.99]',
    ai:
      'bg-cyan-950/60 hover:bg-cyan-900/60 text-cyan-200 border border-cyan-500/40 shadow-md shadow-cyan-500/10 hover:shadow-cyan-500/20 active:scale-[0.99]',
    outline:
      'bg-transparent hover:bg-slate-900/80 text-slate-300 hover:text-white border border-slate-700 hover:border-slate-500 active:scale-[0.99]',
    ghost:
      'bg-transparent hover:bg-slate-800/60 text-slate-400 hover:text-white border border-transparent',
    danger:
      'bg-rose-950/60 hover:bg-rose-900/60 text-rose-200 border border-rose-500/40 hover:border-rose-500 shadow-sm',
  };

  const chosenSize = sizeStyles[size] || sizeStyles.md;
  const chosenVariant = variantStyles[variant] || variantStyles.primary;

  return (
    <button
      type={type}
      disabled={disabled || isLoading}
      onClick={onClick}
      className={`${baseStyles} ${chosenSize} ${chosenVariant} ${className}`}
      {...props}
    >
      {isLoading ? (
        <Loader2 className="w-4 h-4 animate-spin text-current" />
      ) : (
        leftIcon && <span className="shrink-0">{leftIcon}</span>
      )}
      <span>{children}</span>
      {!isLoading && rightIcon && <span className="shrink-0">{rightIcon}</span>}
    </button>
  );
}
