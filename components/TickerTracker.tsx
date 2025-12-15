import React, { useState, useEffect, useMemo } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { StockData } from '../types';
import { fetchStockPerformance } from '../services/geminiService';

interface TickerTrackerProps {
  refreshTrigger: number;
}

const STORAGE_KEY = 'finagent_tickers';
type SortKey = 'symbol' | 'price' | 'change';
type SortDirection = 'asc' | 'desc';

const COMPARE_COLORS = ['#10b981', '#3b82f6', '#f43f5e']; // Emerald, Blue, Rose

// Simple SVG Sparkline Component (kept for the list view)
const SimpleSparkline = ({ data, color }: { data: number[], color: string }) => {
  if (!data || data.length < 2) return null;
  
  const min = Math.min(...data);
  const max = Math.max(...data);
  const range = max - min || 1;
  const width = 60;
  const height = 24;
  
  const points = data.map((val, i) => {
    const x = (i / (data.length - 1)) * width;
    const y = height - ((val - min) / range) * (height - 4) - 2; 
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
      <circle 
        cx={width} 
        cy={height - ((data[data.length - 1] - min) / range) * (height - 4) - 2} 
        r="1.5" 
        className={`${color} fill-current`} 
      />
    </svg>
  );
};

// Comparison Section Component
const ComparisonSection = ({ 
  tickers, 
  allStockData, 
  onClose 
}: { 
  tickers: string[], 
  allStockData: StockData[], 
  onClose: () => void 
}) => {
  if (tickers.length === 0) return null;

  // Prepare data for the chart
  // We want to show Percentage Return over the 5 days relative to Day 1
  const chartData = useMemo(() => {
    const dataPoints = [];
    const days = ['T-4', 'T-3', 'T-2', 'Yesterday', 'Today'];

    for (let i = 0; i < 5; i++) {
      const point: any = { day: days[i] };
      tickers.forEach(symbol => {
        const stock = allStockData.find(s => s.symbol === symbol);
        if (stock && stock.sparkline && stock.sparkline[i] !== undefined) {
           const startPrice = stock.sparkline[0];
           const currentPrice = stock.sparkline[i];
           // Calculate percentage change relative to start of period
           const pctChange = startPrice !== 0 ? ((currentPrice - startPrice) / startPrice) * 100 : 0;
           point[symbol] = pctChange;
        }
      });
      dataPoints.push(point);
    }
    return dataPoints;
  }, [tickers, allStockData]);

  const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-slate-900/95 backdrop-blur-sm border border-slate-700 p-3 rounded-lg shadow-xl z-50 text-xs">
          <p className="text-slate-400 mb-2">{label}</p>
          {payload.map((entry: any, index: number) => (
            <div key={index} className="flex items-center gap-2 mb-1 last:mb-0">
               <div className="w-2 h-2 rounded-full" style={{ backgroundColor: entry.color }}></div>
               <span className="font-semibold text-slate-200">{entry.name}:</span>
               <span className={`${entry.value >= 0 ? 'text-emerald-400' : 'text-red-400'}`}>
                 {Number(entry.value).toFixed(2)}%
               </span>
            </div>
          ))}
        </div>
      );
    }
    return null;
  };

  return (
    <div className="mb-6 bg-gradient-to-b from-slate-900 to-slate-800/50 rounded-xl border border-blue-500/20 p-5 shadow-inner animate-fade-in relative overflow-hidden">
       {/* Decorative background element */}
       <div className="absolute top-0 right-0 w-32 h-32 bg-blue-500/5 rounded-full blur-3xl -mr-16 -mt-16 pointer-events-none"></div>
       
       <div className="flex items-center justify-between mb-4 relative z-10">
          <h4 className="text-sm font-semibold text-slate-200 flex items-center gap-2">
             <i className="fas fa-chart-area text-blue-400"></i>
             Performance Comparison <span className="text-xs font-normal text-slate-500">(5-Day Trend)</span>
          </h4>
          <button onClick={onClose} className="text-xs text-slate-500 hover:text-slate-300 bg-slate-800/50 hover:bg-slate-700 px-2 py-1 rounded transition-colors">
             Close
          </button>
       </div>
       
       <div className="h-[200px] w-full relative z-10">
         <ResponsiveContainer width="100%" height="100%">
           <LineChart data={chartData} margin={{ top: 5, right: 5, bottom: 5, left: -20 }}>
             <CartesianGrid strokeDasharray="3 3" stroke="#334155" opacity={0.3} />
             <XAxis dataKey="day" stroke="#94a3b8" fontSize={10} tickLine={false} axisLine={false} />
             <YAxis stroke="#94a3b8" fontSize={10} tickLine={false} axisLine={false} tickFormatter={(val) => `${val.toFixed(1)}%`} />
             <Tooltip content={<CustomTooltip />} />
             <Legend wrapperStyle={{ fontSize: '10px', paddingTop: '10px' }} />
             {tickers.map((symbol, index) => (
               <Line 
                 key={symbol} 
                 type="monotone" 
                 dataKey={symbol} 
                 stroke={COMPARE_COLORS[index % COMPARE_COLORS.length]} 
                 strokeWidth={2}
                 dot={{ r: 3, fill: COMPARE_COLORS[index % COMPARE_COLORS.length], strokeWidth: 0 }}
                 activeDot={{ r: 5 }}
               />
             ))}
           </LineChart>
         </ResponsiveContainer>
       </div>
    </div>
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

  // Comparison State
  const [compareList, setCompareList] = useState<string[]>([]);

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
      setFilterQuery(''); 
    }
  };

  const handleRemoveTicker = (symbol: string) => {
    setTickers((prev) => prev.filter((t) => t !== symbol));
    setCompareList((prev) => prev.filter(t => t !== symbol)); // Remove from compare if deleted
  };

  const handleToggleCompare = (symbol: string) => {
    setCompareList(prev => {
       if (prev.includes(symbol)) {
         return prev.filter(s => s !== symbol);
       }
       if (prev.length >= 3) return prev; // Limit 3
       return [...prev, symbol];
    });
  };

  const handleSort = (key: SortKey) => {
    if (sortKey === key) {
      setSortDirection(prev => prev === 'asc' ? 'desc' : 'asc');
    } else {
      setSortKey(key);
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

      // Handle missing data
      if (!dataA && !dataB) return 0;
      if (!dataA) return 1;
      if (!dataB) return -1;

      if (sortKey === 'symbol') {
        return sortDirection === 'asc' 
          ? aSymbol.localeCompare(bSymbol) 
          : bSymbol.localeCompare(aSymbol);
      }

      let valA = 0;
      let valB = 0;

      if (sortKey === 'price') {
        valA = parseFloat(dataA.price.replace(/[^0-9.-]/g, '')) || 0;
        valB = parseFloat(dataB.price.replace(/[^0-9.-]/g, '')) || 0;
      } else if (sortKey === 'change') {
        valA = parseFloat(dataA.percentChange.replace(/[^0-9.-]/g, '')) || 0;
        valB = parseFloat(dataB.percentChange.replace(/[^0-9.-]/g, '')) || 0;
      }

      if (valA === valB) return 0;
      
      return sortDirection === 'asc' ? valA - valB : valB - valA;
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

      {/* Comparison Section */}
      <ComparisonSection 
        tickers={compareList} 
        allStockData={stockData} 
        onClose={() => setCompareList([])} 
      />

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
                        const isComparing = compareList.includes(ticker);
                        const canCompare = compareList.length < 3 || isComparing;
                        
                        return (
                            <div key={ticker} className={`flex items-center justify-between p-3 rounded-lg border transition-all group relative ${isComparing ? 'bg-slate-800 border-blue-500/50 shadow-md shadow-blue-500/10' : 'bg-slate-900/40 border-slate-800 hover:border-slate-600'}`}>
                                <div className="flex items-center gap-3">
                                   <button 
                                      onClick={() => handleToggleCompare(ticker)}
                                      disabled={!canCompare && !isComparing}
                                      className={`w-6 h-6 rounded flex items-center justify-center transition-colors ${isComparing ? 'bg-blue-600 text-white' : 'bg-slate-800 text-slate-500 hover:text-blue-400'} ${!canCompare && !isComparing ? 'opacity-30 cursor-not-allowed' : ''}`}
                                      title={isComparing ? "Remove from comparison" : (canCompare ? "Add to comparison" : "Max 3 selected")}
                                   >
                                     <i className="fas fa-balance-scale text-xs"></i>
                                   </button>
                                   <div className="flex flex-col w-16">
                                       <span className="font-bold text-slate-200">{ticker}</span>
                                       {data ? (
                                           <span className="text-xs text-slate-400">${data.price}</span>
                                       ) : (
                                           loading ? (
                                               <span className="text-xs text-slate-500">Updating...</span>
                                           ) : (
                                               <span className="text-xs text-red-400" title="Data unavailable">N/A</span>
                                           )
                                       )}
                                   </div>
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
            
            {/* Helper Text */}
            <div className="mt-3 text-[10px] text-slate-500 text-center">
              Select up to 3 stocks to compare 5-day performance trends.
            </div>
        </>
      )}
    </div>
  );
};