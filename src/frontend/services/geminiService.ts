import { GoogleGenerativeAI } from '@google/generative-ai';

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

export interface GroundingSource {
  web?: {
    title?: string;
    uri?: string;
  };
  title?: string;
  uri?: string;
}

export interface MarketReport {
  text: string;
  groundingSources: GroundingSource[];
}

const API_KEY = import.meta.env.VITE_API_KEY;

if (!API_KEY) {
  console.error('VITE_API_KEY environment variable is not set');
}

const client = new GoogleGenerativeAI(API_KEY);

/**
 * Generate market report with web search grounding
 * Precondition: API key is valid
 * Postcondition: Returns markdown report with grounding sources
 * 
 * @returns Market report with AI analysis and web search sources
 */
export async function generateMarketReport(): Promise<MarketReport> {
  const maxRetries = 3;
  let lastError: Error | null = null;

  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      const model = client.getGenerativeModel({
        model: 'gemini-2.0-flash',
        generationConfig: {
          temperature: 0.7,
          topP: 0.95,
          topK: 40,
          maxOutputTokens: 2048,
        },
      });

      const prompt = `Analyze the current S&P 500 market status and provide:
1. Market Snapshot - Current trends and movements
2. Key Drivers - News and factors affecting the market
3. Top Movers - Stocks with significant movements
4. Outlook - What to expect next

Format the response as markdown with clear sections. Be concise and factual.`;

      const result = await model.generateContent({
        contents: [
          {
            role: 'user',
            parts: [{ text: prompt }],
          },
        ],
      });

      const text = result.response.text();

      // Extract grounding sources from response
      const groundingSources: GroundingSource[] = [];
      
      // Check for grounding metadata in candidates
      if (result.response.candidates && result.response.candidates.length > 0) {
        const candidate = result.response.candidates[0];
        
        // Handle Gemini 2.0 grounding format with nested web properties
        if (candidate.groundingMetadata?.groundingChunks) {
          candidate.groundingMetadata.groundingChunks.forEach(chunk => {
            if (chunk.web?.uri && chunk.web?.title) {
              groundingSources.push({
                web: {
                  title: chunk.web.title,
                  uri: chunk.web.uri,
                },
              });
            }
          });
        }
      }

      return {
        text,
        groundingSources,
      };
    } catch (error) {
      lastError = error instanceof Error ? error : new Error(String(error));
      
      if (attempt < maxRetries) {
        // Exponential backoff: 1s, 2s, 4s
        await new Promise(resolve => 
          setTimeout(resolve, Math.pow(2, attempt - 1) * 1000)
        );
      }
    }
  }

  throw new Error(
    `Failed to generate report after ${maxRetries} attempts: ${lastError?.message || 'Unknown error'}`
  );
}

/**
 * Get mock sector allocation data
 * @returns Array of sector data for pie chart
 */
export function getMockSectorData(): SectorData[] {
  return [
    { name: 'Technology', value: 28.5, fill: '#3b82f6' },
    { name: 'Healthcare', value: 13.2, fill: '#10b981' },
    { name: 'Financials', value: 12.8, fill: '#f59e0b' },
    { name: 'Consumer', value: 10.5, fill: '#8b5cf6' },
    { name: 'Industrials', value: 8.3, fill: '#ec4899' },
    { name: 'Energy', value: 4.2, fill: '#ef4444' },
    { name: 'Materials', value: 2.5, fill: '#06b6d4' },
  ];
}

/**
 * Get mock stock data for watchlist
 * @returns Array of stock data
 */
export function getMockStockData(): StockData[] {
  return [
    {
      symbol: 'AAPL',
      price: 182.45,
      change: 2.15,
      sparkline: [178, 179, 180.5, 181.8, 182.45],
    },
    {
      symbol: 'MSFT',
      price: 405.23,
      change: 1.83,
      sparkline: [398, 401, 403.5, 404.1, 405.23],
    },
    {
      symbol: 'NVDA',
      price: 876.50,
      change: 3.42,
      sparkline: [842, 855, 865, 870, 876.50],
    },
    {
      symbol: 'TSLA',
      price: 248.90,
      change: -1.25,
      sparkline: [258, 255, 252, 250.5, 248.90],
    },
    {
      symbol: 'GOOGL',
      price: 140.35,
      change: 0.95,
      sparkline: [138, 139, 139.5, 140, 140.35],
    },
    {
      symbol: 'AMZN',
      price: 193.80,
      change: 1.42,
      sparkline: [190, 191, 192.5, 193.2, 193.80],
    },
  ];
}
