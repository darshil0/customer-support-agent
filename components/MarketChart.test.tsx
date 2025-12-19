import React from 'react';
import { render, screen } from '@testing-library/react';
import { MarketChart } from './MarketChart';
import { describe, it, expect, vi } from 'vitest';

// Mock recharts
vi.mock('recharts', () => ({
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
  Legend: () => null,
}));

describe('MarketChart', () => {
  const mockData = [
    { name: 'Technology', value: 30.5, fill: '#3b82f6' },
    { name: 'Finance', value: 20.0, fill: '#f59e0b' }
  ];

  it('renders the chart and legend items', () => {
    render(<MarketChart data={mockData} />);

    expect(screen.getByTestId('market-chart-container')).toBeInTheDocument();

    // Check for sector breakdown items
    expect(screen.getByText('Technology')).toBeInTheDocument();
    expect(screen.getByText('30.5%')).toBeInTheDocument();
    expect(screen.getByText('Finance')).toBeInTheDocument();
    expect(screen.getByText('20.0%')).toBeInTheDocument();
  });

  it('renders the correct number of pie cells', () => {
    render(<MarketChart data={mockData} />);
    const cells = screen.getAllByTestId('pie-cell');
    expect(cells).toHaveLength(2);
    expect(cells[0]).toHaveTextContent('Technology');
    expect(cells[1]).toHaveTextContent('Finance');
  });
});