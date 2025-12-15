import React, { useEffect, useState, useCallback } from 'react';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Sector } from 'recharts';
import { SectorData, GroundingChunk } from '../types';
import { fetchSectorWeights } from '../services/geminiService';

// Fallback data in case API fails
const FALLBACK_DATA: SectorData[] = [
  { name: 'Info Tech', value: 29.5 },
  { name: 'Financials', value: 13.0 },
  { name: 'Health Care', value: 12.5 },
  { name: 'Consumer Disc', value: 10.3 },
  { name: 'Comm Services', value: 8.9 },
  { name: 'Industrials', value: 8.6 },
  { name: 'Consumer Staples', value: 6.0 },
  { name: 'Energy', value: 3.7 },
  { name: 'Others', value: 7.5 },
];

const COLORS = ['#10b981', '#3b82f6', '#6366f1', '#8b5cf6', '#ec4899', '#f43f5e', '#f97316', '#eab308', '#64748b', '#94a3b8', '#cbd5e1'];

const CustomTooltip = ({ active, payload }: any) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    return (
      <div className="bg-slate-900/95 backdrop-blur-sm border border-slate-700 p-3 rounded-lg shadow-xl z-50">
        <p className="text-slate-200 font-medium text-xs mb-1 uppercase tracking-wider">{data.name}</p>
        <div className="flex items-center gap-2">
           <div className="w-2 h-2 rounded-full" style={{ backgroundColor: payload[0].fill }}></div>
           <p className="text-white font-bold text-lg">
             {Number(data.value).toFixed(1)}%
           </p>
        </div>
      </div>
    );
  }
  return null;
};

const renderActiveShape = (props: any) => {
  const { cx, cy, innerRadius, outerRadius, startAngle, endAngle, fill } = props;
  return (
    <g>
      <text x={cx} y={cy} dy={-10} textAnchor="middle" fill="#94a3b8" fontSize={10}>
        Selected
      </text>
      <text x={cx} y={cy} dy={15} textAnchor="middle" fill="#fff" fontSize={16} fontWeight="bold">
        {props.payload.value.toFixed(1)}%
      </text>
      <Sector
        cx={cx}
        cy={cy}
        innerRadius={innerRadius}
        outerRadius={outerRadius + 8}
        startAngle={startAngle}
        endAngle={endAngle}
        fill={fill}
        stroke="#fff"
        strokeWidth={2}
      />
      <Sector
        cx={cx}
        cy={cy}
        startAngle={startAngle}
        endAngle={endAngle}
        innerRadius={innerRadius - 6}
        outerRadius={innerRadius}
        fill={fill}
        fillOpacity={0.3}
      />
    </g>
  );
};

interface MarketChartProps {
  refreshTrigger: number;
}

