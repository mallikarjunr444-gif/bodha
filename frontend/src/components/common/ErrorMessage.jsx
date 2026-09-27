import React from 'react';
import { AlertCircle, RefreshCw } from 'lucide-react';
import Button from './Button';

export default function ErrorMessage({
  title = 'Something went wrong',
  message = 'An unexpected error occurred while communicating with the server.',
  onRetry = null,
  className = '',
}) {
  return (
    <div
      className={`p-5 rounded-2xl bg-rose-500/10 border border-rose-500/25 text-rose-200 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 ${className}`}
    >
      <div className="flex items-start gap-3">
        <AlertCircle className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
        <div>
          <h4 className="text-sm font-bold text-white tracking-tight">{title}</h4>
          <p className="text-xs text-rose-200/80 mt-0.5 leading-relaxed">{message}</p>
        </div>
      </div>

      {onRetry && (
        <Button
          variant="outline"
          size="sm"
          onClick={onRetry}
          icon={RefreshCw}
          className="border-rose-500/30 text-rose-300 hover:bg-rose-500/15 shrink-0"
        >
          Retry
        </Button>
      )}
    </div>
  );
}

