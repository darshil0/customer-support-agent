package com.example.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Customer Support Agent System.
 *
 * <p>This application demonstrates a production-ready multi-agent architecture built with Google
 * ADK for Java.
 *
 * @author Darshil
 * @version 1.0.1
 */
@SpringBootApplication
public class App {

  private static final Logger logger = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    try {
      EnvironmentConfig.validateConfiguration();
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
}
