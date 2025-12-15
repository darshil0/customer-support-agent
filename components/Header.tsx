import React from 'react';

export const Header: React.FC = () => {
  return (
    <header className="sticky top-0 z-50 bg-slate-900/95 backdrop-blur-md border-b border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-emerald-500 to-teal-700 flex items-center justify-center shadow-lg shadow-emerald-500/20">
              <i className="fas fa-robot text-white text-lg"></i>
            </div>
            <div>
              <h1 className="text-xl font-bold text-white tracking-tight">FinAgent <span className="text-emerald-500">Pro</span></h1>
              <p className="text-xs text-slate-400">AI-Powered Market Intelligence</p>
            </div>
          </div>
          <div className="flex items-center gap-4">
            <a href="#" className="text-slate-400 hover:text-white transition-colors">
                <i className="fab fa-github text-xl"></i>
            </a>
            <button className="text-sm font-medium text-slate-300 hover:text-white transition-colors">
              Documentation
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};
