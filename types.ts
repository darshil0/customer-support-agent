export interface GroundingChunk {
  web?: {
    uri: string;
    title: string;
  };
}

export interface MarketReport {
  content: string;
  sources: GroundingChunk[];
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

export interface StockData {
  symbol: string;
  price: string;
  change: string;
  percentChange: string;
  sparkline: number[];
}