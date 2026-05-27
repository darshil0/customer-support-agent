export interface GroundingChunk {
  web?: {
    uri: string;
    title: string;
  };
}

// Aligned with geminiService.ts MarketReport shape and useHistory usage
export interface MarketReport {
  text: string;
  groundingSources: GroundingChunk[];
  timestamp: string;
}

export enum ReportStatus {
  IDLE = 'IDLE',
  LOADING = 'LOADING',
  SUCCESS = 'SUCCESS',
  ERROR = 'ERROR',
}

export interface SectorData {
  name: string;
  value: number;
  [key: string]: any;
}

// Aligned with geminiService.ts StockData — price and change are numbers, not strings
export interface StockData {
  symbol: string;
  price: number;
  change: number;
  sparkline: number[];
}
