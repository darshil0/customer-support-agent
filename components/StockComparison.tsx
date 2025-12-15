import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { StockData } from '../services/geminiService';

interface StockComparisonProps {
  stocks: StockData[];
}

export const StockComparison: React.FC<StockComparisonProps> = ({ stocks }) => {
  if (!stocks || stocks.length === 0) {
    return null;
  }

  // Transform data for recharts
  const chartData = Array.from({ length: 5 }, (_, index) => {
    const dataPoint: any = { day: `Day ${index + 1}` };
    
    stocks.forEach(stock => {
      if (stock.sparkline && stock.sparkline[index] !== undefined) {
        dataPoint[stock.symbol] = stock.sparkline[index];
      }
    });
    
    return dataPoint;
  });

  const colors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'];

  const CustomTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-gray-800 border border-gray-700 rounded-lg p-3 shadow-lg">
          <p className="text-white font-semibold mb-2">{payload[0].payload.day}</p>
          {payload.map((entry: any, index: number) => (
            <p key={index} style={{ color: entry.color }} className="text-sm">
              {entry.name}: ${entry.value.toFixed(2)}
            </p>
          ))}
        </div>
      );
    }
    return null;
  };

  return (
    <div className="bg-gray-800/50 backdrop-blur-sm border border-gray-700 rounded-lg p-6">
      <h3 className="text-xl font-semibold mb-6 flex items-center gap-2">
        <svg 
          className="w-6 h-6 text-purple-400" 
          fill="none" 
          viewBox="0 0 24 24" 
          stroke="currentColor"
        >
          <path 
            strokeLinecap="round" 
            strokeLinejoin="round" 
            strokeWidth={2} 
            d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" 
          />
        </svg>
        Stock Comparison
      </h3>

      {/* Comparison Chart */}
      <div aria-label="Stock comparison chart showing 5-day price trends">
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
            <XAxis 
              dataKey="day" 
              stroke="#9ca3af"
              style={{ fontSize: '12px' }}
            />
            <YAxis 
              stroke="#9ca3af"
              style={{ fontSize: '12px' }}
              tickFormatter={(value) => `$${value.toFixed(0)}`}
            />
            <Tooltip content={<CustomTooltip />} />
            <Legend 
              wrapperStyle={{ fontSize: '14px' }}
              formatter={(value) => <span className="text-gray-300">{value}</span>}
            />
            {stocks.map((stock, index) => (
              <Line
                key={stock.symbol}
                type="monotone"
                dataKey={stock.symbol}
                stroke={colors[index % colors.length]}
                strokeWidth={2}
                dot={{ r: 4 }}
                activeDot={{ r: 6 }}
              />
            ))}
          </LineChart>
        </ResponsiveContainer>
      </div>

      {/* Comparison Table */}
      <div className="mt-6 overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-700">
              <th className="text-left py-3 px-4 text-gray-400 font-semibold">Symbol</th>
              <th className="text-right py-3 px-4 text-gray-400 font-semibold">Current Price</th>
              <th className="text-right py-3 px-4 text-gray-400 font-semibold">Change</th>
              <th className="text-right py-3 px-4 text-gray-400 font-semibold">5-Day Range</th>
            </tr>
          </thead>
          <tbody>
            {stocks.map((stock, index) => {
              const min = Math.min(...stock.sparkline);
              const max = Math.max(...stock.sparkline);
              const isPositive = stock.change >= 0;

              return (
                <tr key={stock.symbol} className="border-b border-gray-700/50">
                  <td className="py-3 px-4">
                    <div className="flex items-center gap-2">
                      <div 
                        className="w-3 h-3 rounded-full" 
                        style={{ backgroundColor: colors[index % colors.length] }}
                      ></div>
                      <span className="font-semibold">{stock.symbol}</span>
                    </div>
                  </td>
                  <td className="text-right py-3 px-4 font-semibold">
                    ${stock.price.toFixed(2)}
                  </td>
                  <td className={`text-right py-3 px-4 font-semibold ${isPositive ? 'text-green-500' : 'text-red-500'}`}>
                    {isPositive ? '+' : ''}{stock.change.toFixed(2)}%
                  </td>
                  <td className="text-right py-3 px-4 text-gray-400">
                    ${min.toFixed(2)} - ${max.toFixed(2)}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
