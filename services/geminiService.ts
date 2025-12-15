import { GoogleGenAI } from '@google/genai';
import { MarketReport, GroundingChunk, SectorData, StockData } from '../types';

const ai = new GoogleGenAI({ apiKey: process.env.API_KEY });

/**
 * Maps technical API errors to user-friendly messages.
 */
const getFriendlyErrorMessage = (error: unknown): string => {
  const msg = error instanceof Error ? error.message : String(error);
  const lowerMsg = msg.toLowerCase();

  if (
    lowerMsg.includes('401') ||
    lowerMsg.includes('api key') ||
    lowerMsg.includes('unauthorized')
  ) {
    return 'Invalid API Key. Please check your configuration.';
  }
  if (
    lowerMsg.includes('429') ||
    lowerMsg.includes('quota') ||
    lowerMsg.includes('resource exhausted')
  ) {
    return 'API rate limit exceeded. Please try again later.';
  }
  if (
    lowerMsg.includes('503') ||
    lowerMsg.includes('overloaded') ||
    lowerMsg.includes('service unavailable')
  ) {
    return 'Service is currently overloaded. Please try again in a moment.';
  }
  if (
    lowerMsg.includes('fetch failed') ||
    lowerMsg.includes('network') ||
    lowerMsg.includes('connection')
  ) {
    return 'Network connection failed. Please check your internet connection.';
  }
  if (lowerMsg.includes('safety') || lowerMsg.includes('blocked')) {
    return 'The report generation was blocked due to safety filters.';
  }

  return 'An unexpected error occurred. Please try again.';
};

export const generateDailyReport = async (): Promise<MarketReport> => {
  const currentDate = new Date().toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });

  const prompt = `
    Generate a comprehensive, professional daily financial report for the S&P 500 for today, ${currentDate}.
    
    Structure the report clearly with the following sections using Markdown formatting (#, ##, -):
    1. **Market Snapshot**: A brief summary of how the S&P 500 performed today (Up/Down, points).
    2. **Key Drivers**: What economic events, earnings reports, or news drove the market sentiment?
    3. **Top Movers**: List notable gainers and losers in the index.
    4. **Sector Performance**: Briefly discuss which sectors (e.g., Tech, Energy, Healthcare) led or lagged.
    5. **Outlook**: A sentence on what to watch for tomorrow.

    Use a professional, objective tone suitable for a financial analyst.
  `;

  try {
    const response = await ai.models.generateContent({
      model: 'gemini-2.5-flash',
      contents: prompt,
      config: {
        tools: [{ googleSearch: {} }],
        systemInstruction:
          'You are FinAgent Pro, a senior financial analyst providing accurate, data-backed daily stock market reports.',
      },
    });

    const text = response.text || 'No report generated.';

    // Extract grounding chunks (sources)
    const chunks = response.candidates?.[0]?.groundingMetadata?.groundingChunks || [];

    // Filter to keep only valid web sources and map to local type
    const validSources: GroundingChunk[] = chunks
      .filter((chunk: any) => chunk.web?.uri && chunk.web?.title)
      .map((chunk: any) => ({
        web: {
          uri: chunk.web.uri,
          title: chunk.web.title,
        },
      }));

    return {
      content: text,
      sources: validSources,
      timestamp: new Date().toISOString(),
    };
  } catch (error) {
    console.error('Gemini API Error (Report):', error);
    throw new Error(getFriendlyErrorMessage(error));
  }
};

export const fetchSectorWeights = async (): Promise<{
  data: SectorData[];
  sources: GroundingChunk[];
}> => {
  const prompt = `
    Find the latest available S&P 500 sector weightings (percentage allocation).
    
    Return the result strictly as a JSON array containing objects with:
    - "name": The sector name (e.g., Information Technology)
    - "value": The percentage weight as a number (e.g., 29.5)
    
    Do not include any Markdown formatting, backticks, or 'json' labels. Return ONLY the raw JSON string.
  `;

  try {
    const response = await ai.models.generateContent({
      model: 'gemini-2.5-flash',
      contents: prompt,
      config: {
        tools: [{ googleSearch: {} }],
      },
    });

    // Extract grounding chunks (sources)
    const chunks = response.candidates?.[0]?.groundingMetadata?.groundingChunks || [];
    const validSources: GroundingChunk[] = chunks
      .filter((chunk: any) => chunk.web?.uri && chunk.web?.title)
      .map((chunk: any) => ({
        web: {
          uri: chunk.web.uri,
          title: chunk.web.title,
        },
      }));

    let text = response.text || '[]';

    // Robust JSON extraction: look for the first '[' and the last ']'
    const jsonMatch = text.match(/\[[\s\S]*\]/);
    if (!jsonMatch) {
      console.warn('No JSON array found in response text');
      return { data: [], sources: [] };
    }

    const jsonString = jsonMatch[0];
    const data = JSON.parse(jsonString);

    let parsedData: SectorData[] = [];
    if (Array.isArray(data) && data.length > 0) {
      parsedData = data.map((item: any) => ({
        name: item.name || 'Unknown',
        value: Number(item.value) || 0,
      }));
    }
    return { data: parsedData, sources: validSources };
  } catch (error) {
    // Log the detailed error for debugging, but return empty to allow graceful UI degradation
    console.error('Failed to fetch sector weights:', error);
    return { data: [], sources: [] };
  }
};

export const fetchStockPerformance = async (tickers: string[]): Promise<StockData[]> => {
  if (tickers.length === 0) return [];
  
  const prompt = `
    Get the latest real-time stock price and percentage change for these tickers: ${tickers.join(', ')}.
    
    Return ONLY a raw JSON array (no markdown code blocks). Each object must have:
    - "symbol": The ticker symbol (e.g. AAPL)
    - "price": Current price (e.g. "230.50")
    - "change": Price change (e.g. "+1.50" or "-0.20")
    - "percentChange": Percentage change (e.g. "+0.65%")
    
    Ensure data is from the latest available trading session. If a ticker is invalid, omit it from the array.
  `;
  
  try {
    const response = await ai.models.generateContent({
      model: 'gemini-2.5-flash',
      contents: prompt,
      config: {
        tools: [{ googleSearch: {} }],
      },
    });

    const text = response.text || '[]';
    
    // Robust JSON extraction
    const jsonMatch = text.match(/\[[\s\S]*\]/);
    if (!jsonMatch) {
      return [];
    }
    
    const parsed = JSON.parse(jsonMatch[0]);
    
    if (Array.isArray(parsed)) {
       return parsed.map((item: any) => ({
         symbol: String(item.symbol || '').toUpperCase(),
         price: String(item.price || '0.00'),
         change: String(item.change || '0.00'),
         percentChange: String(item.percentChange || '0.00%')
       }));
    }
    return [];

  } catch (error) {
    console.error('Failed to fetch stock data:', error);
    return [];
  }
};
