package com.example.support;

import com.google.adk.AdkRunner;
import com.google.adk.agents.BaseAgent;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Main application class for Customer Support Agent System
 * * This application demonstrates a production-ready multi-agent system
 * built with Google ADK for Java.
 * * @author Darshil
 * @version 1.0.0
 */
@SpringBootApplication
public class App {

    public static void main(String[] args) {
        try {
            // Validate environment configuration before starting Spring
            validateEnvironment();
            
            // Start Spring Boot application. This call is blocking until the application is stopped.
            SpringApplication.run(App.class, args);
            
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
     * Define the root agent as a Spring Bean.
     * This allows Spring to manage its lifecycle and dependencies.
     * * NOTE: Assuming CustomerSupportAgent class exists and has a static createRootAgent()
     * or is itself a Spring component (e.g., uses @Component). 
     * If it's a Spring Component, you should inject it instead of calling createRootAgent().
     */
    @Bean
    public BaseAgent rootAgent() {
        return CustomerSupportAgent.createRootAgent();
    }
    
    /**
     * Runs the ADK console runner after the Spring application has fully started.
     * This keeps the web server running on the main thread and runs the
     * interactive console runner on a separate thread/hook.
     */
    @Bean
    public CommandLineRunner runAdkConsole(BaseAgent rootAgent) {
        return args -> {
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
            
            // Start the agent runner. This call is still blocking, but since it's in a 
            // CommandLineRunner, it runs after SpringApp.run() and achieves the desired setup.
            AdkRunner.run(rootAgent);
        };
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
