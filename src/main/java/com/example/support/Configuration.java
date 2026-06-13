package com.example.support;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Configuration class for managing API keys and application settings. */
@Component
public class Configuration {

  @Value("${google.api.key:${GOOGLE_API_KEY:}}")
  private String googleApiKey;

  @Value("${skip.api.key.validation:false}")
  private boolean skipValidation;

  public Configuration() {}

  /**
   * Validates that required configuration is present.
   *
   * @throws IllegalStateException if required configuration is missing
   */
  @PostConstruct
  private void validateConfiguration() {
    if (skipValidation) {
      return;
    }
    if (googleApiKey == null || googleApiKey.trim().isEmpty()) {
      throw new IllegalStateException(
          "GOOGLE_API_KEY environment variable is not set. "
              + "Please set it before running the application.\n"
              + "Example: export GOOGLE_API_KEY=\"your-api-key-here\"");
    }

    if (googleApiKey.length() < 20) {
      throw new IllegalStateException(
          "GOOGLE_API_KEY appears to be invalid (too short). " + "Please check your API key.");
    }

    System.out.println("✓ Configuration validated successfully");
    System.out.println("✓ Google API Key: " + maskApiKey(googleApiKey));
  }

  /**
   * Gets the Google API key.
   *
   * @return the API key
   */
  public String getGoogleApiKey() {
    return googleApiKey;
  }

  /**
   * Masks the API key for logging purposes.
   *
   * @param apiKey the API key to mask
   * @return masked API key
   */
  private String maskApiKey(String apiKey) {
    if (apiKey == null || apiKey.length() < 8) {
      return "***";
    }
    return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
  }
}
