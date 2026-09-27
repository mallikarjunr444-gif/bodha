import React from 'react';

export default function Card({
  children,
  variant = 'default',
  interactive = false,
  className = '',
  onClick = undefined,
  ...props
}) {
  const baseStyles = 'rounded-2xl transition-all duration-200';

  const variantStyles = {
    default:
      'bg-[#0e1526]/80 border border-[#1e2b45] shadow-lg shadow-black/20',
    elevated:
      'bg-[#141f36] border border-[#2a3b5c] shadow-xl shadow-black/40',
    glass:
      'jnanora-glass shadow-2xl shadow-black/50',
    glow:
      'bg-[#0e1526] border border-blue-500/40 shadow-xl shadow-blue-500/10 ring-1 ring-blue-500/30',
    ai:
      'bg-gradient-to-br from-cyan-950/20 via-[#0e1526] to-[#0e1526] border border-cyan-500/30 shadow-lg shadow-cyan-500/5',
  };

  const interactiveStyles = interactive
    ? 'hover:border-blue-500/60 hover:bg-[#131d33] hover:shadow-xl hover:shadow-blue-500/10 cursor-pointer active:scale-[0.995]'
    : '';

  const chosenVariant = variantStyles[variant] || variantStyles.default;

  return (
    <div
      onClick={onClick}
      className={`${baseStyles} ${chosenVariant} ${interactiveStyles} ${className}`}
      {...props}
    >
      {children}
    </div>
  );
}
