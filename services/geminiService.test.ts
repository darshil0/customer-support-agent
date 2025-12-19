import { generateMarketReport } from './geminiService';
import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock the Google Generative AI SDK
const mockGenerateContent = vi.fn();
vi.mock('@google/generative-ai', () => {
  return {
    GoogleGenerativeAI: vi.fn().mockImplementation(() => ({
      getGenerativeModel: vi.fn().mockImplementation(() => ({
        generateContent: mockGenerateContent,
      })),
    })),
  };
});

// Mock environment variables
vi.mock('import.meta.env', () => ({
  VITE_API_KEY: 'test-api-key'
}));

describe('geminiService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('generateMarketReport', () => {
    it('returns structured report data on success', async () => {
      // Setup Mock
      mockGenerateContent.mockResolvedValueOnce({
        response: {
          text: () => '## Market Report\nThings are up.',
          candidates: [
            {
              groundingMetadata: {
                groundingChunks: [{ web: { uri: 'http://test.com', title: 'Test News' } }],
              },
            },
          ],
        }
      });

      const result = await generateMarketReport();

      expect(result.text).toBe('## Market Report\nThings are up.');
      expect(result.groundingSources).toHaveLength(1);
    });

    it('handles API errors', async () => {
      mockGenerateContent.mockRejectedValueOnce(new Error('API Error'));
      await expect(generateMarketReport()).rejects.toThrow('Failed to generate report after 3 attempts');
    });
  });
});