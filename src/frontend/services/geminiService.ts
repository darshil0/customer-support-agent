import axios, { AxiosInstance } from 'axios';

interface GeminiRequestOptions {
  temperature?: number;
  topP?: number;
  topK?: number;
  maxOutputTokens?: number;
  groundingConfig?: {
    googleSearchRetrieval?: {
      disable?: boolean;
    };
  };
}

interface GeminiMessage {
  role: 'user' | 'model';
  parts: Array<{ text: string }>;
}

interface GenerateContentRequest {
  contents: GeminiMessage[];
  generationConfig?: {
    temperature?: number;
    topP?: number;
    topK?: number;
    maxOutputTokens?: number;
  };
  groundingConfig?: {
    googleSearchRetrieval?: {
      disable?: boolean;
    };
  };
}

interface GroundingChunk {
  web?: {
    title?: string;
    uri?: string;
  };
}

interface GenerateContentResponse {
  candidates: Array<{
    content: {
      parts: Array<{ text: string }>;
    };
    groundingMetadata?: {
      searchQueries?: string[];
      groundingChunks?: GroundingChunk[];
      webSearchQueries?: string[];
    };
  }>;
  usageMetadata: {
    promptTokenCount: number;
    candidatesTokenCount: number;
    totalTokenCount: number;
  };
}

class GeminiService {
  private client: AxiosInstance;
  private apiKey: string;
  private model = 'gemini-2.0-flash'; // Stable model instead of experimental
  private baseURL = 'https://generativelanguage.googleapis.com/v1beta/models';

  constructor(apiKey: string) {
    this.apiKey = apiKey;
    this.client = axios.create({
      baseURL: this.baseURL,
      timeout: 30000,
    });
  }

  /**
   * Generate content with optional search grounding
   * Precondition: apiKey is valid, prompt is non-empty string
   * @param prompt User prompt
   * @param options Optional configuration (temperature, grounding, etc.)
   * @returns Generated text with optional grounding sources
   */
  async generateContent(
    prompt: string,
    options: GeminiRequestOptions = {}
  ): Promise<{
    text: string;
    groundingSources: GroundingChunk[];
    usage: {
      promptTokens: number;
      outputTokens: number;
      totalTokens: number;
    };
  }> {
    // Validation
    if (!prompt || typeof prompt !== 'string' || prompt.trim().length === 0) {
      throw new Error('Prompt must be a non-empty string');
    }

    try {
      const requestBody: GenerateContentRequest = {
        contents: [
          {
            role: 'user',
            parts: [{ text: prompt }],
          },
        ],
        generationConfig: {
          temperature: options.temperature ?? 0.7,
          topP: options.topP ?? 0.95,
          topK: options.topK ?? 40,
          maxOutputTokens: options.maxOutputTokens ?? 2048,
        },
      };

      // Add grounding if requested
      if (options.groundingConfig) {
        requestBody.groundingConfig = options.groundingConfig;
      }

      const response = await this.client.post<GenerateContentResponse>(
        `${this.model}:generateContent?key=${this.apiKey}`,
        requestBody
      );

      // Validate response structure
      if (
        !response.data.candidates ||
        response.data.candidates.length === 0
      ) {
        throw new Error('No candidates in response');
      }

      const candidate = response.data.candidates[0];
      const text =
        candidate.content.parts[0]?.text || '';

      // Extract grounding sources (Gemini 2.0 format)
      const groundingSources =
        candidate.groundingMetadata?.groundingChunks || [];

      return {
        text,
        groundingSources,
        usage: {
          promptTokens: response.data.usageMetadata.promptTokenCount,
          outputTokens: response.data.usageMetadata.candidatesTokenCount,
          totalTokens: response.data.usageMetadata.totalTokenCount,
        },
      };
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const message = error.response?.data?.error?.message || error.message;
        throw new Error(`Gemini API Error: ${message}`);
      }
      throw error;
    }
  }

  /**
   * Generate content with search grounding enabled
   * Precondition: apiKey is valid, query is non-empty string
   * @param query Search query
   * @param options Optional configuration
   * @returns Generated response with web search grounding
   */
  async generateWithSearchGrounding(
    query: string,
    options: Omit<GeminiRequestOptions, 'groundingConfig'> = {}
  ): Promise<{
    text: string;
    groundingSources: Array<{ title?: string; uri?: string }>;
    usage: {
      promptTokens: number;
      outputTokens: number;
      totalTokens: number;
    };
  }> {
    const result = await this.generateContent(query, {
      ...options,
      groundingConfig: {
        googleSearchRetrieval: {
          disable: false,
        },
      },
    });

    // Transform grounding chunks to consistent format
    const sources = result.groundingSources.map((chunk) => ({
      title: chunk.web?.title,
      uri: chunk.web?.uri,
    }));

    return {
      text: result.text,
      groundingSources: sources,
      usage: result.usage,
    };
  }

  /**
   * Chat with multi-turn context
   * Precondition: messages array is non-empty, each message has role and content
   * @param messages Conversation history
   * @param options Optional configuration
   * @returns Model response
   */
  async chat(
    messages: Array<{
      role: 'user' | 'model';
      content: string;
    }>,
    options: GeminiRequestOptions = {}
  ): Promise<{
    text: string;
    groundingSources: GroundingChunk[];
    usage: {
      promptTokens: number;
      outputTokens: number;
      totalTokens: number;
    };
  }> {
    // Validation
    if (!Array.isArray(messages) || messages.length === 0) {
      throw new Error('Messages must be a non-empty array');
    }

    try {
      const contents: GeminiMessage[] = messages.map((msg) => ({
        role: msg.role,
        parts: [{ text: msg.content }],
      }));

      const requestBody: GenerateContentRequest = {
        contents,
        generationConfig: {
          temperature: options.temperature ?? 0.7,
          topP: options.topP ?? 0.95,
          topK: options.topK ?? 40,
          maxOutputTokens: options.maxOutputTokens ?? 2048,
        },
      };

      if (options.groundingConfig) {
        requestBody.groundingConfig = options.groundingConfig;
      }

      const response = await this.client.post<GenerateContentResponse>(
        `${this.model}:generateContent?key=${this.apiKey}`,
        requestBody
      );

      if (
        !response.data.candidates ||
        response.data.candidates.length === 0
      ) {
        throw new Error('No candidates in response');
      }

      const candidate = response.data.candidates[0];
      const text = candidate.content.parts[0]?.text || '';
      const groundingSources =
        candidate.groundingMetadata?.groundingChunks || [];

      return {
        text,
        groundingSources,
        usage: {
          promptTokens: response.data.usageMetadata.promptTokenCount,
          outputTokens: response.data.usageMetadata.candidatesTokenCount,
          totalTokens: response.data.usageMetadata.totalTokenCount,
        },
      };
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const message = error.response?.data?.error?.message || error.message;
        throw new Error(`Gemini API Error: ${message}`);
      }
      throw error;
    }
  }

  getModel(): string {
    return this.model;
  }
}

export default GeminiService;
