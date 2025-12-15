import React, { useState, useEffect, useMemo } from 'react';
import { StockData } from '../types';
import { fetchStockPerformance } from '../services/geminiService';

interface TickerTrackerProps {
  refreshTrigger: number;
}

const STORAGE_KEY = 'finagent_tickers';
type SortKey = 'symbol' | 'price' | 'change';
type SortDirection = 'asc' | 'desc';

// Simple SVG Sparkline Component
const SimpleSparkline = ({ data, color }: { data: number[], color: string }) => {
  if (!data || data.length < 2) return null;
  
  const min = Math.min(...data);
  const max = Math.max(...data);
  const range = max - min || 1;
  const width = 60;
  const height = 24;
  
  const points = data.map((val, i) => {
    const x = (i / (data.length - 1)) * width;
    // Invert Y because SVG coordinates go down
    const y = height - ((val - min) / range) * (height - 4) - 2; // -4 and -2 for padding
    return `${x},${y}`;
  }).join(' ');

  return (
    <svg width={width} height={height} className="overflow-visible opacity-80" aria-hidden="true">
      <polyline
        points={points}
        fill="none"
        stroke="currentColor"
        strokeWidth="1.5"
        className={color}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      {/* Small dot at the end */}
      <circle 
        cx={width} 
        cy={height - ((data[data.length - 1] - min) / range) * (height - 4) - 2} 
        r="1.5" 
        className={`${color} fill-current`} 
      />
    </svg>
  );
};

