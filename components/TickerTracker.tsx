import React, { useState, useEffect } from 'react';
import { StockData } from '../types';
import { fetchStockPerformance } from '../services/geminiService';

interface TickerTrackerProps {
  refreshTrigger: number;
}

const STORAGE_KEY = 'finagent_tickers';

export const TickerTracker: React.FC<TickerTrackerProps> = ({ refreshTrigger }) => {
  const [tickers, setTickers] = useState<string[]>([]);
  const [stockData, setStockData] = useState<StockData[]>([]);
  const [loading, setLoading] = useState(false);
  const [inputVal, setInputVal] = useState('');

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
    }
  };

  const handleRemoveTicker = (symbol: string) => {
    setTickers((prev) => prev.filter((t) => t !== symbol));
  };

  const getChangeColor = (val: string) => {
    if (val.includes('-')) return 'text-red-400';
    if (val.includes('+')) return 'text-emerald-400';
    return 'text-slate-400';
  };

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
          className="flex-1 bg-slate-900 border border-slate-700 text-slate-200 text-sm rounded-lg focus:ring-emerald-500 focus:border-emerald-500 p-2.5 uppercase placeholder:normal-case"
        />
        <button
          type="submit"
          className="bg-emerald-600 hover:bg-emerald-700 text-white p-2.5 rounded-lg transition-colors"
          disabled={!inputVal.trim()}
        >
          <i className="fas fa-plus"></i>
        </button>
      </form>

      {tickers.length === 0 ? (
        <p className="text-slate-500 text-sm italic text-center py-4">
          Add stocks to track performance.
        </p>
      ) : (
        <div className="space-y-2">
          {stockData.length === 0 && loading ? (
             // Skeleton loading for initial data
             Array.from({length: tickers.length}).map((_, i) => (
                 <div key={i} className="h-12 bg-slate-900/50 rounded-lg animate-pulse"></div>
             ))
          ) : (
             // Use stockData if available, otherwise fallback to ticker list so user can still delete
             tickers.map((ticker) => {
                const data = stockData.find(d => d.symbol === ticker);
                return (
                    <div key={ticker} className="flex items-center justify-between p-3 bg-slate-900/40 rounded-lg border border-slate-800 hover:border-slate-600 transition-colors group">
                        <div className="flex flex-col">
                            <span className="font-bold text-slate-200">{ticker}</span>
                            {data ? (
                                <span className="text-xs text-slate-400">${data.price}</span>
                            ) : (
                                <span className="text-xs text-slate-500">Loading...</span>
                            )}
                        </div>
                        <div className="flex items-center gap-3">
                            {data && (
                                <div className={`text-right text-sm font-medium ${getChangeColor(data.percentChange)}`}>
                                    <div>{data.percentChange}</div>
                                    <div className="text-[10px] opacity-75">{data.change}</div>
                                </div>
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
          )}
        </div>
      )}
    </div>
  );
};