export const MarketChart: React.FC<MarketChartProps> = ({ refreshTrigger }) => {
  const [data, setData] = useState<SectorData[]>([]);
  const [sources, setSources] = useState<GroundingChunk[]>([]);
  const [loading, setLoading] = useState(true);
  
  // Interaction State
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null);
  const [selectedIndex, setSelectedIndex] = useState<number | null>(null);
  const [selectedSector, setSelectedSector] = useState<SectorData | null>(null);

  const fetchData = useCallback(async () => {
    setLoading(true);
    // Reset selection on refresh
    setSelectedIndex(null);
    setSelectedSector(null);
    try {
      const result = await fetchSectorWeights();
      if (result.data && result.data.length > 0) {
        setData(result.data);
        setSources(result.sources);
      } else {
        setData(FALLBACK_DATA);
        setSources([]);
      }
    } catch (err) {
      setData(FALLBACK_DATA);
      setSources([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [refreshTrigger, fetchData]);

  const onPieEnter = (_: any, index: number) => {
    setHoveredIndex(index);
  };

  const onPieLeave = () => {
    setHoveredIndex(null);
  };

  const onPieClick = (entry: any, index: number) => {
    if (selectedIndex === index) {
        // Deselect if clicked again
        setSelectedIndex(null);
        setSelectedSector(null);
    } else {
        setSelectedIndex(index);
        // Recharts passes the data object directly or in payload
        setSelectedSector(entry.payload || entry);
    }
  };

  // Determine which index is "active" for the shape rendering
  // Priority: Hovered > Selected
  const activeIndex = hoveredIndex !== null ? hoveredIndex : (selectedIndex !== null ? selectedIndex : -1);

  return (
    <div className="bg-slate-800 rounded-xl p-6 shadow-lg border border-slate-700 flex flex-col h-auto" data-testid="market-chart-container">
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-lg font-semibold text-slate-200">
          S&P 500 Sector Allocation
        </h3>
        <div className="flex items-center gap-3">
          {loading ? (
             <span className="text-xs font-normal text-emerald-400 flex items-center gap-1" data-testid="chart-loading-indicator">
                 <i className="fas fa-sync fa-spin"></i> 
                 <span className="hidden sm:inline">Updating</span>
             </span>
          ) : (
             <span className="text-xs font-normal text-slate-400" data-testid="chart-data-source">
                {data === FALLBACK_DATA ? '(Reference Data)' : '(Live Estimate)'}
             </span>
          )}
          
          <button
            onClick={fetchData}
            disabled={loading}
            className="w-8 h-8 flex items-center justify-center rounded-lg bg-slate-700 hover:bg-slate-600 text-slate-300 hover:text-emerald-400 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            aria-label="Refresh Chart Data"
            title="Refresh Chart Data"
          >
            <i className={`fas fa-redo-alt text-xs ${loading ? 'animate-spin' : ''}`}></i>
          </button>
        </div>
      </div>
      
      <div className="w-full h-[250px] relative">
        {loading && data.length === 0 ? (
           <div className="absolute inset-0 flex items-center justify-center">
             <div className="flex flex-col items-center gap-3">
               <div className="w-8 h-8 border-2 border-emerald-500 border-t-transparent rounded-full animate-spin"></div>
               <p className="text-xs text-slate-500">Fetching latest weights...</p>
             </div>
           </div>
        ) : (
            <div className={loading ? "opacity-50 transition-opacity" : "opacity-100 transition-opacity"}>
                <ResponsiveContainer width="100%" height="100%" minHeight={250}>
                <PieChart>
                    <Pie
                        data={data}
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={80}
                        paddingAngle={4}
                        dataKey="value"
                        nameKey="name"
                        stroke="none"
                        {...({ activeIndex } as any)}
                        activeShape={renderActiveShape}
                        onMouseEnter={onPieEnter}
                        onMouseLeave={onPieLeave}
                        onClick={onPieClick}
                        className="cursor-pointer focus:outline-none"
                    >
                    {data.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} stroke={selectedIndex === index ? '#fff' : 'none'} strokeWidth={selectedIndex === index ? 2 : 0} />
                    ))}
                    </Pie>
                    <Tooltip content={<CustomTooltip />} />
                </PieChart>
                </ResponsiveContainer>
            </div>
        )}
      </div>

      {/* Selected Sector Details Tooltip (Below Chart) */}
      <div className={`mt-4 transition-all duration-300 overflow-hidden ${selectedSector ? 'max-h-24 opacity-100' : 'max-h-0 opacity-0'}`}>
        {selectedSector && (
            <div className="bg-slate-700/50 rounded-lg p-3 border border-slate-600 flex items-center justify-between">
                <div>
                    <p className="text-xs text-slate-400 uppercase tracking-wider">Selected Sector</p>
                    <p className="text-sm font-bold text-white">{selectedSector.name}</p>
                </div>
                <div className="text-right">
                     <p className="text-xl font-bold text-emerald-400">{Number(selectedSector.value).toFixed(2)}%</p>
                </div>
            </div>
        )}
      </div>

      {/* Custom Legend */}
      {data.length > 0 && (
        <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-x-4 gap-y-2">
            {data.map((entry, index) => (
                <div 
                    key={`legend-${index}`} 
                    className={`flex items-center gap-2 group cursor-pointer p-1 rounded transition-colors ${selectedIndex === index ? 'bg-slate-700/50 ring-1 ring-slate-600' : 'hover:bg-slate-800/50'}`}
                    onClick={() => onPieClick(entry, index)}
                >
                    <div className="w-2.5 h-2.5 rounded-full shrink-0" style={{ backgroundColor: COLORS[index % COLORS.length] }}></div>
                    <div className="flex items-center justify-between w-full min-w-0">
                        <span className={`text-xs truncate transition-colors ${selectedIndex === index ? 'text-white font-medium' : 'text-slate-400 group-hover:text-slate-200'}`} title={entry.name}>
                            {entry.name}
                        </span>
                        <span className={`text-xs font-medium ml-2 ${selectedIndex === index ? 'text-emerald-400' : 'text-slate-300'}`}>
                             {Number(entry.value).toFixed(1)}%
                        </span>
                    </div>
                </div>
            ))}
        </div>
      )}

      <div className="mt-6 border-t border-slate-700/50 pt-4">
        <p className="text-[10px] text-slate-500 text-center italic opacity-60 mb-2">
           Data retrieved via AI search grounding.
        </p>
        {sources.length > 0 && (
            <div className="flex flex-wrap gap-2 justify-center" data-testid="chart-sources">
                {sources.map((s, i) => (
                    <a key={i} href={s.web?.uri} target="_blank" rel="noreferrer" className="text-[10px] text-blue-400 hover:underline">
                        {s.web?.title}
                    </a>
                ))}
            </div>
        )}
      </div>
    </div>
  );
};