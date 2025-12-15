import React, { useState } from 'react';
import { ErrorBoundary } from './components/ErrorBoundary';
import { Header } from './components/Header';
import { Hero } from './components/Hero';
import { ReportView } from './components/ReportView';
import { MarketChart } from './components/MarketChart';
import { TickerTracker } from './components/TickerTracker';
import { LoadingState } from './components/LoadingState';
import { ErrorState } from './components/ErrorState';
import { 
  generateMarketReport, 
  getMockSectorData, 
  getMockStockData,
  type MarketReport 
} from './services/geminiService';

function App() {
  const [report, setReport] = useState<MarketReport | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);

  const sectorData = getMockSectorData();
  const stockData = getMockStockData();

  const handleGenerateReport = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const result = await generateMarketReport();
      setReport(result);
      setLastUpdated(new Date());
    } catch (err: any) {
      console.error('Error generating report:', err);
      setError(err.message || 'Failed to generate report. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleRefresh = () => {
    handleGenerateReport();
  };

  return (
    <ErrorBoundary>
      <div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 text-white">
        <Header />
        
        <main className="container mx-auto px-4 py-8">
          {!report && !isLoading && !error && (
            <Hero onGenerate={handleGenerateReport} />
          )}

          {isLoading && (
            <LoadingState />
          )}

          {error && (
            <ErrorState 
              message={error} 
              onRetry={handleGenerateReport}
            />
          )}

          {report && !isLoading && (
            <div className="space-y-8">
              {/* Report Header */}
              <div className="flex justify-between items-center">
                <div>
                  <h2 className="text-3xl font-bold mb-2">Market Analysis Report</h2>
                  {lastUpdated && (
                    <p className="text-gray-400 text-sm">
                      Last updated: {lastUpdated.toLocaleString()}
                    </p>
                  )}
                </div>
                <button
                  data-testid="refresh-analysis-btn"
                  onClick={handleRefresh}
                  disabled={isLoading}
                  className="px-6 py-3 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-600 disabled:cursor-not-allowed rounded-lg transition-colors flex items-center gap-2"
                  aria-label="Refresh market analysis"
                >
                  <svg 
                    className={`w-5 h-5 ${isLoading ? 'animate-spin' : ''}`}
                    fill="none" 
                    viewBox="0 0 24 24" 
                    stroke="currentColor"
                  >
                    <path 
                      strokeLinecap="round" 
                      strokeLinejoin="round" 
                      strokeWidth={2} 
                      d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" 
                    />
                  </svg>
                  Refresh
                </button>
              </div>

              {/* Main Content Grid */}
              <div className="grid lg:grid-cols-3 gap-8">
                {/* Report Section - Takes 2 columns */}
                <div className="lg:col-span-2 space-y-6">
                  <ReportView 
                    report={report.text}
                    sources={report.groundingSources}
                  />
                </div>

                {/* Sidebar - Takes 1 column */}
                <div className="space-y-6">
                  <MarketChart data={sectorData} />
                </div>
              </div>

              {/* Stock Tracker Section */}
              <div className="mt-8">
                <TickerTracker stocks={stockData} />
              </div>
            </div>
          )}
        </main>

        {/* Footer */}
        <footer className="container mx-auto px-4 py-8 mt-16 border-t border-gray-800">
          <div className="text-center text-gray-400 text-sm">
            <p>Powered by Google Gemini 2.0 Flash</p>
            <p className="mt-2">
              Data is AI-generated and should not be considered financial advice.
            </p>
          </div>
        </footer>
      </div>
    </ErrorBoundary>
  );
}

export default App;