export const TickerTracker: React.FC<TickerTrackerProps> = ({ refreshTrigger }) => {
  const [tickers, setTickers] = useState<string[]>([]);
  const [stockData, setStockData] = useState<StockData[]>([]);
  const [loading, setLoading] = useState(false);
  const [inputVal, setInputVal] = useState('');
  const [filterQuery, setFilterQuery] = useState('');
  
  // Sorting State
  const [sortKey, setSortKey] = useState<SortKey>('symbol');
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc');

  // Load tickers from local storage on mount
  useEffect(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        setTickers(JSON.parse(saved));
      }
    } catch (e) {
      console.error('Failed to load tickers', e);
    }
  }, []);

  // Save tickers when changed
  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(tickers));
  }, [tickers]);

  // Fetch data when tickers change or refresh triggered
  useEffect(() => {
    if (tickers.length === 0) {
      setStockData([]);
      return;
    }

    const fetchData = async () => {
      setLoading(true);
      try {
        const data = await fetchStockPerformance(tickers);
        setStockData(data);
      } catch (err) {
        console.error(err);
        // We keep previous data or empty, UI handles missing data
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [tickers, refreshTrigger]);

  const handleAddTicker = (e: React.FormEvent) => {
    e.preventDefault();
    const normalized = inputVal.trim().toUpperCase();
    if (normalized && !tickers.includes(normalized)) {
      setTickers((prev) => [...prev, normalized]);
      setInputVal('');
      setFilterQuery(''); 
    }
  };

  const handleRemoveTicker = (symbol: string) => {
    setTickers((prev) => prev.filter((t) => t !== symbol));
  };

  const handleSort = (key: SortKey) => {
    if (sortKey === key) {
      setSortDirection(prev => prev === 'asc' ? 'desc' : 'asc');
    } else {
      setSortKey(key);
      // Smart defaults: Symbol -> ASC, Numbers -> DESC
      setSortDirection(key === 'symbol' ? 'asc' : 'desc');
    }
  };

  const getChangeColor = (val: string) => {
    if (val.includes('-')) return 'text-red-400';
    if (val.includes('+')) return 'text-emerald-400';
    return 'text-slate-400';
  };

  const filteredAndSortedTickers = useMemo(() => {
    // 1. Filter
    let result = tickers;
    if (filterQuery) {
      result = result.filter(t => t.includes(filterQuery.toUpperCase()));
    }

    // 2. Sort
    return [...result].sort((aSymbol, bSymbol) => {
      const dataA = stockData.find(d => d.symbol === aSymbol);
      const dataB = stockData.find(d => d.symbol === bSymbol);

      let valA: string | number = '';
      let valB: string | number = '';

      if (sortKey === 'symbol') {
        valA = aSymbol;
        valB = bSymbol;
      } else if (sortKey === 'price') {
        // Treat missing data as -999999 to push to bottom in desc sort, or top in asc
        valA = dataA ? parseFloat(dataA.price.replace(/[^0-9.-]/g, '')) || 0 : -999999;
        valB = dataB ? parseFloat(dataB.price.replace(/[^0-9.-]/g, '')) || 0 : -999999;
      } else if (sortKey === 'change') {
        valA = dataA ? parseFloat(dataA.percentChange.replace(/[^0-9.-]/g, '')) || 0 : -999999;
        valB = dataB ? parseFloat(dataB.percentChange.replace(/[^0-9.-]/g, '')) || 0 : -999999;
      }

      if (valA === valB) return 0;
      
      const comparison = valA > valB ? 1 : -1;
      return sortDirection === 'asc' ? comparison : -comparison;
    });
  }, [tickers, filterQuery, stockData, sortKey, sortDirection]);

  return (
    <div className="bg-slate-800 rounded-xl p-6 shadow-lg border border-slate-700">
      <h3 className="text-white font-semibold mb-4 flex items-center justify-between">
        <span className="flex items-center gap-2">
          <i className="fas fa-list-ul text-emerald-500"></i> Watchlist
        </span>
        {loading && <i className="fas fa-circle-notch fa-spin text-emerald-500 text-xs"></i>}
      </h3>

      <form onSubmit={handleAddTicker} className="flex gap-2 mb-4">
        <input
          type="text"
          value={inputVal}
          onChange={(e) => setInputVal(e.target.value)}
          placeholder="Symbol (e.g. AAPL)"
          className="flex-1 bg-slate-900 border border-slate-700 text-slate-200 text-sm rounded-lg focus:ring-emerald-500 focus:border-emerald-500 p-2.5 uppercase placeholder:normal-case transition-all"
        />
        <button
          type="submit"
          className="bg-emerald-600 hover:bg-emerald-700 text-white p-2.5 rounded-lg transition-colors"
          disabled={!inputVal.trim()}
          aria-label="Add Ticker"
        >
          <i className="fas fa-plus"></i>
        </button>
      </form>

      {tickers.length === 0 ? (
        <p className="text-slate-500 text-sm italic text-center py-4">
          Add stocks to track performance.
        </p>
      ) : (
        <>
            <div className="flex gap-2 mb-3">
                <div className="relative group flex-1">
                    <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                        <i className="fas fa-search text-slate-500 group-focus-within:text-emerald-500 transition-colors text-xs"></i>
                    </div>
                    <input 
                        type="text" 
                        value={filterQuery}
                        onChange={(e) => setFilterQuery(e.target.value)}
                        placeholder="Filter..." 
                        className="w-full bg-slate-900/30 border border-slate-700/50 text-slate-300 text-xs rounded-lg focus:ring-1 focus:ring-emerald-500 focus:border-emerald-500 pl-9 p-2 placeholder:text-slate-600 transition-all outline-none"
                        aria-label="Filter watchlist"
                    />
                </div>
                
                <div className="flex items-center bg-slate-900/50 rounded-lg border border-slate-700/50 p-1">
                     <button
                        onClick={() => handleSort('symbol')}
                        className={`w-7 h-7 flex items-center justify-center rounded transition-colors ${sortKey === 'symbol' ? 'bg-slate-700 text-emerald-400' : 'text-slate-500 hover:text-slate-300'}`}
                        title="Sort by Symbol"
                     >
                        <i className="fas fa-font text-xs"></i>
                     </button>
                     <button
                        onClick={() => handleSort('price')}
                        className={`w-7 h-7 flex items-center justify-center rounded transition-colors ${sortKey === 'price' ? 'bg-slate-700 text-emerald-400' : 'text-slate-500 hover:text-slate-300'}`}
                        title="Sort by Price"
                     >
                        <i className="fas fa-dollar-sign text-xs"></i>
                     </button>
                     <button
                        onClick={() => handleSort('change')}
                        className={`w-7 h-7 flex items-center justify-center rounded transition-colors ${sortKey === 'change' ? 'bg-slate-700 text-emerald-400' : 'text-slate-500 hover:text-slate-300'}`}
                        title="Sort by Performance"
                     >
                        <i className="fas fa-chart-line text-xs"></i>
                     </button>
                     <div className="w-px h-4 bg-slate-700 mx-1"></div>
                     <button
                        onClick={() => setSortDirection(d => d === 'asc' ? 'desc' : 'asc')}
                        className="w-7 h-7 flex items-center justify-center rounded text-slate-400 hover:text-white transition-colors"
                        title={sortDirection === 'asc' ? 'Ascending' : 'Descending'}
                     >
                        <i className={`fas fa-sort-amount-${sortDirection === 'asc' ? 'down' : 'up'} text-xs`}></i>
                     </button>
                </div>
            </div>

            <div className="space-y-2">
            {stockData.length === 0 && loading ? (
                // Skeleton loading for initial data
                Array.from({length: filteredAndSortedTickers.length || 1}).map((_, i) => (
                    <div key={i} className="h-12 bg-slate-900/50 rounded-lg animate-pulse"></div>
                ))
            ) : (
                filteredAndSortedTickers.length === 0 ? (
                    <div className="text-center py-3 text-slate-500 text-xs italic">
                        No matching symbols found.
                    </div>
                ) : (
                    filteredAndSortedTickers.map((ticker) => {
                        const data = stockData.find(d => d.symbol === ticker);
                        const changeColor = data ? getChangeColor(data.percentChange) : 'text-slate-400';
                        
                        return (
                            <div key={ticker} className="flex items-center justify-between p-3 bg-slate-900/40 rounded-lg border border-slate-800 hover:border-slate-600 transition-colors group">
                                <div className="flex flex-col w-20">
                                    <span className="font-bold text-slate-200">{ticker}</span>
                                    {data ? (
                                        <span className="text-xs text-slate-400">${data.price}</span>
                                    ) : (
                                        loading ? (
                                            <span className="text-xs text-slate-500">Updating...</span>
                                        ) : (
                                            <span className="text-xs text-red-400" title="Data unavailable">Unavailable</span>
                                        )
                                    )}
                                </div>

                                <div className="flex-1 flex justify-center px-2">
                                    {data && data.sparkline && data.sparkline.length > 0 && (
                                        <SimpleSparkline data={data.sparkline} color={changeColor} />
                                    )}
                                </div>

                                <div className="flex items-center gap-3">
                                    {data ? (
                                        <div className={`text-right text-sm font-medium ${changeColor}`}>
                                            <div>{data.percentChange}</div>
                                            <div className="text-[10px] opacity-75">{data.change}</div>
                                        </div>
                                    ) : (
                                        !loading && <span className="text-xs text-slate-600">-</span>
                                    )}
                                    <button 
                                        onClick={() => handleRemoveTicker(ticker)}
                                        className="text-slate-600 hover:text-red-400 transition-colors opacity-0 group-hover:opacity-100"
                                        aria-label={`Remove ${ticker}`}
                                    >
                                        <i className="fas fa-times"></i>
                                    </button>
                                </div>
                            </div>
                        );
                    })
                )
            )}
            </div>
        </>
      )}
    </div>
  );
};