import '@testing-library/jest-dom';
import { vi } from 'vitest';
import React from 'react';

// Mock environment variables for testing
vi.stubEnv('VITE_API_KEY', 'test-api-key');

// Mock the GoogleGenerativeAI module
vi.mock('@google/generative-ai', () => {
  // We need to mock the constructor and the methods it returns
  const mockGenerateContent = vi.fn(() => Promise.resolve({
    response: {
      text: () => 'This is a mock AI-generated market report.',
      candidates: [{
        groundingMetadata: {
          groundingChunks: [{ web: { uri: 'http://mocksource.com', title: 'Mock Source' } }]
        }
      }]
    }
  }));

  const mockGetGenerativeModel = vi.fn(() => ({
    generateContent: mockGenerateContent,
  }));

  return {
    GoogleGenerativeAI: vi.fn().mockImplementation(function (this: any) {
      this.getGenerativeModel = mockGetGenerativeModel;
      return this;
    }),
  };
});

// Mock recharts
vi.mock('recharts', async (importOriginal) => {
  const OriginalModule = await importOriginal<typeof import('recharts')>();
  return {
    ...OriginalModule,
    ResponsiveContainer: ({ children }: { children: React.ReactNode }) => children,
  };
});

// Mock window.matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(), // deprecated
    removeListener: vi.fn(), // deprecated
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
});
