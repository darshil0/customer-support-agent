import React from 'react';

interface ErrorStateProps {
  message: string;
  onRetry: () => void;
}

export const ErrorState: React.FC<ErrorStateProps> = ({ message, onRetry }) => {
  return (
    <div 
      data-testid="error-state"
      className="flex flex-col items-center justify-center py-20"
      role="alert"
      aria-live="assertive"
    >
      <div className="bg-red-900/20 border border-red-500 rounded-lg p-8 max-w-2xl w-full">
        <div className="flex items-start gap-4">
          <div className="flex-shrink-0">
            <svg 
              className="w-8 h-8 text-red-500" 
              fill="none" 
              viewBox="0 0 24 24" 
              stroke="currentColor"
              aria-hidden="true"
            >
              <path 
                strokeLinecap="round" 
                strokeLinejoin="round" 
                strokeWidth={2} 
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" 
              />
            </svg>
          </div>
          
          <div className="flex-1">
            <h3 className="text-xl font-semibold text-red-400 mb-2">
              Error Generating Report
            </h3>
            <p className="text-gray-300 mb-6">
              {message}
            </p>

            {/* Common troubleshooting tips */}
            <details className="mb-6">
              <summary className="cursor-pointer text-sm text-gray-400 hover:text-white mb-2">
                Troubleshooting Tips
              </summary>
              <ul className="text-sm text-gray-400 space-y-2 mt-2 ml-4 list-disc">
                <li>Check that your VITE_API_KEY is set correctly in the .env file</li>
                <li>Verify your API key at <a href="https://makersuite.google.com/app/apikey" target="_blank" rel="noopener noreferrer" className="text-blue-400 hover:underline">Google AI Studio</a></li>
                <li>If you're seeing rate limit errors, wait a minute and try again</li>
                <li>Make sure your internet connection is stable</li>
              </ul>
            </details>

            <div className="flex gap-4">
              <button
                onClick={onRetry}
                className="px-6 py-3 bg-red-600 hover:bg-red-700 rounded-lg transition-colors font-semibold"
                aria-label="Retry generating report"
              >
                Try Again
              </button>
              
              <button
                onClick={() => window.location.reload()}
                className="px-6 py-3 bg-gray-700 hover:bg-gray-600 rounded-lg transition-colors"
                aria-label="Reload page"
              >
                Reload Page
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
