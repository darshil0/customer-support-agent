import { useState, useEffect, useCallback } from 'react';
import { MarketReport } from '../types';

const STORAGE_KEY = 'finagent_history';
const MAX_HISTORY_ITEMS = 10;

export const useHistory = () => {
  const [history, setHistory] = useState<MarketReport[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (Array.isArray(parsed)) {
          return parsed;
        }
      }
    } catch (e) {
      console.warn('Failed to parse history from local storage:', e);
      // If corrupted, clear it to prevent persistent errors
      localStorage.removeItem(STORAGE_KEY);
    }
    return [];
  });

  const addToHistory = useCallback((report: MarketReport) => {
    setHistory((prev) => {
      // Prevent duplicates based on timestamp
      const exists = prev.some((item) => item.timestamp === report.timestamp);
      if (exists) return prev;

      const updated = [report, ...prev].slice(0, MAX_HISTORY_ITEMS);
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
      } catch (e) {
        console.error('Failed to save history to local storage:', e);
      }
      return updated;
    });
  }, []);

  const clearHistory = useCallback(() => {
    setHistory([]);
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch (e) {
      console.error('Failed to clear history from local storage:', e);
    }
  }, []);

  return { history, addToHistory, clearHistory };
};