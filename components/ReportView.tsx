import React, { useState } from 'react';
import { jsPDF } from 'jspdf';
import { MarketReport } from '../types';

interface ReportViewProps {
  report: MarketReport;
}

export const ReportView: React.FC<ReportViewProps> = ({ report }) => {
  const [showSources, setShowSources] = useState(false);
  
  // Helper to parse inline styles (currently just bold)
  const parseInline = (text: string) => {
    // Split by bold syntax **text**
    const parts = text.split(/(\*\*[^*]+\*\*)/g);
    return parts.map((part, index) => {
      if (part.startsWith('**') && part.endsWith('**')) {
        return (
          <span key={index} className="font-bold text-slate-100">
            {part.slice(2, -2)}
          </span>
        );
      }
      return part;
    });
  };

  const renderContent = (text: string) => {
    const lines = text.split('\n');
    const elements: React.ReactNode[] = [];
    let currentListItems: React.ReactNode[] = [];

    const flushList = () => {
      if (currentListItems.length > 0) {
        elements.push(
          <ul key={`list-${elements.length}`} className="list-disc pl-6 mb-4 space-y-2 text-slate-300 marker:text-emerald-500">
            {currentListItems}
          </ul>
        );
        currentListItems = [];
      }
    };

    lines.forEach((line, i) => {
      const trimmedLine = line.trim();

      // Skip completely empty lines, but flush lists
      if (!trimmedLine) {
        flushList();
        return;
      }

      // Headings
      if (trimmedLine.startsWith('## ')) {
        flushList();
        elements.push(
          <h2 key={`h2-${i}`} className="text-2xl font-bold text-emerald-400 mt-8 mb-4 border-b border-slate-700 pb-2">
            {parseInline(trimmedLine.replace(/^##\s+/, ''))}
          </h2>
        );
        return;
      }
      if (trimmedLine.startsWith('### ')) {
        flushList();
        elements.push(
          <h3 key={`h3-${i}`} className="text-xl font-semibold text-sky-400 mt-6 mb-3">
            {parseInline(trimmedLine.replace(/^###\s+/, ''))}
          </h3>
        );
        return;
      }
       if (trimmedLine.startsWith('# ')) {
        flushList();
        elements.push(
          <h1 key={`h1-${i}`} className="text-3xl font-bold text-white mt-8 mb-6">
            {parseInline(trimmedLine.replace(/^#\s+/, ''))}
          </h1>
        );
        return;
      }

      // List Items (Bullets)
      if (trimmedLine.startsWith('- ') || trimmedLine.startsWith('* ')) {
        const content = trimmedLine.replace(/^[-*]\s+/, '');
        currentListItems.push(
          <li key={`li-${i}`} className="pl-1">
            {parseInline(content)}
          </li>
        );
        return;
      }

      // Regular Paragraphs (flushes previous list if any)
      flushList();
      elements.push(
        <p key={`p-${i}`} className="text-slate-300 leading-relaxed mb-3">
          {parseInline(trimmedLine)}
        </p>
      );
    });

    flushList(); // Final flush in case the text ends with a list
    return elements;
  };

  const handleExportPDF = () => {
    const doc = new jsPDF();
    const pageWidth = doc.internal.pageSize.getWidth();
    const margin = 20;
    const maxLineWidth = pageWidth - margin * 2;
    
    let y = 20;

    // Title
    doc.setFont("helvetica", "bold");
    doc.setFontSize(22);
    doc.setTextColor(22, 163, 74); // Emerald color like web
    doc.text("Daily S&P 500 Report", margin, y);
    y += 10;

    // Timestamp
    doc.setFont("helvetica", "normal");
    doc.setFontSize(10);
    doc.setTextColor(100, 100, 100);
    doc.text(`Generated: ${new Date(report.timestamp).toLocaleString()}`, margin, y);
    y += 15;

    // Divider
    doc.setDrawColor(200, 200, 200);
    doc.line(margin, y - 5, pageWidth - margin, y - 5);

    // Content Processing
    const lines = report.content.split('\n');
    
    lines.forEach((line) => {
        if (y > 280) { // New page
            doc.addPage();
            y = 20;
        }

        const cleanLine = line.replace(/\*\*/g, '').trim();
        if (!cleanLine) {
            y += 5;
            return;
        }

        if (line.startsWith('# ')) {
             doc.setFont("helvetica", "bold");
             doc.setFontSize(18);
             doc.setTextColor(0, 0, 0);
             const text = cleanLine.replace(/^#\s+/, '');
             doc.text(text, margin, y);
             y += 10;
        } else if (line.startsWith('## ')) {
             doc.setFont("helvetica", "bold");
             doc.setFontSize(14);
             doc.setTextColor(40, 40, 40);
             const text = cleanLine.replace(/^##\s+/, '');
             doc.text(text, margin, y);
             y += 8;
        } else if (line.startsWith('### ')) {
             doc.setFont("helvetica", "bold");
             doc.setFontSize(12);
             doc.setTextColor(60, 60, 60);
             const text = cleanLine.replace(/^###\s+/, '');
             doc.text(text, margin, y);
             y += 7;
        } else if (line.startsWith('- ') || line.startsWith('* ')) {
             doc.setFont("helvetica", "normal");
             doc.setFontSize(11);
             doc.setTextColor(20, 20, 20);
             const text = cleanLine.replace(/^[-*]\s+/, '');
             // bullet point handling
             doc.text('•', margin + 2, y);
             const splitText = doc.splitTextToSize(text, maxLineWidth - 10);
             doc.text(splitText, margin + 8, y);
             y += (splitText.length * 5) + 2;
        } else {
             doc.setFont("helvetica", "normal");
             doc.setFontSize(11);
             doc.setTextColor(20, 20, 20);
             const splitText = doc.splitTextToSize(cleanLine, maxLineWidth);
             doc.text(splitText, margin, y);
             y += (splitText.length * 5) + 2;
        }
    });

    // Sources footer
    if (report.sources.length > 0) {
         if (y > 260) { doc.addPage(); y = 20; }
         y += 10;
         doc.line(margin, y, pageWidth - margin, y);
         y += 10;
         
         doc.setFont("helvetica", "italic");
         doc.setFontSize(9);
         doc.setTextColor(100, 100, 100);
         doc.text("Sources:", margin, y);
         y += 5;
         
         report.sources.forEach(src => {
             if (src.web?.title) {
                if (y > 280) { doc.addPage(); y = 20; }
                const sourceText = `- ${src.web.title} (${src.web.uri})`;
                const splitSource = doc.splitTextToSize(sourceText, maxLineWidth);
                doc.text(splitSource, margin + 5, y);
                y += (splitSource.length * 4) + 2;
             }
         });
    }

    // Footer Branding
    
    doc.save(`FinAgent_Report_${new Date().toISOString().split('T')[0]}.pdf`);
  };

  return (
    <div className="space-y-6 animate-fade-in" data-testid="report-content-view">
      <div className="bg-slate-800 rounded-xl p-8 shadow-xl border border-slate-700">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between mb-6 border-b border-slate-700 pb-4 gap-4">
          <h2 className="text-2xl font-bold text-white flex items-center gap-3">
            <i className="fas fa-chart-line text-emerald-500" aria-hidden="true"></i>
            Daily S&P 500 Report
          </h2>
          <div className="flex items-center gap-3 self-end sm:self-auto">
            <span className="hidden lg:inline text-sm text-slate-400" data-testid="report-timestamp">
              Generated: {new Date(report.timestamp).toLocaleTimeString()}
            </span>
            <button
              onClick={handleExportPDF}
              className="flex items-center gap-2 bg-slate-700 hover:bg-slate-600 hover:border-slate-500 text-white text-xs font-medium px-4 py-2 rounded-lg transition-all border border-slate-600 shadow-sm"
              title="Export to PDF"
              aria-label="Export Report to PDF"
            >
              <i className="fas fa-file-pdf text-red-400 text-sm"></i>
              <span>Export PDF</span>
            </button>
          </div>
        </div>
        
        <div className="prose prose-invert max-w-none" data-testid="report-markdown-body">
          {renderContent(report.content)}
        </div>
      </div>

      {report.sources.length > 0 && (
        <div className="flex flex-col gap-4">
           {/* Toggle Button */}
           <button
             onClick={() => setShowSources(!showSources)}
             className="flex items-center justify-center gap-2 w-full py-3 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-xl text-slate-300 transition-all font-medium text-sm shadow-sm group"
             aria-expanded={showSources}
             aria-controls="sources-panel"
           >
             <i className={`fas fa-chevron-${showSources ? 'up' : 'down'} text-emerald-500 transition-transform duration-200`} />
             {showSources ? 'Hide Sources' : 'View Sources'}
             <span className="bg-slate-700 text-xs px-2 py-0.5 rounded-full text-slate-400 group-hover:bg-slate-600">
               {report.sources.length}
             </span>
           </button>

           {/* Collapsible Area */}
           {showSources && (
             <div 
               id="sources-panel"
               className="bg-slate-800 rounded-xl p-6 shadow-lg border border-slate-700 animate-fade-in" 
               data-testid="grounding-sources"
             >
               <h3 className="text-lg font-semibold text-slate-200 mb-4 flex items-center gap-2">
                 <i className="fas fa-link text-blue-400" aria-hidden="true"></i>
                 Sources & Grounding
               </h3>
               <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                 {report.sources.map((source, idx) => (
                   <a 
                     key={idx}
                     href={source.web?.uri}
                     target="_blank"
                     rel="noopener noreferrer"
                     className="flex items-center p-3 rounded-lg bg-slate-900/50 hover:bg-slate-700 transition-colors border border-slate-700 hover:border-blue-500 group"
                     aria-label={`Open source: ${source.web?.title || 'Unknown Source'}`}
                   >
                     <div className="w-8 h-8 rounded-full bg-slate-800 flex items-center justify-center mr-3 group-hover:bg-blue-500/20 transition-colors">
                       <i className="fas fa-globe text-slate-400 group-hover:text-blue-400 text-sm" aria-hidden="true"></i>
                     </div>
                     <div className="flex-1 min-w-0">
                       <p className="text-sm font-medium text-slate-200 truncate group-hover:text-blue-300">
                         {source.web?.title || "Unknown Source"}
                       </p>
                       <p className="text-xs text-slate-500 truncate">
                         {source.web?.uri}
                       </p>
                     </div>
                     <i className="fas fa-external-link-alt text-xs text-slate-600 group-hover:text-slate-400 ml-2" aria-hidden="true"></i>
                   </a>
                 ))}
               </div>
             </div>
           )}
        </div>
      )}
    </div>
  );
};