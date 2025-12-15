import React from 'react';
import { MarketReport } from '../types';

interface HistoryListProps {
  history: MarketReport[];
  onSelect: (report: MarketReport) => void;
  onClear: () => void;
}

export const HistoryList: React.FC<HistoryListProps> = ({ history, onSelect, onClear }) => {
  if (history.length === 0) return null;

  return (
    <div className="bg-slate-800 rounded-xl p-6 border border-slate-700 shadow-lg animate-fade-in" data-testid="history-list-container">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-white font-semibold flex items-center gap-2">
          <i className="fas fa-history text-slate-400" aria-hidden="true"></i>
          Recent History
        </h3>
        <button
          onClick={onClear}
          className="text-xs text-red-400 hover:text-red-300 hover:underline transition-colors"
          aria-label="Clear History"
          data-testid="clear-history-btn"
        >
          Clear
        </button>
      </div>
      
      <div className="space-y-2 max-h-60 overflow-y-auto pr-1">
        {history.map((item, index) => (
          <button
            key={`${item.timestamp}-${index}`}
            onClick={() => onSelect(item)}
            className="w-full text-left p-3 rounded-lg bg-slate-900/50 hover:bg-slate-700 border border-slate-800 hover:border-emerald-500/30 transition-all group focus:outline-none focus:ring-1 focus:ring-emerald-500"
            data-testid={`history-item-${index}`}
            aria-label={`Load report from ${new Date(item.timestamp).toLocaleDateString()}`}
          >
            <div className="flex items-center justify-between mb-1">
              <span className="text-sm text-slate-300 group-hover:text-emerald-400 font-medium">
                 {new Date(item.timestamp).toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}
              </span>
              <span className="text-xs text-slate-500 font-mono">
                 {new Date(item.timestamp).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
              </span>
            </div>
            <div className="text-xs text-slate-500 truncate opacity-70 group-hover:opacity-100 transition-opacity">
               {item.content.replace(/[#*]/g, '').slice(0, 40)}...
            </div>
          </button>
        ))}
      </div>
    </div>
  );
};
