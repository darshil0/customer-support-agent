import React, { useState, useMemo, useCallback } from 'react';
import { StockData } from '../services/geminiService';
import { Sparkline } from './Sparkline';
import { StockComparison } from './StockComparison';

interface TickerTrackerProps {
  stocks: StockData[];
}

const sanitizeInput = (input: string): string => {
  // Remove potential HTML tags and limit length
  return input.replace(/[<>]/g, '').slice(0, 100);
};

export const TickerTracker: React.FC<TickerTrackerProps> = ({ stocks }) => {
  const [filter, setFilter] = useState('');
  const [sortBy, setSortBy] = useState<'symbol' | 'price' | 'change'>('symbol');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [selectedStocks, setSelectedStocks] = useState<string[]>([]);

  // Memoize filtered and sorted stocks
  const processedStocks = useMemo(() => {
    let filtered = stocks.filter(s => 
      s.symbol.toLowerCase().includes(filter.toLowerCase())
    );

    return filtered.sort((a, b) => {
      let comparison = 0;
      switch (sortBy) {
        case 'price':
          comparison = a.price - b.price;
          break;
        case 'change':
          comparison = a.change - b.change;
          break;
        default:
          comparison = a.symbol.localeCompare(b.symbol);
      }
      return sortOrder === 'asc' ? comparison : -comparison;
    });
  }, [stocks, filter, sortBy, sortOrder]);

  const handleStockToggle = useCallback((symbol: string) => {
    setSelectedStocks(prev => {
      if (prev.includes(symbol)) {
        return prev.filter(s => s !== symbol);
      }
      if (prev.length >= 3) {
        // Remove oldest selection and add new one
        return [...prev.slice(1), symbol];
      }
      return [...prev, symbol];
    });
  }, []);

  const handleSortChange = (newSortBy: 'symbol' | 'price' | 'change') => {
    if (sortBy === newSortBy) {
      setSortOrder(prev => prev === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(newSortBy);
      setSortOrder('asc');
    }
  };

  return (
    <div data-testid="ticker-tracker" className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold flex items-center gap-2">
          <svg 
            className="w-6 h-6 text-green-400" 
            fill="none" 
            viewBox="0 0 24 24" 
            stroke="currentColor"
          >
            <path 
              strokeLinecap="round" 
              strokeLinejoin="round" 
              strokeWidth={2} 
              d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z" 
            />
          </svg>
          Stock Watchlist
        </h2>
        {selectedStocks.length > 0 && (
          <span className="text-sm text-gray-400">
            {selectedStocks.length} selected (max 3 for comparison)
          </span>
        )}
      </div>

      {/* Controls */}
      <div className="bg-gray-800/50 backdrop-blur-sm border border-gray-700 rounded-lg p-4">
        <div className="flex flex-col sm:flex-row gap-3">
          <div className="flex-1">
            <label htmlFor="stock-filter" className="sr-only">Filter stocks</label>
            <input
              id="stock-filter"
              type="text"
              placeholder="Search stocks..."
              value={filter}
              onChange={(e) => setFilter(sanitizeInput(e.target.value))}
              className="w-full px-4 py-2 rounded-lg bg-gray-900 text-white border border-gray-700 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/50"
              aria-label="Filter stocks by symbol"
            />
          </div>
          
          <div className="flex gap-2">
            <button
              onClick={() => handleSortChange('symbol')}
              className={`px-4 py-2 rounded-lg transition-colors ${
                sortBy === 'symbol' 
                  ? 'bg-blue-600 text-white' 
                  : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
              }`}
              aria-label={`Sort by symbol ${sortBy === 'symbol' ? `(${sortOrder})` : ''}`}
            >
              Symbol {sortBy === 'symbol' && (sortOrder === 'asc' ? '↑' : '↓')}
            </button>
            <button
              onClick={() => handleSortChange('price')}
              className={`px-4 py-2 rounded-lg transition-colors ${
                sortBy === 'price' 
                  ? 'bg-blue-600 text-white' 
                  : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
              }`}
              aria-label={`Sort by price ${sortBy === 'price' ? `(${sortOrder})` : ''}`}
            >
              Price {sortBy === 'price' && (sortOrder === 'asc' ? '↑' : '↓')}
            </button>
            <button
              onClick={() => handleSortChange('change')}
              className={`px-4 py-2 rounded-lg transition-colors ${
                sortBy === 'change' 
                  ? 'bg-blue-600 text-white' 
                  : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
              }`}
              aria-label={`Sort by change ${sortBy === 'change' ? `(${sortOrder})` : ''}`}
            >
              Change {sortBy === 'change' && (sortOrder === 'asc' ? '↑' : '↓')}
            </button>
          </div>
        </div>
      </div>

      {/* Stock List */}
      <div className="bg-gray-800/50 backdrop-blur-sm border border-gray-700 rounded-lg overflow-hidden">
        <div className="max-h-96 overflow-y-auto">
          {processedStocks.length === 0 ? (
            <div className="p-8 text-center text-gray-400">
              No stocks found matching "{filter}"
            </div>
          ) : (
            <div className="divide-y divide-gray-700">
              {processedStocks.map(stock => (
                <StockRow 
                  key={stock.symbol}
                  stock={stock}
                  isSelected={selectedStocks.includes(stock.symbol)}
                  onToggle={handleStockToggle}
                />
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Stock Comparison */}
      {selectedStocks.length > 0 && (
        <StockComparison 
          stocks={stocks.filter(s => selectedStocks.includes(s.symbol))} 
        />
      )}
    </div>
  );
};

// Memoized row component to prevent unnecessary re-renders
const StockRow = React.memo<{
  stock: StockData;
  isSelected: boolean;
  onToggle: (symbol: string) => void;
}>(({ stock, isSelected, onToggle }) => {
  const isPositive = stock.change >= 0;

  return (
    <div 
      className={`p-4 cursor-pointer transition-all ${
        isSelected 
          ? 'bg-blue-900/30 hover:bg-blue-900/40' 
          : 'hover:bg-gray-700/50'
      }`}
      onClick={() => onToggle(stock.symbol)}
      role="button"
      tabIndex={0}
      onKeyPress={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          onToggle(stock.symbol);
        }
      }}
      aria-pressed={isSelected}
      aria-label={`${stock.symbol}: $${stock.price.toFixed(2)}, ${isPositive ? 'up' : 'down'} ${Math.abs(stock.change).toFixed(2)}%`}
    >
      <div className="flex items-center justify-between gap-4">
        {/* Symbol */}
        <div className="flex items-center gap-3 min-w-0 flex-1">
          <div className={`w-2 h-2 rounded-full ${isSelected ? 'bg-blue-500' : 'bg-gray-600'}`}></div>
          <span className="font-bold text-lg">{stock.symbol}</span>
        </div>

        {/* Price */}
        <div className="text-right min-w-[100px]">
          <div className="text-xl font-semibold">${stock.price.toFixed(2)}</div>
        </div>

        {/* Change */}
        <div className="text-right min-w-[80px]">
          <div className={`text-lg font-semibold ${isPositive ? 'text-green-500' : 'text-red-500'}`}>
            {isPositive ? '+' : ''}{stock.change.toFixed(2)}%
          </div>
        </div>

        {/* Sparkline */}
        <div className="min-w-[120px]">
          <Sparkline 
            data={stock.sparkline} 
            color={isPositive ? '#10b981' : '#ef4444'}
          />
        </div>
      </div>
    </div>
  );
});
