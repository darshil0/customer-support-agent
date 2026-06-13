import React, { useState, useEffect, useMemo } from 'react';
import {
  Client,
  Provider,
  cacheExchange,
  fetchExchange,
  useQuery,
  subscriptionExchange
} from 'urql';
import { createClient as createWSClient } from 'graphql-ws';
import { Client as StompClient } from '@stomp/stompjs';
import { ErrorBoundary } from './components/ErrorBoundary';
import { Header } from './components/Header';
import { Hero } from './components/Hero';
import { ReportView } from './components/ReportView';
import { MarketChart } from './components/MarketChart';
import { TickerTracker } from './components/TickerTracker';
import { AnalyticsDashboard } from './components/AnalyticsDashboard';
import { LoadingState } from './components/LoadingState';
import { ErrorState } from './components/ErrorState';
import { logger } from './services/logger';
import { 
  generateMarketReport, 
  getMockSectorData, 
  getMockStockData,
  type MarketReport 
} from './services/geminiService';

function App() {
  const [report, setReport] = useState<MarketReport | null>(() => {
    const saved = localStorage.getItem('last_report');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch (e) {
        console.error('Failed to parse persisted report:', e);
        return null;
      }
    }
    return null;
  });

  const [lastUpdated, setLastUpdated] = useState<Date | null>(() => {
    const saved = localStorage.getItem('last_updated');
    return saved ? new Date(saved) : null;
  });

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'market' | 'analytics'>('market');

  const [analyticsData, setAnalyticsData] = useState({
    ticketStatusDistribution: [
      { status: 'open', count: 12 },
      { status: 'closed', count: 45 },
      { status: 'pending', count: 8 }
    ],
    totalRevenue: 125430,
    customerTierBreakdown: [
      { tier: 'Basic', count: 120 },
      { tier: 'Premium', count: 85 },
      { tier: 'Enterprise', count: 24 }
    ]
  });

  // Persist report when it changes
  useEffect(() => {
    if (report && lastUpdated) {
      localStorage.setItem('last_report', JSON.stringify(report));
      localStorage.setItem('last_updated', lastUpdated.toISOString());
    }
  }, [report, lastUpdated]);

  const ANALYTICS_QUERY = `
    query GetAnalytics {
      analytics {
        ticketStatusDistribution {
          status
          count
        }
        totalRevenue
        customerTierBreakdown {
          tier
          count
        }
      }
    }
  `;

  const [analyticsResult, reexecuteAnalytics] = useQuery({
    query: ANALYTICS_QUERY,
    pause: activeTab !== 'analytics'
  });

  useEffect(() => {
    if (analyticsResult.data?.analytics) {
      setAnalyticsData(analyticsResult.data.analytics);
    }
  }, [analyticsResult.data]);

  useEffect(() => {
    // Setup STOMP over WebSocket for real-time updates
    const stompClient = new StompClient({
      brokerURL: 'ws://localhost:8000/ws',
      onConnect: () => {
        logger.info('STOMP connected');
        stompClient.subscribe('/topic/analytics', (message) => {
          logger.info('Analytics updated event received');
          reexecuteAnalytics({ requestPolicy: 'network-only' });
        });
      },
      onStompError: (frame) => {
        logger.error('STOMP error', frame);
      },
    });

    stompClient.activate();

    return () => {
      stompClient.deactivate();
    };
  }, [reexecuteAnalytics]);

  const client = useMemo(() => {
    return new Client({
      url: 'http://localhost:8000/graphql',
      exchanges: [cacheExchange, fetchExchange],
    });
  }, []);

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
    <Provider value={client}>
    <ErrorBoundary>
      <div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 text-white">
        <Header />
        
        <main className="container mx-auto px-4 py-8">
          <div className="flex gap-4 mb-8 border-b border-gray-800">
            <button
              onClick={() => setActiveTab('market')}
              className={`pb-4 px-2 font-medium transition-colors relative ${
                activeTab === 'market' ? 'text-blue-500' : 'text-gray-400 hover:text-gray-200'
              }`}
            >
              Market Analysis
              {activeTab === 'market' && <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-blue-500" />}
            </button>
            <button
              onClick={() => setActiveTab('analytics')}
              className={`pb-4 px-2 font-medium transition-colors relative ${
                activeTab === 'analytics' ? 'text-blue-500' : 'text-gray-400 hover:text-gray-200'
              }`}
            >
              Support Analytics
              {activeTab === 'analytics' && <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-blue-500" />}
            </button>
          </div>

          {activeTab === 'analytics' && (
            <AnalyticsDashboard data={analyticsData} />
          )}

          {activeTab === 'market' && !report && !isLoading && !error && (
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

          {activeTab === 'market' && report && !isLoading && (
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
    </Provider>
  );
}

export default App;
