package com.example.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Customer Support Agent System.
 * <p>
 * This application demonstrates a production-ready multi-agent architecture
 * built with Google ADK for Java.
 * </p>
 *
 * @author Darshil
 * @version 1.0.1
 */
@SpringBootApplication
public class App {

  private static final Logger logger = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    try {
      validateEnvironment();
      SpringApplication.run(App.class, args);
      logger.info("✅ Customer Support Agent System started successfully.");
    } catch (IllegalStateException e) {
      logger.error("Configuration Error: {}", e.getMessage());
      System.exit(1);
    } catch (Exception e) {
      logger.error("Fatal Error: {}", e.getMessage(), e);
      System.exit(2);
    }
  }

  /**
   * Validate required environment variables and configuration before startup.
   */
  private static void validateEnvironment() {
    String apiKey = System.getenv("GOOGLE_API_KEY");

    if (apiKey == null || apiKey.trim().isEmpty()) {
      throw new IllegalStateException(
          "Missing environment variable: GOOGLE_API_KEY.\n"
              + "Please set it using one of these methods:\n\n"
              + "Linux/Mac:\n"
              + "  export GOOGLE_API_KEY=your-api-key\n\n"
              + "Windows (Command Prompt):\n"
              + "  set GOOGLE_API_KEY=your-api-key\n\n"
              + "Windows (PowerShell):\n"
              + "  $env:GOOGLE_API_KEY=\"your-api-key\"\n\n"
              + "Or add it in your IDE run configuration.");
    }

    logger.info("✓ Environment configuration validated");
  }
}
