import '@testing-library/jest-dom';
import { vi } from 'vitest';

// Mock environment variables for testing
vi.stubGlobal('import.meta.env', {
  VITE_API_KEY: 'test-api-key',
});

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

  const mockGoogleGenerativeAI = vi.fn(() => ({
    getGenerativeModel: mockGetGenerativeModel,
  }));

  return {
    GoogleGenerativeAI: mockGoogleGenerativeAI,
  };
});

// Mock recharts
vi.mock('recharts', async () => {
  const OriginalModule = await vi.importActual('recharts');
  return {
    ...OriginalModule,
    ResponsiveContainer: ({ children }) => children,
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
