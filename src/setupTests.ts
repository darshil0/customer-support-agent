import '@testing-library/jest-dom';
import { vi } from 'vitest';

// Mock environment variables
Object.defineProperty(import.meta, 'env', {
  value: {
    VITE_API_KEY: 'test-api-key-123',
  },
  writable: true,
});

// Mock Google GenAI
vi.mock('@google/generative-ai', () => ({
  GoogleGenerativeAI: vi.fn(() => ({
    getGenerativeModel: vi.fn(() => ({
      generateContent: vi.fn(() =>
        Promise.resolve({
          response: {
            text: () => '# Mock Market Report\n\nThis is a test report.',
            candidates: [
              {
                groundingMetadata: {
                  groundingChunks: [
                    {
                      uri: 'https://example.com/source1',
                      title: 'Test Source 1',
                    },
                  ],
                },
              },
            ],
          },
        })
      ),
    })),
  })),
}));

// Mock window.matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation((query) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
});
