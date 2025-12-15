import React from 'react';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { SectorData } from '../types';

// Static representation of S&P 500 approximate sector weights for visualization purposes
// Since getting real-time weights requires a paid specific API, we use this as a reference visual.
const data: SectorData[] = [
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

const COLORS = ['#10b981', '#3b82f6', '#6366f1', '#8b5cf6', '#ec4899', '#f43f5e', '#f97316', '#eab308', '#64748b'];

export const MarketChart: React.FC = () => {
  return (
    <div className="bg-slate-800 rounded-xl p-6 shadow-lg border border-slate-700">
      <h3 className="text-lg font-semibold text-slate-200 mb-4">S&P 500 Sector Allocation (Reference)</h3>
      <div className="h-[300px] w-full">
        <ResponsiveContainer width="100%" height="100%">
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
            >
              {data.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip 
                contentStyle={{ backgroundColor: '#1e293b', borderColor: '#334155', color: '#f1f5f9' }}
                itemStyle={{ color: '#f1f5f9' }}
            />
            <Legend wrapperStyle={{ fontSize: '12px', color: '#94a3b8' }} />
          </PieChart>
        </ResponsiveContainer>
      </div>
      <p className="text-xs text-slate-500 mt-2 text-center italic">
        *Allocations are approximate and for visual reference.
      </p>
    </div>
  );
};
