import React, { useState, useCallback } from 'react';
import { Header } from './components/Header';
import { Spinner } from './components/Spinner';
import { ReportView } from './components/ReportView';
import { MarketChart } from './components/MarketChart';
import { generateDailyReport } from './services/geminiService';
import { MarketReport, ReportStatus } from './types';

const App: React.FC = () => {
  const [status, setStatus] = useState<ReportStatus>(ReportStatus.IDLE);
  const [report, setReport] = useState<MarketReport | null>(null);
  const [error, setError] = useState<string | null>(null);
  // Counter to trigger chart refreshes when report is regenerated
  const [refreshChartTrigger, setRefreshChartTrigger] = useState<number>(0);

  const handleGenerateReport = useCallback(async () => {
    setStatus(ReportStatus.LOADING);
    setError(null);
    try {
      // Trigger chart refresh in parallel
      setRefreshChartTrigger(prev => prev + 1);
      
      const data = await generateDailyReport();
      setReport(data);
      setStatus(ReportStatus.SUCCESS);
    } catch (err) {
      setError("Failed to generate report. Please try again.");
      setStatus(ReportStatus.ERROR);
    }
  }, []);

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 flex flex-col">
      <Header />

      <main className="flex-grow container mx-auto px-4 sm:px-6 lg:px-8 py-8" role="main">
        {/* Hero Section */}
        <div className="mb-10 text-center max-w-3xl mx-auto">
          <h2 className="text-4xl md:text-5xl font-bold text-white mb-6 leading-tight">
            Daily S&P 500 <br />
            <span className="bg-clip-text text-transparent bg-gradient-to-r from-emerald-400 to-cyan-500">
              Performance Report
            </span>
          </h2>
          <p className="text-lg text-slate-400 mb-8">
            Get instant, AI-generated analysis of today's market movements, powered by Gemini and real-time grounding.
          </p>
          
          {status === ReportStatus.IDLE && (
            <button
              onClick={handleGenerateReport}
              data-testid="generate-report-btn"
              className="group relative inline-flex items-center justify-center px-8 py-4 text-lg font-semibold text-white transition-all duration-200 bg-emerald-600 font-pj rounded-full focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-emerald-600 hover:bg-emerald-700 hover:scale-105 active:scale-95 shadow-lg shadow-emerald-500/30"
              aria-label="Generate Daily Market Report"
            >
              <span className="absolute left-0 w-full h-full -ml-2 -mt-2 transition-all duration-200 origin-bottom-right rotate-45 translate-x-3 translate-y-3 bg-white opacity-20 group-hover:rotate-90 ease"></span>
              <span className="relative flex items-center gap-2">
                <i className="fas fa-bolt" aria-hidden="true"></i> Generate Daily Report
              </span>
            </button>
          )}
        </div>

        {/* Content Area */}
        <div className="max-w-6xl mx-auto">
          
          {status === ReportStatus.LOADING && (
            <div className="flex flex-col items-center animate-fade-in" data-testid="loading-state">
                <Spinner />
                <p className="text-slate-500 text-sm mt-4">Consulting trusted financial sources...</p>
            </div>
          )}

          {status === ReportStatus.ERROR && (
             <div className="bg-red-500/10 border border-red-500/20 rounded-xl p-6 text-center max-w-lg mx-auto" data-testid="error-state">
                <i className="fas fa-exclamation-circle text-red-500 text-3xl mb-3" aria-hidden="true"></i>
                <p className="text-red-400 font-medium">{error}</p>
                <button 
                  onClick={handleGenerateReport}
                  className="mt-4 text-sm bg-red-500/20 hover:bg-red-500/30 text-red-300 py-2 px-4 rounded-lg transition-colors"
                  aria-label="Retry generating report"
                >
                  Retry
                </button>
             </div>
          )}

          {status === ReportStatus.SUCCESS && report && (
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8" data-testid="report-container">
              {/* Main Report Column */}
              <div className="lg:col-span-2">
                <ReportView report={report} />
              </div>

              {/* Sidebar / Stats Column */}
              <div className="space-y-6">
                 {/* Market Stats Card - Now receives the trigger to refresh when the main button is clicked */}
                 <MarketChart refreshTrigger={refreshChartTrigger} />
                 
                 {/* Info Card */}
                 <div className="bg-gradient-to-br from-slate-800 to-slate-900 rounded-xl p-6 border border-slate-700 shadow-lg">
                    <h3 className="text-white font-semibold mb-3 flex items-center gap-2">
                        <i className="fas fa-info-circle text-sky-400" aria-hidden="true"></i>
                        About S&P 500
                    </h3>
                    <p className="text-sm text-slate-400 leading-relaxed">
                        The Standard and Poor's 500 is a stock market index tracking the stock performance of 500 of the largest companies listed on stock exchanges in the United States. It is one of the most commonly followed equity indices.
                    </p>
                 </div>

                 <button
                    onClick={handleGenerateReport}
                    data-testid="refresh-analysis-btn"
                    className="w-full py-3 rounded-xl border border-slate-700 hover:border-emerald-500/50 hover:bg-slate-800 text-slate-300 hover:text-emerald-400 transition-all text-sm font-medium flex items-center justify-center gap-2"
                    aria-label="Refresh Market Analysis"
                 >
                    <i className="fas fa-redo" aria-hidden="true"></i> Refresh Analysis
                 </button>
              </div>
            </div>
          )}
        </div>
      </main>

      <footer className="bg-slate-900 py-6 border-t border-slate-800 mt-12">
        <div className="text-center text-slate-500 text-sm">
          <p>© {new Date().getFullYear()} FinAgent Pro. Powered by Gemini.</p>
          <p className="text-xs mt-2 opacity-60">Not financial advice. For informational purposes only.</p>
        </div>
      </footer>
    </div>
  );
};

export default App;