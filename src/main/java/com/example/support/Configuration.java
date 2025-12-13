package com.example.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles application configuration, particularly API key management and validation
 * for Google ADK and related services.
 */
public final class EnvironmentConfig {

  private static final Logger logger = LoggerFactory.getLogger(EnvironmentConfig.class);

  private EnvironmentConfig() {
    // Prevent instantiation
  }

  /** Validate required environment variables. */
  public static void validateConfiguration() {
    String apiKey = System.getenv("GOOGLE_API_KEY");

    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalStateException(
          "GOOGLE_API_KEY environment variable is not set.\n"
              + "Please set it using one of these methods:\n\n"
              + "Linux/Mac:\n"
              + "  export GOOGLE_API_KEY=your-api-key\n\n"
              + "Windows (Command Prompt):\n"
              + "  set GOOGLE_API_KEY=your-api-key\n\n"
              + "Windows (PowerShell):\n"
              + "  $env:GOOGLE_API_KEY=\"your-api-key\"\n\n"
              + "Or add it to your IDE's run configuration.");
    }

    logger.info("✓ GOOGLE_API_KEY successfully validated");
  }

  /** Get the current Google API key from the environment. */
  public static String getApiKey() {
    return System.getenv("GOOGLE_API_KEY");
  }
}
