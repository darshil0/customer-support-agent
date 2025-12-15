import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MarketChart } from './MarketChart';
import * as geminiService from '../services/geminiService';

declare const jest: any;
declare const describe: any;
declare const beforeEach: any;
declare const it: any;
declare const expect: any;

// Mock recharts
jest.mock('recharts', () => ({
  ResponsiveContainer: ({ children }: any) => <div data-testid="responsive-container">{children}</div>,
  PieChart: ({ children }: any) => <div data-testid="pie-chart">{children}</div>,
  Pie: ({ data }: any) => (
    <div data-testid="pie">
      {data.map((entry: any, i: number) => (
        <div key={i} data-testid="pie-cell">{entry.name}</div>
      ))}
    </div>
  ),
  Cell: () => null,
  Tooltip: () => null,
}));

jest.mock('../services/geminiService');

describe('MarketChart', () => {
  const mockFetchSectorWeights = geminiService.fetchSectorWeights as any;

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders loading state initially', async () => {
    // Return a promise that doesn't resolve immediately
    mockFetchSectorWeights.mockImplementation(() => new Promise(() => {}));
    
    render(<MarketChart refreshTrigger={0} />);
    
    expect(screen.getByTestId('chart-loading-indicator')).toBeInTheDocument();
  });

  it('renders data and custom legend after fetch', async () => {
    mockFetchSectorWeights.mockResolvedValue({
      data: [
        { name: 'Technology', value: 30.5 },
        { name: 'Finance', value: 20.0 }
      ],
      sources: []
    });

    render(<MarketChart refreshTrigger={0} />);

    await waitFor(() => {
      expect(screen.queryByTestId('chart-loading-indicator')).not.toBeInTheDocument();
    });

    // Check for custom legend items
    expect(screen.getByText('Technology')).toBeInTheDocument();
    expect(screen.getByText('30.5%')).toBeInTheDocument();
    expect(screen.getByText('Finance')).toBeInTheDocument();
  });

  it('renders fallback data on error', async () => {
    mockFetchSectorWeights.mockRejectedValue(new Error('Failed'));

    render(<MarketChart refreshTrigger={0} />);

    await waitFor(() => {
      expect(screen.getByTestId('chart-data-source')).toHaveTextContent('Reference Data');
    });
  });

  it('manual refresh button triggers new data fetch', async () => {
    mockFetchSectorWeights.mockResolvedValue({
      data: [{ name: 'Tech', value: 50 }],
      sources: []
    });

    render(<MarketChart refreshTrigger={0} />);
    await waitFor(() => screen.getByText('Tech'));

    const refreshBtn = screen.getByLabelText('Refresh Chart Data');
    fireEvent.click(refreshBtn);

    expect(mockFetchSectorWeights).toHaveBeenCalledTimes(2); // 1 on mount, 1 on click
    
    // Loading indicator should appear again briefly
    expect(screen.getByTestId('chart-loading-indicator')).toBeInTheDocument();
  });
});