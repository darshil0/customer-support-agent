import { generateDailyReport, fetchSectorWeights } from './geminiService';

declare const jest: any;
declare const describe: any;
declare const beforeEach: any;
declare const it: any;
declare const expect: any;

// Mock the Google GenAI SDK
const mockGenerateContent = jest.fn();
jest.mock('@google/genai', () => {
  return {
    GoogleGenAI: jest.fn().mockImplementation(() => ({
      models: {
        generateContent: mockGenerateContent,
      },
    })),
  };
});

describe('geminiService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('generateDailyReport', () => {
    it('returns structured report data on success', async () => {
      // Setup Mock
      mockGenerateContent.mockResolvedValueOnce({
        text: '# Market Report\nThings are up.',
        candidates: [
          {
            groundingMetadata: {
              groundingChunks: [{ web: { uri: 'http://test.com', title: 'Test News' } }],
            },
          },
        ],
      });

      const result = await generateDailyReport();

      expect(result.content).toBe('# Market Report\nThings are up.');
      expect(result.sources).toHaveLength(1);
      expect(result.sources[0].web?.uri).toBe('http://test.com');
      expect(result.timestamp).toBeDefined();
    });

    it('handles missing source data gracefully', async () => {
      mockGenerateContent.mockResolvedValueOnce({
        text: 'Report text',
        candidates: [], // No grounding metadata
      });

      const result = await generateDailyReport();
      expect(result.content).toBe('Report text');
      expect(result.sources).toEqual([]);
    });

    it('throws error when API fails', async () => {
      mockGenerateContent.mockRejectedValueOnce(new Error('API Failure'));
      await expect(generateDailyReport()).rejects.toThrow('API Failure');
    });
  });

  describe('fetchSectorWeights', () => {
    it('parses valid JSON response correctly', async () => {
      mockGenerateContent.mockResolvedValueOnce({
        text: 'Here is the data: [{"name": "Tech", "value": 30}]',
        candidates: [],
      });

      const result = await fetchSectorWeights();
      expect(result.data).toHaveLength(1);
      expect(result.data[0]).toEqual({ name: 'Tech', value: 30 });
    });

    it('returns empty array if no JSON found in text', async () => {
      mockGenerateContent.mockResolvedValueOnce({
        text: 'I cannot provide that information.',
        candidates: [],
      });

      const result = await fetchSectorWeights();
      expect(result.data).toEqual([]);
    });

    it('returns empty array on JSON parse error', async () => {
      mockGenerateContent.mockResolvedValueOnce({
        text: '[{"name": "Broken JSON"',
        candidates: [],
      });

      // The service catches the JSON parse error internally and returns empty
      const result = await fetchSectorWeights();
      expect(result.data).toEqual([]);
    });
  });
});