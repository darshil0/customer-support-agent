// App.test.tsx
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import App from './App';
import * as geminiService from './services/geminiService';
import { vi } from 'vitest';

// Mock the entire geminiService module
vi.mock('./services/geminiService');

describe('App Integration', () => {
  beforeEach(() => {
    // Reset mocks and localStorage before each test
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('renders the landing page correctly', () => {
    render(<App />);
    expect(screen.getByText(/AI-Powered S&P 500 Analysis/i)).toBeInTheDocument();
    expect(screen.getByTestId('generate-report-btn')).toBeInTheDocument();
  });

  it('handles report generation flow: loading -> success', async () => {
    const mockReport = {
      text: '## Market is Up!',
      groundingSources: [{ title: 'Source 1', uri: 'https://source1.com' }],
    };

    // Mock the generateMarketReport function
    vi.spyOn(geminiService, 'generateMarketReport').mockResolvedValue(mockReport);

    render(<App />);

    // Click the generate report button
    fireEvent.click(screen.getByTestId('generate-report-btn'));

    // Check for loading state
    expect(screen.getByTestId('loading-state')).toBeInTheDocument();

    // Wait for the report to be displayed
    await waitFor(() => {
      expect(screen.getByTestId('report-container')).toBeInTheDocument();
    });

    // Check that the report content is displayed
    expect(screen.getByText('Market is Up!')).toBeInTheDocument();

    // Check that the TickerTracker and MarketChart are displayed
    expect(screen.getByTestId('ticker-tracker')).toBeInTheDocument();
    expect(screen.getByTestId('market-chart-container')).toBeInTheDocument();
  });

  it('displays an error state if report generation fails', async () => {
    // Mock the generateMarketReport function to reject with an error
    vi.spyOn(geminiService, 'generateMarketReport').mockRejectedValue(new Error('API Error'));

    render(<App />);

    // Click the generate report button
    fireEvent.click(screen.getByTestId('generate-report-btn'));

    // Wait for the error state to be displayed
    await waitFor(() => {
      expect(screen.getByTestId('error-state')).toBeInTheDocument();
    });

    // Check that the error message is displayed
    expect(screen.getByText('API Error')).toBeInTheDocument();
  });

  it('persists and loads history', async () => {
    const mockReport = {
      text: '## Historical Report',
      groundingSources: [],
    };
    vi.spyOn(geminiService, 'generateMarketReport').mockResolvedValue(mockReport);

    const { unmount } = render(<App />);

    // Generate a report
    fireEvent.click(screen.getByTestId('generate-report-btn'));

    await waitFor(() => {
      expect(screen.getByText('Historical Report')).toBeInTheDocument();
    });

    // Unmount the component to simulate a page reload
    unmount();

    // Re-render the component
    render(<App />);

    // Check that the historical report is loaded from localStorage
    expect(screen.getByText('Historical Report')).toBeInTheDocument();
  });

  it('handles manual refresh of analysis', async () => {
    const mockReport1 = {
      text: '## Report 1',
      groundingSources: [],
    };
    const mockReport2 = {
      text: '## Report 2',
      groundingSources: [],
    };

    // Mock the generateMarketReport function
    const mockFn = vi.spyOn(geminiService, 'generateMarketReport')
      .mockResolvedValueOnce(mockReport1)
      .mockResolvedValueOnce(mockReport2);

    render(<App />);

    // Generate the first report
    fireEvent.click(screen.getByTestId('generate-report-btn'));
    await waitFor(() => {
      expect(screen.getByText('Report 1')).toBeInTheDocument();
    });

    // Click the refresh button
    fireEvent.click(screen.getByTestId('refresh-analysis-btn'));

    // Wait for the second report to be displayed
    await waitFor(() => {
      expect(screen.getByText('Report 2')).toBeInTheDocument();
    });

    // Check that the mock function was called twice
    expect(mockFn).toHaveBeenCalledTimes(2);
  });
});
