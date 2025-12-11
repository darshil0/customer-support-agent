package com.example.support;

import com.google.adk.AdkRunner;
import com.google.adk.agents.BaseAgent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Customer Support Agent System
 * 
 * This application demonstrates a production-ready multi-agent system
 * built with Google ADK for Java.
 * 
 * @author Darshil
 * @version 1.0.0
 */
@SpringBootApplication
public class App {

    public static void main(String[] args) {
        try {
            // Validate environment configuration
            validateEnvironment();
            
            // Start Spring Boot application
            SpringApplication.run(App.class, args);
            
            // Create and configure the root agent
            BaseAgent rootAgent = CustomerSupportAgent.createRootAgent();
            
            // Start the ADK runner with the agent
            System.out.println("=".repeat(60));
            System.out.println("  Customer Support Multi-Agent System");
            System.out.println("  Version 1.0.0");
            System.out.println("=".repeat(60));
            System.out.println();
            System.out.println("Starting agent system...");
            System.out.println("Web UI available at: http://localhost:8000");
            System.out.println();
            System.out.println("Commands:");
            System.out.println("  - Type 'help' for usage examples");
            System.out.println("  - Type 'state' to view session state");
            System.out.println("  - Type 'clear' to reset session");
            System.out.println("  - Type 'exit' or 'quit' to stop");
            System.out.println();
            
            // Start the agent runner
            AdkRunner.run(rootAgent);
            
        } catch (IllegalStateException e) {
            System.err.println("Configuration Error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Fatal Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Validate required environment variables and configuration
     */
    private static void validateEnvironment() {
        String apiKey = System.getenv("GOOGLE_API_KEY");
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException(
                "GOOGLE_API_KEY environment variable is not set.\n" +
                "Please set it using one of these methods:\n\n" +
                "Linux/Mac:\n" +
                "  export GOOGLE_API_KEY=your-api-key-here\n\n" +
                "Windows (Command Prompt):\n" +
                "  set GOOGLE_API_KEY=your-api-key-here\n\n" +
                "Windows (PowerShell):\n" +
                "  $env:GOOGLE_API_KEY=\"your-api-key-here\"\n\n" +
                "Or add to your IDE run configuration."
            );
        }
        
        System.out.println("✓ Environment configuration validated");
    }
}
