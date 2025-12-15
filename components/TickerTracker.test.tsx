import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { TickerTracker } from './TickerTracker';
import * as geminiService from '../services/geminiService';

declare const jest: any;
declare const describe: any;
declare const beforeEach: any;
declare const it: any;
declare const expect: any;

jest.mock('../services/geminiService');

describe('TickerTracker', () => {
  const mockFetchStock = geminiService.fetchStockPerformance as any;

  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  it('renders input and empty state initially', () => {
    render(<TickerTracker refreshTrigger={0} />);
    expect(screen.getByPlaceholderText(/Symbol/i)).toBeInTheDocument();
    expect(screen.getByText(/Add stocks to track/i)).toBeInTheDocument();
  });

  it('allows adding a ticker and updates local storage', async () => {
    mockFetchStock.mockResolvedValue([]);
    render(<TickerTracker refreshTrigger={0} />);
    
    const input = screen.getByPlaceholderText(/Symbol/i);
    const submitBtn = screen.getByRole('button', { name: '' }); // The plus icon button

    fireEvent.change(input, { target: { value: 'googl' } });
    fireEvent.click(submitBtn);

    // Should appear in list
    expect(screen.getByText('GOOGL')).toBeInTheDocument();

    // Should persist
    expect(JSON.parse(localStorage.getItem('finagent_tickers') || '[]')).toContain('GOOGL');
  });

  it('fetches and displays stock data for added tickers', async () => {
    localStorage.setItem('finagent_tickers', JSON.stringify(['MSFT']));
    mockFetchStock.mockResolvedValue([
      { symbol: 'MSFT', price: '300.00', change: '+2.00', percentChange: '+0.6%' }
    ]);

    render(<TickerTracker refreshTrigger={0} />);

    await waitFor(() => {
      expect(screen.getByText('MSFT')).toBeInTheDocument();
      expect(screen.getByText('$300.00')).toBeInTheDocument();
      expect(screen.getByText('+0.6%')).toBeInTheDocument();
    });
  });

  it('allows removing a ticker', async () => {
    localStorage.setItem('finagent_tickers', JSON.stringify(['NVDA']));
    mockFetchStock.mockResolvedValue([]);

    render(<TickerTracker refreshTrigger={0} />);
    
    // Wait for it to render
    await waitFor(() => expect(screen.getByText('NVDA')).toBeInTheDocument());

    const removeBtn = screen.getByLabelText('Remove NVDA');
    fireEvent.click(removeBtn);

    expect(screen.queryByText('NVDA')).not.toBeInTheDocument();
    expect(JSON.parse(localStorage.getItem('finagent_tickers') || '[]')).not.toContain('NVDA');
  });
});