import React from 'react';
import { MarketReport } from '../types';

interface ReportViewProps {
  report: MarketReport;
}

export const ReportView: React.FC<ReportViewProps> = ({ report }) => {
  // Simple function to process markdown-like text into React nodes
  // This avoids heavy dependencies for this specific environment
  const renderContent = (text: string) => {
    return text.split('\n').map((line, index) => {
      if (line.startsWith('## ')) {
        return <h2 key={index} className="text-2xl font-bold text-emerald-400 mt-6 mb-3">{line.replace('## ', '')}</h2>;
      }
      if (line.startsWith('### ')) {
        return <h3 key={index} className="text-xl font-semibold text-sky-400 mt-4 mb-2">{line.replace('### ', '')}</h3>;
      }
      if (line.startsWith('**') && line.endsWith('**')) {
         return <p key={index} className="font-bold text-slate-200 my-2">{line.replace(/\*\*/g, '')}</p>;
      }
      if (line.trim().startsWith('- ')) {
        return <li key={index} className="ml-4 text-slate-300 mb-1 list-disc pl-2">{line.replace('- ', '').replace(/\*\*/g, '')}</li>;
      }
      if (line.trim().startsWith('* ')) {
        return <li key={index} className="ml-4 text-slate-300 mb-1 list-disc pl-2">{line.replace('* ', '').replace(/\*\*/g, '')}</li>;
      }
      if (line.trim() === '') {
        return <div key={index} className="h-2"></div>;
      }
      // Handle bolding within text crudely
      const parts = line.split('**');
      if (parts.length > 1) {
          return (
              <p key={index} className="text-slate-300 leading-relaxed mb-2">
                  {parts.map((part, i) => i % 2 === 1 ? <span key={i} className="font-bold text-slate-100">{part}</span> : part)}
              </p>
          )
      }

      return <p key={index} className="text-slate-300 leading-relaxed mb-2">{line}</p>;
    });
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="bg-slate-800 rounded-xl p-8 shadow-xl border border-slate-700">
        <div className="flex items-center justify-between mb-6 border-b border-slate-700 pb-4">
          <h2 className="text-2xl font-bold text-white flex items-center gap-3">
            <i className="fas fa-chart-line text-emerald-500"></i>
            Daily S&P 500 Report
          </h2>
          <span className="text-sm text-slate-400">
            Generated: {new Date(report.timestamp).toLocaleTimeString()}
          </span>
        </div>
        
        <div className="prose prose-invert max-w-none">
          {renderContent(report.content)}
        </div>
      </div>

      {report.sources.length > 0 && (
        <div className="bg-slate-800 rounded-xl p-6 shadow-lg border border-slate-700">
          <h3 className="text-lg font-semibold text-slate-200 mb-4 flex items-center gap-2">
            <i className="fas fa-link text-blue-400"></i>
            Sources & Grounding
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {report.sources.map((source, idx) => (
              <a 
                key={idx}
                href={source.web?.uri}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center p-3 rounded-lg bg-slate-900/50 hover:bg-slate-700 transition-colors border border-slate-700 hover:border-blue-500 group"
              >
                <div className="w-8 h-8 rounded-full bg-slate-800 flex items-center justify-center mr-3 group-hover:bg-blue-500/20 transition-colors">
                  <i className="fas fa-globe text-slate-400 group-hover:text-blue-400 text-sm"></i>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-slate-200 truncate group-hover:text-blue-300">
                    {source.web?.title || "Unknown Source"}
                  </p>
                  <p className="text-xs text-slate-500 truncate">
                    {source.web?.uri}
                  </p>
                </div>
                <i className="fas fa-external-link-alt text-xs text-slate-600 group-hover:text-slate-400 ml-2"></i>
              </a>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
