import React from 'react';
import { Copy, ExternalLink } from 'lucide-react';

interface GroundingSource {
  web?: {
    title?: string;
    uri?: string;
  };
  title?: string;
  uri?: string;
}

interface ReportViewProps {
  report: string;
  sources?: GroundingSource[];
}

export const ReportView: React.FC<ReportViewProps> = ({
  report,
  sources = [],
}) => {
  const [copied, setCopied] = React.useState(false);

  const handleCopyReport = () => {
    const reportText = `${report}\n\n${
      sources.length > 0
        ? 'Sources:\n' +
          sources
            .map((source) => {
              const sourceTitle = source.web?.title || source.title || 'Unknown';
              const sourceUri = source.web?.uri || source.uri || '#';
              return `- ${sourceTitle}: ${sourceUri}`;
            })
            .join('\n')
        : ''
    }`;

    navigator.clipboard.writeText(reportText).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    });
  };

  // Safely extract source title and URI with nested property support
  const extractSourceInfo = (
    source: GroundingSource
  ): { title: string; uri: string } => {
    const title =
      source.web?.title ||
      source.title ||
      'Reference';
    const uri =
      source.web?.uri ||
      source.uri ||
      '#';
    return { title, uri };
  };

  return (
    <div 
      data-testid="report-container"
      className="w-full max-w-4xl mx-auto space-y-6"
    >
      {/* Copy Button */}
      <button
        onClick={handleCopyReport}
        className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors"
      >
        <Copy size={16} />
        {copied ? 'Copied!' : 'Copy Report'}
      </button>

      {/* Report Content */}
      <div className="bg-gray-800/50 backdrop-blur-sm border border-gray-700 rounded-lg p-6 prose prose-invert max-w-none">
        <div className="text-gray-300 whitespace-pre-wrap leading-relaxed">
          {report}
        </div>
      </div>

      {/* Grounding Sources */}
      {sources && sources.length > 0 && (
        <div className="bg-gray-800/50 backdrop-blur-sm border border-gray-700 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-white mb-4">Sources</h3>
          <div className="space-y-3">
            {sources.map((source, index) => {
              const { title: sourceTitle, uri: sourceUri } =
                extractSourceInfo(source);
              return (
                <div
                  key={index}
                  className="flex items-start gap-3 p-3 bg-gray-900/50 rounded-lg hover:bg-gray-900 transition-colors"
                >
                  <ExternalLink
                    size={16}
                    className="text-blue-400 flex-shrink-0 mt-0.5"
                  />
                  <div className="flex-1 min-w-0">
                    <a
                      href={sourceUri}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-blue-400 hover:text-blue-300 font-medium block truncate"
                    >
                      {sourceTitle}
                    </a>
                    <p className="text-xs text-gray-500 truncate">
                      {sourceUri}
                    </p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};
