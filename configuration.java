package com.example.support;

/**
 * Handles application configuration, particularly API key management and validation.
 */
public class Configuration {

    private static final String GOOGLE_API_KEY = System.getenv("GOOGLE_API_KEY");

    public static void validateConfiguration() {
        if (GOOGLE_API_KEY == null || GOOGLE_API_KEY.isBlank()) {
            throw new IllegalStateException(
                "GOOGLE_API_KEY environment variable is not set. "
                + "Please set it using: export GOOGLE_API_KEY=your-key-here"
            );
        }
    }

    public static String getApiKey() {
        return GOOGLE_API_KEY;
    }
}
