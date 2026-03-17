import React from 'react';

export const LoadingState: React.FC = () => {
  return (
    <div 
      data-testid="loading-state"
      className="flex flex-col items-center justify-center py-20"
      role="status"
      aria-live="polite"
      aria-label="Loading market analysis"
    >
      <div className="relative">
        {/* Outer spinning circle */}
        <div className="w-24 h-24 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin"></div>
        
        {/* Inner pulsing circle */}
        <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2">
          <div className="w-12 h-12 bg-blue-600 rounded-full animate-pulse"></div>
        </div>
      </div>

      <div className="mt-8 text-center">
        <h3 className="text-2xl font-semibold mb-2">Analyzing Market Data</h3>
        <p className="text-gray-400 mb-4">
          Fetching real-time information and generating insights...
        </p>
        
        {/* Loading steps animation */}
        <div className="flex items-center justify-center gap-2 mt-6">
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" style={{ animationDelay: '0ms' }}></div>
            <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" style={{ animationDelay: '150ms' }}></div>
            <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" style={{ animationDelay: '300ms' }}></div>
          </div>
        </div>
      </div>

      {/* Loading skeleton preview */}
      <div className="mt-12 w-full max-w-4xl space-y-4">
        <div className="h-4 bg-gray-800 rounded animate-pulse"></div>
        <div className="h-4 bg-gray-800 rounded animate-pulse w-5/6"></div>
        <div className="h-4 bg-gray-800 rounded animate-pulse w-4/6"></div>
      </div>
    </div>
  );
};
