import React, { useEffect, useState } from 'react';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts';
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

interface MarketChartProps {
  refreshTrigger: number;
}

export const MarketChart: React.FC<MarketChartProps> = ({ refreshTrigger }) => {
  const [data, setData] = useState<SectorData[]>([]);
  const [sources, setSources] = useState<GroundingChunk[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;
    setLoading(true);

    const getData = async () => {
      try {
        const result = await fetchSectorWeights();
        if (isMounted) {
          if (result.data && result.data.length > 0) {
            setData(result.data);
            setSources(result.sources);
          } else {
            setData(FALLBACK_DATA);
            setSources([]);
          }
        }
      } catch (err) {
        if (isMounted) {
            setData(FALLBACK_DATA);
            setSources([]);
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    getData();

    return () => {
      isMounted = false;
    };
  }, [refreshTrigger]);

  return (
    <div className="bg-slate-800 rounded-xl p-6 shadow-lg border border-slate-700 min-h-[400px] flex flex-col" data-testid="market-chart-container">
      <h3 className="text-lg font-semibold text-slate-200 mb-4 flex items-center justify-between">
        <span>S&P 500 Sector Allocation</span>
        {loading ? (
             <span className="text-xs font-normal text-emerald-400 flex items-center gap-1" data-testid="chart-loading-indicator">
                 <i className="fas fa-sync fa-spin"></i> Live Data
             </span>
        ) : (
             <span className="text-xs font-normal text-slate-400" data-testid="chart-data-source">
                {data === FALLBACK_DATA ? '(Reference Data)' : '(Live Estimate)'}
             </span>
        )}
      </h3>
      
      <div className="flex-grow w-full relative">
        {loading ? (
           <div className="absolute inset-0 flex items-center justify-center">
             <div className="flex flex-col items-center gap-3">
               <div className="w-8 h-8 border-2 border-emerald-500 border-t-transparent rounded-full animate-spin"></div>
               <p className="text-xs text-slate-500">Fetching latest weights...</p>
             </div>
           </div>
        ) : (
            <ResponsiveContainer width="100%" height="100%" minHeight={300}>
            <PieChart>
                <Pie
                data={data}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={80}
                fill="#8884d8"
                paddingAngle={5}
                dataKey="value"
                nameKey="name"
                >
                {data.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
                </Pie>
                <Tooltip content={<CustomTooltip />} />
                <Legend 
                    layout="vertical" 
                    verticalAlign="middle" 
                    align="right"
                    wrapperStyle={{ fontSize: '11px', color: '#94a3b8' }} 
                />
            </PieChart>
            </ResponsiveContainer>
        )}
      </div>
      <div className="mt-4">
        <p className="text-xs text-slate-500 text-center italic opacity-60 mb-2">
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