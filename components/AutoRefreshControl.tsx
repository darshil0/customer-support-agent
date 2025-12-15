import React from 'react';

interface AutoRefreshControlProps {
  interval: number;
  onIntervalChange: (interval: number) => void;
  disabled: boolean;
}

export const AutoRefreshControl: React.FC<AutoRefreshControlProps> = ({
  interval,
  onIntervalChange,
  disabled,
}) => {
  return (
    <div className="bg-slate-800 rounded-xl p-4 border border-slate-700 shadow-lg">
      <div className="flex items-center justify-between mb-2">
        <label
          htmlFor="refresh-interval"
          className="text-sm font-semibold text-white flex items-center gap-2"
        >
          <i className="fas fa-clock text-emerald-400" aria-hidden="true"></i>
          Auto-Refresh
        </label>
        {interval > 0 && !disabled && (
          <span className="flex h-2 w-2 relative" title="Auto-refresh active">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
          </span>
        )}
      </div>
      <select
        id="refresh-interval"
        value={interval}
        onChange={(e) => onIntervalChange(Number(e.target.value))}
        disabled={disabled}
        className="w-full bg-slate-900 border border-slate-700 text-slate-300 text-sm rounded-lg focus:ring-emerald-500 focus:border-emerald-500 block p-2.5 disabled:opacity-50 disabled:cursor-not-allowed transition-colors cursor-pointer hover:border-emerald-500/50"
        aria-label="Select auto-refresh interval"
      >
        <option value={0}>Manual (Off)</option>
        <option value={300000}>Every 5 Minutes</option>
        <option value={900000}>Every 15 Minutes</option>
        <option value={1800000}>Every 30 Minutes</option>
      </select>
      <p className="text-[10px] text-slate-500 mt-2 italic">
        {interval > 0
          ? `Refreshing automatically every ${interval / 60000} minutes.`
          : 'Select an interval to enable automatic updates.'}
      </p>
    </div>
  );
};
