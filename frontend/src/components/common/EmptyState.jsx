import React from 'react';
import { Inbox } from 'lucide-react';
import Card from './Card';

export default function EmptyState({
  icon: Icon = Inbox,
  title = 'No items found',
  description = 'There is currently no data available for this section.',
  action = null,
  className = '',
}) {
  return (
    <Card
      variant="default"
      className={`p-8 sm:p-12 text-center flex flex-col items-center justify-center space-y-3 ${className}`}
    >
      <div className="w-12 h-12 rounded-2xl bg-[var(--bg-canvas)] border border-[var(--border-subtle)] flex items-center justify-center text-[var(--text-muted)]">
        <Icon className="w-6 h-6 text-blue-400" />
      </div>
      <h3 className="text-base font-bold text-white tracking-tight">{title}</h3>
      <p className="text-xs sm:text-sm text-[var(--text-muted)] max-w-sm leading-relaxed">
        {description}
      </p>
      {action && <div className="pt-2">{action}</div>}
    </Card>
  );
}

