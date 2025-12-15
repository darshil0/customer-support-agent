import React from 'react';
import ReactMarkdown from 'react-markdown';

interface ReportViewProps {
  report: string;
  sources: any[];
}

export const ReportView: React.FC<ReportViewProps> = ({ report, sources }) => {
  return (
    <div data-testid="report-container" className="space-y-6">
      {/* Report Content */}
      <div className="bg-gray-800/50 backdrop-blur-sm border border-gray-700 rounded-lg p-6">
        <div 
          data-testid="report-markdown-body"
          className="prose prose-invert prose-lg max-w-none"
        >
          <ReactMarkdown
            components={{
              h1: ({ children }) => (
                <h1 className="text-3xl font-bold mb-4 text-white border-b border-gray-700 pb-2">
                  {children}
                </h1>
              ),
              h2: ({ children }) => (
                <h2 className="text-2xl font-bold mt-8 mb-4 text-white">
                  {children}
                </h2>
              ),
              h3: ({ children }) => (
                <h3 className="text-xl font-semibold mt-6 mb-3 text-white">
                  {children}
                </h3>
              ),
              p: ({ children }) => (
                <p className="text-gray-300 mb-4 leading-relaxed">
                  {children}
                </p>
              ),
              ul: ({ children }) => (
                <ul className="list-disc list-inside space-y-2 mb-4 text-gray-300">
                  {children}
                </ul>
              ),
              ol: ({ children }) => (
                <ol className="list-decimal list-inside space-y-2 mb-4 text-gray-300">
                  {children}
                </ol>
              ),
              li: ({ children }) => (
                <li className="ml-4">{children}</li>
              ),
              strong: ({ children }) => (
                <strong className="font-semibold text-white">{children}</strong>
              ),
              em: ({ children }) => (
                <em className="italic text-blue-300">{children}</em>
              ),
              code: ({ children }) => (
                <code className="bg-gray-900 px-2 py-1 rounded text-blue-300 text-sm">
                  {children}
                </code>
              ),
              blockquote: ({ children }) => (
                <blockquote className="border-l-4 border-blue-500 pl-4 italic text-gray-400 my-4">
                  {children}
                </blockquote>
              ),
            }}
          >
            {report}
          </ReactMarkdown>
        </div>
      </div>

      {/* Grounding Sources */}
      {sources && sources.length > 0 && (
        <div 
          data-testid="grounding-sources"
          className="bg-gray-800/50 backdrop-blur-sm border border-gray-700 rounded-lg p-6"
        >
          <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
            <svg 
              className="w-5 h-5 text-blue-400" 
              fill="none" 
              viewBox="0 0 24 24" 
              stroke="currentColor"
            >
              <path 
                strokeLinecap="round" 
                strokeLinejoin="round" 
                strokeWidth={2} 
                d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" 
              />
            </svg>
            Sources & References
          </h3>
          <div className="space-y-3">
            {sources.map((source, index) => (
              <a
                key={index}
                href={source.uri || source.url || '#'}
                target="_blank"
                rel="noopener noreferrer"
                className="block p-3 bg-gray-900/50 hover:bg-gray-900 rounded-lg transition-colors border border-gray-700 hover:border-blue-500"
              >
                <div className="flex items-start gap-3">
                  <svg 
                    className="w-5 h-5 text-blue-400 flex-shrink-0 mt-1" 
                    fill="none" 
                    viewBox="0 0 24 24" 
                    stroke="currentColor"
                  >
                    <path 
                      strokeLinecap="round" 
                      strokeLinejoin="round" 
                      strokeWidth={2} 
                      d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" 
                    />
                  </svg>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-white truncate">
                      {source.title || `Source ${index + 1}`}
                    </p>
                    <p className="text-xs text-gray-400 truncate">
                      {source.uri || source.url || 'No URL available'}
                    </p>
                  </div>
                </div>
              </a>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
