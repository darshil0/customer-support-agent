import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { AnalyticsDashboard } from './AnalyticsDashboard';
import React from 'react';

// Mock Recharts to avoid issues in JSDOM
vi.mock('recharts', () => ({
  ResponsiveContainer: ({ children }: any) => <div>{children}</div>,
  BarChart: () => <div>BarChart</div>,
  Bar: () => null,
  XAxis: () => null,
  YAxis: () => null,
  CartesianGrid: () => null,
  Tooltip: () => null,
  PieChart: () => <div>PieChart</div>,
  Pie: () => null,
  Cell: () => null,
  Legend: () => null,
}));

describe('AnalyticsDashboard', () => {
  const mockData = {
    ticketStatusDistribution: [
      { status: 'open', count: 5 },
      { status: 'closed', count: 10 }
    ],
    totalRevenue: 5000,
    customerTierBreakdown: [
      { tier: 'Basic', count: 20 },
      { tier: 'Premium', count: 10 }
    ]
  };

  it('renders metric cards correctly', () => {
    render(<AnalyticsDashboard data={mockData} />);

    expect(screen.getByText('Total Balance')).toBeDefined();
    expect(screen.getByText('$5,000')).toBeDefined();
    expect(screen.getByText('Active Tickets')).toBeDefined();
    expect(screen.getByText('5')).toBeDefined();
    expect(screen.getByText('Total Customers')).toBeDefined();
    expect(screen.getByText('30')).toBeDefined(); // 20 + 10
  });

  it('renders chart titles', () => {
    render(<AnalyticsDashboard data={mockData} />);

    expect(screen.getByText('Ticket Status Distribution')).toBeDefined();
    expect(screen.getByText('Customer Tier Breakdown')).toBeDefined();
  });
});
