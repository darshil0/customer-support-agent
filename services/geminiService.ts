import { GoogleGenAI } from "@google/genai";
import { MarketReport, GroundingChunk, SectorData } from "../types";

const ai = new GoogleGenAI({ apiKey: process.env.API_KEY });

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
      model: "gemini-2.5-flash",
      contents: prompt,
      config: {
        tools: [{ googleSearch: {} }],
        systemInstruction: "You are FinAgent Pro, a senior financial analyst providing accurate, data-backed daily stock market reports.",
      },
    });

    const text = response.text || "No report generated.";
    
    // Extract grounding chunks (sources)
    const chunks = response.candidates?.[0]?.groundingMetadata?.groundingChunks || [];
    
    // Filter to keep only valid web sources
    const validSources: GroundingChunk[] = chunks.filter(
      (chunk: any) => chunk.web?.uri && chunk.web?.title
    );

    return {
      content: text,
      sources: validSources,
      timestamp: new Date().toISOString(),
    };
  } catch (error) {
    console.error("Gemini API Error:", error);
    throw error;
  }
};

export const fetchSectorWeights = async (): Promise<{data: SectorData[], sources: GroundingChunk[]}> => {
  const prompt = `
    Find the latest available S&P 500 sector weightings (percentage allocation).
    
    Return the result strictly as a JSON array containing objects with:
    - "name": The sector name (e.g., Information Technology)
    - "value": The percentage weight as a number (e.g., 29.5)
    
    Do not include any Markdown formatting, backticks, or 'json' labels. Return ONLY the raw JSON string.
  `;

  try {
    const response = await ai.models.generateContent({
      model: "gemini-2.5-flash",
      contents: prompt,
      config: {
        tools: [{ googleSearch: {} }],
      },
    });

    // Extract grounding chunks (sources)
    const chunks = response.candidates?.[0]?.groundingMetadata?.groundingChunks || [];
    const validSources: GroundingChunk[] = chunks.filter(
      (chunk: any) => chunk.web?.uri && chunk.web?.title
    );

    let text = response.text || "[]";
    // Sanitize in case model adds markdown blocks
    text = text.replace(/```json/g, '').replace(/```/g, '').trim();

    const data = JSON.parse(text);
    
    let parsedData: SectorData[] = [];
    if (Array.isArray(data) && data.length > 0) {
        parsedData = data.map((item: any) => ({
            name: item.name || 'Unknown',
            value: Number(item.value) || 0
        }));
    }
    return { data: parsedData, sources: validSources };
  } catch (error) {
    console.error("Failed to fetch sector weights:", error);
    return { data: [], sources: [] };
  }
};
