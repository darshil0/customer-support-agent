import React, { Component, ErrorInfo, ReactNode } from 'react';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Uncaught error:', error, errorInfo);
  }

  public render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-[200px] flex items-center justify-center p-6 bg-slate-900/50 rounded-xl border border-slate-700">
          <div className="max-w-md text-center">
            <i className="fas fa-exclamation-triangle text-3xl text-amber-500 mb-3" aria-hidden="true"></i>
            <h3 className="text-lg font-semibold text-white mb-2">Content Unavailable</h3>
            <p className="text-sm text-slate-400 mb-4">
              We couldn't load this section. This might be due to a temporary connection issue.
            </p>
            <button
              onClick={() => this.setState({ hasError: false })}
              className="px-4 py-2 bg-slate-700 hover:bg-slate-600 border border-slate-600 text-white rounded-lg transition-colors text-xs font-medium uppercase tracking-wider"
            >
              Retry
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}