import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { TickerTracker } from './TickerTracker';
import { describe, it, expect, vi } from 'vitest';
import * as geminiService from '../services/geminiService';

vi.mock('../services/geminiService');

describe('TickerTracker', () => {
  const mockStocks = [
    {
      symbol: 'AAPL',
      price: 150.0,
      change: 2.5,
      sparkline: [148, 149, 150, 151, 150]
    },
    {
      symbol: 'MSFT',
      price: 300.0,
      change: -1.2,
      sparkline: [305, 304, 303, 302, 300]
    }
  ];

  beforeEach(() => {
    vi.spyOn(geminiService, 'getMockStockData').mockReturnValue(mockStocks);
  });

  it('renders fixed stock list correctly', () => {
    render(<TickerTracker stocks={mockStocks} />);

    expect(screen.getByTestId('ticker-tracker')).toBeInTheDocument();
    expect(screen.getByText('AAPL')).toBeInTheDocument();
    expect(screen.getByText('MSFT')).toBeInTheDocument();
    expect(screen.getByText('$150.00')).toBeInTheDocument();
    expect(screen.getByText('$300.00')).toBeInTheDocument();
  });

  it('filters stocks by symbol', () => {
    render(<TickerTracker stocks={mockStocks} />);

    const filterInput = screen.getByPlaceholderText(/Search stocks.../i);
    fireEvent.change(filterInput, { target: { value: 'aap' } });

    expect(screen.getByText('AAPL')).toBeInTheDocument();
    expect(screen.queryByText('MSFT')).not.toBeInTheDocument();
  });

  it('sorts stocks by price', () => {
    render(<TickerTracker stocks={mockStocks} />);

    const priceBtn = screen.getByRole('button', { name: /Sort by price/i });

    // Default is symbol asc
    let rows = screen.getAllByRole('button', { name: /AAPL|MSFT/i });
    expect(rows[0]).toHaveTextContent('AAPL');

    // Click price sort
    fireEvent.click(priceBtn);

    rows = screen.getAllByRole('button', { name: /AAPL|MSFT/i });
    expect(rows[0]).toHaveTextContent('AAPL'); // 150 < 300, so AAPL first

    // Click again for desc
    fireEvent.click(priceBtn);
    rows = screen.getAllByRole('button', { name: /AAPL|MSFT/i });
    expect(rows[0]).toHaveTextContent('MSFT'); // 300 > 150, so MSFT first
  });

  it('toggles stock selection for comparison', () => {
    render(<TickerTracker stocks={mockStocks} />);

    const aaplRow = screen.getByLabelText(/AAPL:/i);
    fireEvent.click(aaplRow);

    expect(screen.getByText(/1 selected/i)).toBeInTheDocument();
    expect(screen.getByTestId('stock-comparison')).toBeInTheDocument();
  });
});
