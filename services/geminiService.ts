import { GoogleGenerativeAI } from '@google/generative-ai';

const apiKey = import.meta.env.VITE_API_KEY;

if (!apiKey) {
  throw new Error('VITE_API_KEY is not defined. Please check your .env file');
}

const genAI = new GoogleGenerativeAI(apiKey);

export interface MarketReport {
  text: string;
  groundingSources: any[];
}

export interface SectorData {
  name: string;
  value: number;
  fill: string;
}

export interface StockData {
  symbol: string;
  price: number;
  change: number;
  sparkline: number[];
}

export async function generateMarketReport(retries = 3): Promise<MarketReport> {
  let lastError: Error | null = null;

  for (let i = 0; i < retries; i++) {
    try {
      const model = genAI.getGenerativeModel({ 
        model: 'gemini-2.0-flash-exp',
      });

      const currentDate = new Date().toLocaleDateString('en-US', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });

      const prompt = `Generate a comprehensive S&P 500 market analysis report for ${currentDate}.

Include the following sections:

1. **Market Snapshot**: Current S&P 500 level, daily change, and overall market sentiment
2. **Key Drivers**: Major factors influencing today's market movement
3. **Top Movers**: Notable gainers and losers across different sectors
4. **Sector Performance**: Analysis of which sectors are leading or lagging
5. **Economic Indicators**: Relevant economic data released today
6. **Market Outlook**: Brief perspective on what to watch for in the coming days

Please use real-time data and cite your sources. Format the response in markdown.`;

      const result = await model.generateContent(prompt);
      
      if (!result.response) {
        throw new Error('No response received from API');
      }

      const text = result.response.text();
      const groundingSources = result.response.candidates?.[0]?.groundingMetadata?.groundingChunks || [];

      return {
        text,
        groundingSources
      };

    } catch (error: any) {
      lastError = error;
      
      console.error(`Attempt ${i + 1} failed:`, error);

      // Handle specific error types
      if (error.message?.includes('API_KEY_INVALID') || error.message?.includes('API key')) {
        throw new Error('Invalid API key. Please check your VITE_API_KEY in the .env file');
      }
      
      if (error.message?.includes('RATE_LIMIT') || error.message?.includes('quota')) {
        if (i < retries - 1) {
          const waitTime = (i + 1) * 2000;
          console.log(`Rate limited. Waiting ${waitTime}ms before retry...`);
          await new Promise(resolve => setTimeout(resolve, waitTime));
          continue;
        }
        throw new Error('Rate limit exceeded. Please try again in a few moments.');
      }

      if (error.message?.includes('PERMISSION_DENIED')) {
        throw new Error('API access denied. Please verify your API key permissions.');
      }

      // Retry for transient errors
      if (i < retries - 1) {
        const waitTime = 1000 * (i + 1);
        console.log(`Retrying in ${waitTime}ms...`);
        await new Promise(resolve => setTimeout(resolve, waitTime));
        continue;
      }
    }
  }

  throw new Error(`Failed to generate report after ${retries} attempts: ${lastError?.message || 'Unknown error'}`);
}

export function getMockSectorData(): SectorData[] {
  return [
    { name: 'Technology', value: 28.5, fill: '#3b82f6' },
    { name: 'Healthcare', value: 13.2, fill: '#10b981' },
    { name: 'Financials', value: 11.8, fill: '#f59e0b' },
    { name: 'Consumer Discretionary', value: 10.9, fill: '#8b5cf6' },
    { name: 'Communication Services', value: 8.7, fill: '#ec4899' },
    { name: 'Industrials', value: 8.3, fill: '#06b6d4' },
    { name: 'Consumer Staples', value: 6.4, fill: '#84cc16' },
    { name: 'Energy', value: 4.2, fill: '#f97316' },
    { name: 'Utilities', value: 2.8, fill: '#6366f1' },
    { name: 'Real Estate', value: 2.5, fill: '#14b8a6' },
    { name: 'Materials', value: 2.7, fill: '#a855f7' }
  ];
}

export function getMockStockData(): StockData[] {
  const symbols = [
    'AAPL', 'MSFT', 'GOOGL', 'AMZN', 'NVDA', 'META', 'TSLA', 'BRK.B',
    'UNH', 'JNJ', 'V', 'WMT', 'XOM', 'JPM', 'PG', 'MA', 'HD', 'CVX',
    'MRK', 'ABBV', 'KO', 'PEP', 'COST', 'AVGO', 'LLY', 'TMO', 'CSCO',
    'ACN', 'MCD', 'ABT', 'DHR', 'NEE', 'CRM', 'VZ', 'ADBE', 'TXN',
    'NKE', 'DIS', 'CMCSA', 'WFC', 'PM', 'INTC', 'BMY', 'UPS', 'QCOM'
  ];

  return symbols.map(symbol => {
    const basePrice = Math.random() * 500 + 50;
    const change = (Math.random() - 0.5) * 10;
    const sparkline = Array.from({ length: 5 }, () => 
      basePrice + (Math.random() - 0.5) * 20
    );

    return {
      symbol,
      price: basePrice,
      change,
      sparkline
    };
  });
}
