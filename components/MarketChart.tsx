import React, { useState } from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import { SectorData } from '../services/geminiService';

interface MarketChartProps {
  data: SectorData[];
}

const CustomTooltip = ({ active, payload }: any) => {
  if (active && payload && payload.length) {
    const data = payload[0];
    return (
      <div className="bg-gray-800 border border-gray-700 rounded-lg p-3 shadow-lg">
        <p className="text-white font-semibold">{data.name}</p>
        <p className="text-blue-400">{data.value}%</p>
      </div>
    );
  }
  return null;
};

export const MarketChart: React.FC<MarketChartProps> = ({ data }) => {
  const [activeIndex, setActiveIndex] = useState<number | null>(null);

  const onPieEnter = (_: any, index: number) => {
    setActiveIndex(index);
  };

  const onPieLeave = () => {
    setActiveIndex(null);
  };

  return (
    <div 
      data-testid="market-chart-container"
      className="bg-gray-800/50 backdrop-blur-sm border border-gray-700 rounded-lg p-6"
    >
      <h3 className="text-xl font-semibold mb-6 flex items-center gap-2">
        <svg 
          className="w-6 h-6 text-blue-400" 
          fill="none" 
          viewBox="0 0 24 24" 
          stroke="currentColor"
        >
          <path 
            strokeLinecap="round" 
            strokeLinejoin="round" 
            strokeWidth={2} 
            d="M11 3.055A9.001 9.001 0 1020.945 13H11V3.055z" 
          />
          <path 
            strokeLinecap="round" 
            strokeLinejoin="round" 
            strokeWidth={2} 
            d="M20.488 9H15V3.512A9.025 9.025 0 0120.488 9z" 
          />
        </svg>
        S&P 500 Sector Allocation
      </h3>

      <div 
        role="img" 
        aria-label="S&P 500 sector allocation pie chart showing the percentage distribution across different market sectors"
      >
        <ResponsiveContainer width="100%" height={300}>
          <PieChart>
            <Pie
              data={data}
              cx="50%"
              cy="50%"
              labelLine={false}
              label={({ name, value }) => `${name}: ${value}%`}
              outerRadius={100}
              fill="#8884d8"
              dataKey="value"
              onMouseEnter={onPieEnter}
              onMouseLeave={onPieLeave}
            >
              {data.map((entry, index) => (
                <Cell 
                  key={`cell-${index}`} 
                  fill={entry.fill}
                  opacity={activeIndex === null || activeIndex === index ? 1 : 0.6}
                />
              ))}
            </Pie>
            <Tooltip content={<CustomTooltip />} />
            <Legend 
              verticalAlign="bottom" 
              height={36}
              formatter={(value) => <span className="text-gray-300 text-sm">{value}</span>}
            />
          </PieChart>
        </ResponsiveContainer>
      </div>

      {/* Sector Details List */}
      <div className="mt-6 space-y-2">
        <h4 className="text-sm font-semibold text-gray-400 mb-3">Sector Breakdown</h4>
        {data.map((sector, index) => (
          <div 
            key={index}
            className="flex items-center justify-between p-2 rounded hover:bg-gray-700/50 transition-colors"
            onMouseEnter={() => setActiveIndex(index)}
            onMouseLeave={() => setActiveIndex(null)}
          >
            <div className="flex items-center gap-3">
              <div 
                className="w-3 h-3 rounded-full" 
                style={{ backgroundColor: sector.fill }}
              ></div>
              <span className="text-sm text-gray-300">{sector.name}</span>
            </div>
            <span className="text-sm font-semibold text-white">{sector.value}%</span>
          </div>
        ))}
      </div>
    </div>
  );
};
