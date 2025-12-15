import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import App from './App';
import * as geminiService from './services/geminiService';

declare const jest: any;
declare const describe: any;
declare const beforeEach: any;
declare const it: any;
declare const expect: any;

// Mock the services
jest.mock('./services/geminiService');
const mockGenerateReport = geminiService.generateDailyReport as any;
const mockFetchSector = geminiService.fetchSectorWeights as any;

// Mock child components that might need resizing/external deps
jest.mock('recharts', () => ({
  ResponsiveContainer: ({ children }: any) => <div>{children}</div>,
  PieChart: ({ children }: any) => <div>{children}</div>,
  Pie: () => <div>Pie Slice</div>,
  Tooltip: () => null,
  Legend: () => null,
  Cell: () => null,
}));

describe('App Integration', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it('renders the landing page correctly', () => {
    render(<App />);
    expect(screen.getByText(/Daily S&P 500/i)).toBeInTheDocument();
    expect(screen.getByTestId('generate-report-btn')).toBeInTheDocument();
  });

  it('handles report generation flow: loading -> success', async () => {
    // Setup Mocks
    mockGenerateReport.mockImplementation(() =>
      new Promise(resolve => setTimeout(() => resolve({
        content: '# Analysis\nMarket is good.',
        sources: [],
        timestamp: new Date().toISOString(),
      }), 100))
    );
    mockFetchSector.mockResolvedValue({ data: [], sources: [] });

    render(<App />);

    // Click Generate
    fireEvent.click(screen.getByTestId('generate-report-btn'));

    // Check Loading State
    expect(screen.getByTestId('loading-state')).toBeInTheDocument();

    // Wait for Success State
    await waitFor(() => {
      expect(screen.getByTestId('report-container')).toBeInTheDocument();
    });

    expect(screen.getByText('Market is good.')).toBeInTheDocument();
  });

  it('displays error state if generation fails', async () => {
    mockGenerateReport.mockRejectedValue(new Error('API Fail'));

    render(<App />);
    fireEvent.click(screen.getByTestId('generate-report-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('error-state')).toBeInTheDocument();
    });
  });

  it('persists and loads history', async () => {
    const mockReport = {
      content: 'Historical Report Content',
      sources: [],
      timestamp: new Date('2024-01-01').toISOString(),
    };

    mockGenerateReport.mockResolvedValue(mockReport);
    mockFetchSector.mockResolvedValue({ data: [], sources: [] });

    render(<App />);
    
    // Generate first report
    fireEvent.click(screen.getByTestId('generate-report-btn'));
    await waitFor(() => screen.getByTestId('report-container'));

    // Check if added to History List
    expect(screen.getByText('Historical Report Content...')).toBeInTheDocument();

    // Reload Page (simulate by unmounting and remounting)
    // In a real browser test we'd refresh, here we check localStorage
    const stored = localStorage.getItem('finagent_history');
    expect(stored).toContain('Historical Report Content');
  });
});