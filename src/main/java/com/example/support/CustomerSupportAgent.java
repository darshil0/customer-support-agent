package com.example.support;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.tools.ToolContext;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Customer Support Multi-Agent System (Refactored)
 * * A production-ready implementation of an intelligent customer support system
 * using Google Agent Development Kit (ADK) for Java.
 * * Uses Configuration, ValidationUtils, and TransactionIdGenerator for robustness.
 * @author Darshil
 * @version 1.0.0
 */
public class CustomerSupportAgent {
    
    // High Priority Fix: Use java.util.logging.Logger (or slf4j/logback) for standard logging
    private static final Logger logger = Logger.getLogger(CustomerSupportAgent.class.getName());
    private static final String MODEL_NAME = "gemini-2.0-flash-exp";
    
    // Mock database for demonstration (uses ConcurrentHashMap for thread safety - Issue 5)
    private static final Map<String, Map<String, Object>> mockDatabase = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Object>> ticketDatabase = new ConcurrentHashMap<>();
    
    static {
        initializeMockData();
    }
    
    /**
     * Initialize mock customer data
     */
    private static void initializeMockData() {
        // Customer 1: Premium tier
        Map<String, Object> customer1 = new HashMap<>();
        customer1.put("customerId", "CUST001");
        customer1.put("name", "John Doe");
        customer1.put("email", "john.doe@example.com");
        customer1.put("accountBalance", 1250.00);
        customer1.put("tier", "Premium");
        customer1.put("accountStatus", "Active");
        customer1.put("lastPaymentDate", "2024-11-25"); // Old date for eligibility test
        mockDatabase.put("CUST001", customer1);
        
        // Customer 2: Basic tier
        Map<String, Object> customer2 = new HashMap<>();
        customer2.put("customerId", "CUST002");
        customer2.put("name", "Jane Smith");
        customer2.put("email", "jane.smith@example.com");
        customer2.put("accountBalance", 0.00);
        customer2.put("tier", "Basic");
        customer2.put("accountStatus", "Active");
        customer2.put("lastPaymentDate", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)); // Recent date
        mockDatabase.put("CUST002", customer2);
        
        // Customer 3: Enterprise tier
        Map<String, Object> customer3 = new HashMap<>();
        customer3.put("customerId", "CUST003");
        customer3.put("name", "Bob Wilson");
        customer3.put("email", "bob.wilson@example.com");
        customer3.put("accountBalance", 5000.00);
        customer3.put("tier", "Enterprise");
        customer3.put("accountStatus", "Active");
        customer3.put("lastPaymentDate", "2024-11-30");
        mockDatabase.put("CUST003", customer3);
        
        logger.info("Mock database initialized with " + mockDatabase.size() + " customers");
    }
    
    /**
     * Create the root orchestrator agent
     */
    public static BaseAgent createRootAgent() {
        return LlmAgent.builder()
            .name("customer-support-orchestrator")
            .description("Root orchestrator for customer support system")
            .model(MODEL_NAME)
            .instruction(
                "You are the root customer support orchestrator. Your role is to:\n" +
                "1. Understand the customer's query\n" +
                "2. Route to the appropriate specialist agent:\n" +
                "   - billing-agent: For payments, balances, invoices\n" +
                "   - technical-support-agent: For technical issues, bugs, login problems\n" +
                "   - account-agent: For account settings, profile updates\n" +
                "   - refund-processor-workflow: For refund requests\n" +
                "3. Provide a friendly, professional response\n" +
                "4. Always start by greeting the customer warmly\n\n" +
                "Before delegating, explain to the customer who you're connecting them with and why."
            )
            .subAgents(
                createBillingAgent(),
                createTechnicalSupportAgent(),
                createAccountAgent(),
                createRefundProcessorWorkflow()
            )
            .beforeModelCallback(CustomerSupportAgent::beforeModelCallback)
            .afterAgentCallback(CustomerSupportAgent::afterAgentCallback)
            .beforeToolCallback(CustomerSupportAgent::beforeToolCallback)
            .build();
    }
    
    // ... (Agent creation methods remain unchanged) ...

    private static BaseAgent createBillingAgent() {
        return LlmAgent.builder()
            .name("billing-agent")
            .description("Handles all billing, payment, and invoice queries")
            .model(MODEL_NAME)
            .instruction(
                "You are a billing specialist. Handle:\n" +
                // ... (instructions) ...
                "Provide payment confirmation"
            )
            .tools(
                "getCustomerAccount",
                "processPayment"
            )
            .build();
    }
    
    private static BaseAgent createTechnicalSupportAgent() {
        return LlmAgent.builder()
            .name("technical-support-agent")
            .description("Handles technical issues and troubleshooting")
            .model(MODEL_NAME)
            .instruction(
                "You are a technical support specialist. Handle:\n" +
                // ... (instructions) ...
                "Escalate complex issues to engineering"
            )
            .tools(
                "getCustomerAccount",
                "createTicket",
                "getTickets"
            )
            .build();
    }
    
    private static BaseAgent createAccountAgent() {
        return LlmAgent.builder()
            .name("account-agent")
            .description("Manages account settings and profile updates")
            .model(MODEL_NAME)
            .instruction(
                "You are an account management specialist. Handle:\n" +
                // ... (instructions) ...
                "Send confirmation of updates"
            )
            .tools(
                "getCustomerAccount",
                "updateAccountSettings"
            )
            .build();
    }
    
    private static BaseAgent createRefundProcessorWorkflow() {
        return SequentialAgent.builder()
            .name("refund-processor-workflow")
            .description("Sequential workflow for processing refund requests")
            .subAgents(
                createRefundValidatorAgent(),
                createRefundProcessorAgent()
            )
            .build();
    }
    
    private static BaseAgent createRefundValidatorAgent() {
        return LlmAgent.builder()
            .name("refund-validator")
            .description("Validates refund eligibility")
            .model(MODEL_NAME)
            .instruction(
                "You validate refund requests. Check:\n" +
                // ... (instructions) ...
                "If not eligible, explain why and suggest alternatives."
            )
            .tools("validateRefundEligibility")
            .outputKey("validation_result")
            .build();
    }
    
    private static BaseAgent createRefundProcessorAgent() {
        return LlmAgent.builder()
            .name("refund-processor")
            .description("Processes approved refunds")
            .model(MODEL_NAME)
            .instruction(
                "You process approved refunds. Actions:\n" +
                // ... (instructions) ...
                "Always explain refund processing times (5-7 business days)."
            )
            .tools("processRefund")
            .build();
    }

    // ==================== TOOL METHODS ====================
    
    /**
     * Get customer account information
     * (Refactored to use ValidationUtils and structured error handling - Issues 4 & 7)
     */
    @Schema(description = "Retrieve detailed customer account information")
    public static Map<String, Object> getCustomerAccount(
        @Schema(description = "Customer ID (format: CUST###)") String customerId,
        ToolContext toolContext
    ) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("[TOOL] getCustomerAccount called for: " + customerId);
            
            // Refactored: Use ValidationUtils
            customerId = ValidationUtils.validateCustomerId(customerId);
            
            // Check cache first
            String cacheKey = "customer:" + customerId;
            Object cached = toolContext.getState().get(cacheKey);
            if (cached != null) {
                logger.info("[CACHE-HIT] Retrieved from cache: " + customerId);
                return (Map<String, Object>) cached;
            }
            
            // Lookup in database
            Map<String, Object> customer = mockDatabase.get(customerId);
            if (customer == null) {
                // High Priority Fix: Throw exception or return structured error map
                result.put("success", false);
                result.put("error", "Customer not found: " + customerId);
                return result;
            }
            
            // Store in cache
            result.put("success", true);
            result.put("customer", customer);
            toolContext.getState().put(cacheKey, result);
            toolContext.getState().put("current_customer", customerId);
            
            logger.info("[SUCCESS] Customer retrieved: " + customerId);
            return result;
            
        } catch (IllegalArgumentException e) {
            // High Priority Fix: Catch specific validation errors
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        } catch (Exception e) {
            // High Priority Fix: Catch general system errors
            logger.severe("[ERROR] getCustomerAccount failed: " + e.getMessage());
            result.put("success", false);
            result.put("error", "System error: Failed to retrieve account data.");
            return result;
        }
    }
    
    /**
     * Process a payment
     * (Refactored to use ValidationUtils and TransactionIdGenerator - Issues 4, 7, & 8)
     */
    @Schema(description = "Process a customer payment")
    public static Map<String, Object> processPayment(
        @Schema(description = "Customer ID") String customerId,
        @Schema(description = "Payment amount (USD)") Object amountObj,
        ToolContext toolContext
    ) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("[TOOL] processPayment called for: " + customerId);
            
            // Refactored: Use ValidationUtils
            customerId = ValidationUtils.validateCustomerId(customerId);
            double amount = ValidationUtils.validateAmount(amountObj);
            
            // Get customer (and check existence)
            Map<String, Object> customer = mockDatabase.get(customerId);
            if (customer == null) {
                result.put("success", false);
                result.put("error", "Customer not found for ID: " + customerId);
                return result;
            }
            
            // Refactored: Use TransactionIdGenerator
            String transactionId = TransactionIdGenerator.generateTransactionId("TXN");
            
            // Update balance
            double currentBalance = ((Number) customer.get("accountBalance")).doubleValue();
            double newBalance = currentBalance + amount;
            // Use BigDecimal for real financial systems, but Double for mock
            customer.put("accountBalance", Math.round(newBalance * 100.0) / 100.0);
            customer.put("lastPaymentDate", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
            
            // Store transaction
            toolContext.getState().put("last_transaction_id", transactionId);
            toolContext.getState().put("last_payment_amount", amount);
            
            result.put("success", true);
            result.put("transactionId", transactionId);
            result.put("amount", amount);
            result.put("previousBalance", currentBalance);
            result.put("newBalance", newBalance);
            result.put("message", "Payment processed successfully");
            
            logger.info("[SUCCESS] Payment processed: " + transactionId);
            return result;
            
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        } catch (Exception e) {
            logger.severe("[ERROR] processPayment failed: " + e.getMessage());
            result.put("success", false);
            result.put("error", "System error: Failed to process payment.");
            return result;
        }
    }
    
    /**
     * Create a support ticket
     * (Refactored to use ValidationUtils and TransactionIdGenerator - Issues 4, 7, & 8)
     */
    @Schema(description = "Create a new support ticket")
    public static Map<String, Object> createTicket(
        @Schema(description = "Customer ID") String customerId,
        @Schema(description = "Ticket subject") String subject,
        @Schema(description = "Ticket description") String description,
        @Schema(description = "Priority (LOW, MEDIUM, HIGH, defaults to MEDIUM)") String priority,
        ToolContext toolContext
    ) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("[TOOL] createTicket called for: " + customerId);
            
            // Refactored: Use ValidationUtils
            customerId = ValidationUtils.validateCustomerId(customerId);
            
            if (subject == null || subject.trim().isEmpty()) {
                result.put("success", false);
                result.put("error", "Subject is required for ticket creation");
                return result;
            }
            
            if (description == null || description.trim().isEmpty()) {
                result.put("success", false);
                result.put("error", "Description is required for ticket creation");
                return result;
            }
            
            // Refactored: Use ValidationUtils for priority
            priority = ValidationUtils.validatePriority(priority);
            
            // Refactored: Use TransactionIdGenerator
            String ticketId = TransactionIdGenerator.generateTicketId();
            
            // Create ticket
            Map<String, Object> ticket = new HashMap<>();
            ticket.put("ticketId", ticketId);
            ticket.put("customerId", customerId);
            ticket.put("subject", subject.trim());
            ticket.put("description", description.trim());
            ticket.put("priority", priority);
            ticket.put("status", "OPEN");
            ticket.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            ticket.put("assignedTo", "Engineering Team");
            
            // Store ticket
            ticketDatabase.put(ticketId, ticket);
            
            result.put("success", true);
            result.put("ticket", ticket);
            result.put("message", "Ticket created successfully. Engineering will investigate within 2-4 hours.");
            
            logger.info("[SUCCESS] Ticket created: " + ticketId);
            return result;
            
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        } catch (Exception e) {
            logger.severe("[ERROR] createTicket failed: " + e.getMessage());
            result.put("success", false);
            result.put("error", "System error: Failed to create ticket.");
            return result;
        }
    }
    
    /**
     * Get tickets for a customer
     * (Refactored to use ValidationUtils - Issues 4 & 7)
     */
    @Schema(description = "Retrieve support tickets for a customer")
    public static Map<String, Object> getTickets(
        @Schema(description = "Customer ID") String customerId,
        @Schema(description = "Filter by status (OPEN, IN_PROGRESS, RESOLVED, CLOSED)") String status,
        ToolContext toolContext
    ) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("[TOOL] getTickets called for: " + customerId);
            
            // Refactored: Use ValidationUtils
            customerId = ValidationUtils.validateCustomerId(customerId);
            
            // Filter tickets
            List<Map<String, Object>> customerTickets = new ArrayList<>();
            // Normalize status filter if present
            String statusFilter = (status != null && !status.trim().isEmpty()) ? status.toUpperCase() : null;

            for (Map<String, Object> ticket : ticketDatabase.values()) {
                if (customerId.equals(ticket.get("customerId"))) {
                    if (statusFilter == null || statusFilter.equalsIgnoreCase((String) ticket.get("status"))) {
                        customerTickets.add(ticket);
                    }
                }
            }
            
            result.put("success", true);
            result.put("tickets", customerTickets);
            result.put("count", customerTickets.size());
            
            logger.info("[SUCCESS] Retrieved " + customerTickets.size() + " tickets");
            return result;
            
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        } catch (Exception e) {
            logger.severe("[ERROR] getTickets failed: " + e.getMessage());
            result.put("success", false);
            result.put("error", "System error: Failed to retrieve tickets.");
            return result;
        }
    }
    
    /**
     * Update account settings
     * (Refactored to use ValidationUtils - Issues 4 & 7)
     */
    @Schema(description = "Update customer account settings")
    public static Map<String, Object> updateAccountSettings(
        @Schema(description = "Customer ID") String customerId,
        @Schema(description = "New email address") String email,
        @Schema(description = "New tier (Basic, Premium, Enterprise)") String tier,
        ToolContext toolContext
    ) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("[TOOL] updateAccountSettings called for: " + customerId);
            
            // Refactored: Use ValidationUtils
            customerId = ValidationUtils.validateCustomerId(customerId);
            
            // Get customer (and check existence)
            Map<String, Object> customer = mockDatabase.get(customerId);
            if (customer == null) {
                result.put("success", false);
                result.put("error", "Customer not found for ID: " + customerId);
                return result;
            }
            
            Map<String, String> updates = new HashMap<>();
            
            // Update email if provided
            if (email != null && !email.trim().isEmpty()) {
                if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    result.put("success", false);
                    result.put("error", "Invalid email format");
                    return result;
                }
                customer.put("email", email.trim());
                updates.put("email", email.trim());
            }
            
            // Update tier if provided
            if (tier != null && !tier.trim().isEmpty()) {
                String[] validTiers = {"BASIC", "PREMIUM", "ENTERPRISE"};
                String normalizedTier = tier.toUpperCase();
                boolean validTier = false;
                for (String t : validTiers) {
                    if (t.equals(normalizedTier)) {
                        validTier = true;
                        break;
                    }
                }
                
                if (!validTier) {
                    result.put("success", false);
                    result.put("error", "Invalid tier. Must be one of: Basic, Premium, or Enterprise");
                    return result;
                }
                
                // Capitalize properly for display
                String properTier = normalizedTier.charAt(0) + normalizedTier.substring(1).toLowerCase();
                customer.put("tier", properTier);
                updates.put("tier", properTier);
            }
            
            if (updates.isEmpty()) {
                result.put("success", false);
                result.put("error", "No valid updates provided (email or tier)");
                return result;
            }
            
            result.put("success", true);
            result.put("updates", updates);
            result.put("message", "Account settings updated successfully");
            
            logger.info("[SUCCESS] Account updated: " + customerId);
            return result;
            
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        } catch (Exception e) {
            logger.severe("[ERROR] updateAccountSettings failed: " + e.getMessage());
            result.put("success", false);
            result.put("error", "System error: Failed to update settings.");
            return result;
        }
    }
    
    /**
     * Validate refund eligibility
     * (Refactored to use ValidationUtils - Issues 4 & 7)
     */
    @Schema(description = "Validate if a customer is eligible for a refund")
    public static Map<String, Object> validateRefundEligibility(
        @Schema(description = "Customer ID") String customerId,
        ToolContext toolContext
    ) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("[TOOL] validateRefundEligibility called for: " + customerId);
            
            // Refactored: Use ValidationUtils
            customerId = ValidationUtils.validateCustomerId(customerId);
            
            // Get customer (and check existence)
            Map<String, Object> customer = mockDatabase.get(customerId);
            if (customer == null) {
                result.put("success", false);
                result.put("error", "Customer not found for ID: " + customerId);
                return result;
            }
            
            // Check eligibility criteria
            List<String> reasons = new ArrayList<>();
            boolean eligible = true;
            
            // Check account status
            if (!"Active".equals(customer.get("accountStatus"))) {
                eligible = false;
                reasons.add("Account is not active");
            }
            
            // Check payment date (within 30 days)
            String lastPaymentDateStr = (String) customer.get("lastPaymentDate");
            if (lastPaymentDateStr != null) {
                // Parse date in ISO format (YYYY-MM-DD)
                LocalDateTime lastPayment = LocalDateTime.parse(lastPaymentDateStr + "T00:00:00"); 
                LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
                
                // If payment is older than 30 days ago, it's ineligible
                if (lastPayment.isBefore(thirtyDaysAgo)) {
                    eligible = false;
                    reasons.add("Last payment was more than 30 days ago (Date: " + lastPaymentDateStr + ")");
                }
            }
            
            // Set eligibility state in context for the next step (processRefund)
            result.put("success", true);
            result.put("eligible", eligible);
            result.put("tier", customer.get("tier"));
            result.put("reasons", reasons);
            
            toolContext.getState().put("refund_eligible", eligible);
            toolContext.getState().put("refund_customer", customerId);
            
            String status = eligible ? "eligible" : "not eligible";
            result.put("message", "Customer is " + status + " for refund.");
            
            logger.info("[SUCCESS] Validation complete for: " + customerId + " - Eligible: " + eligible);
            return result;
            
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        } catch (Exception e) {
            logger.severe("[ERROR] validateRefundEligibility failed: " + e.getMessage());
            result.put("success", false);
            result.put("error", "System error: Failed to validate refund eligibility.");
            return result;
        }
    }
    
    /**
     * Process a refund
     * (Refactored to use ValidationUtils and TransactionIdGenerator - Issues 4, 7, & 8)
     */
    @Schema(description = "Process a refund for an eligible customer")
    public static Map<String, Object> processRefund(
        @Schema(description = "Customer ID") String customerId,
        @Schema(description = "Refund amount") Object amountObj,
        ToolContext toolContext
    ) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("[TOOL] processRefund called for: " + customerId);
            
            // Check if validation passed (High Priority Fix: Dependency Check)
            Boolean eligible = (Boolean) toolContext.getState().get("refund_eligible");
            if (eligible == null || !eligible) {
                result.put("success", false);
                result.put("error", "Refund validation must be completed successfully first.");
                return result;
            }
            
            // Refactored: Use ValidationUtils
            customerId = ValidationUtils.validateCustomerId(customerId);
            double amount = ValidationUtils.validateAmount(amountObj);
            
            // Get customer (and check existence)
            Map<String, Object> customer = mockDatabase.get(customerId);
            if (customer == null) {
                result.put("success", false);
                result.put("error", "Customer not found for ID: " + customerId);
                return result;
            }
            
            // Check if amount doesn't exceed balance
            double currentBalance = ((Number) customer.get("accountBalance")).doubleValue();
            if (amount > currentBalance) {
                result.put("success", false);
                result.put("error", "Refund amount $" + amount + " exceeds current account balance of $" + currentBalance);
                return result;
            }
            
            // Refactored: Use TransactionIdGenerator
            String refundId = TransactionIdGenerator.generateTransactionId("REF");
            
            // Process refund (deduct from balance)
            double newBalance = currentBalance - amount;
            customer.put("accountBalance", Math.round(newBalance * 100.0) / 100.0);
            
            // Store refund info
            toolContext.getState().put("last_refund_id", refundId);
            toolContext.getState().put("refund_amount", amount);
            
            result.put("success", true);
            result.put("refundId", refundId);
            result.put("amount", amount);
            result.put("previousBalance", currentBalance);
            result.put("newBalance", newBalance);
            result.put("message", "Refund processed successfully. Funds will be returned within 5-7 business days.");
            result.put("estimatedDate", LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ISO_DATE));
            
            logger.info("[SUCCESS] Refund processed: " + refundId);
            return result;
            
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        } catch (Exception e) {
            logger.severe("[ERROR] processRefund failed: " + e.getMessage());
            result.put("success", false);
            result.put("error", "System error: Failed to process refund.");
            return result;
        }
    }
    
    // ==================== CALLBACK METHODS (Issue 6 Fix: Improved Safety Filter) ====================
    
    /**
     * Before model callback - Implements content safety filter (High Priority Fix)
     */
    private static String beforeModelCallback(String input, String agentName, String invocationId) {
        logger.info("[CALLBACK] beforeModel - Agent: " + agentName + " | Invocation: " + invocationId);
        
        // Enhanced sensitive keyword detection with regex patterns
        String[] sensitivePatterns = {
            "password", "passwd", "pwd",
            "credit.?card", "cvv", "cvc",
            "ssn", "social.?security",
            "api.?key", "secret", "token",
            "\\b\\d{16}\\b", // Credit card numbers
            "\\b\\d{3}-\\d{2}-\\d{4}\\b", // SSN format
        };
        
        String lowerInput = input.toLowerCase();
        for (String pattern : sensitivePatterns) {
            if (lowerInput.matches(".*" + pattern + ".*")) {
                logger.warning("[SECURITY] Blocked sensitive content - Pattern: " + pattern + 
                             " - Agent: " + agentName);
                throw new SecurityException(
                    "Your request contains sensitive information that cannot be processed. " +
                    "Please remove passwords, credit card numbers, or personal identifiers."
                );
            }
        }
        
        logger.info("[SECURITY] PASSED - Agent: " + agentName);
        return input;
    }
    
    /**
     * After agent callback - logging
     */
    private static void afterAgentCallback(String response, String agentName, 
                                          String invocationId, String sessionId) {
        logger.info("[CALLBACK] afterAgent - Agent: " + agentName + 
                   " | Invocation: " + invocationId + 
                   " | Session: " + sessionId);
        // Note: In production, we'd integrate this with the StateManager to update session timestamp (Issue 5)
    }
    
    /**
     * Before tool callback - logging and caching observation
     */
    private static void beforeToolCallback(String toolName, Map<String, Object> parameters, 
                                          ToolContext toolContext) {
        logger.info("[CALLBACK] beforeTool - Tool: " + toolName + " | Parameters: " + parameters);
        
        // Simple logging for cache check before execution
        if ("getCustomerAccount".equals(toolName)) {
            String customerId = (String) parameters.get("customerId");
            if (customerId != null) {
                String cacheKey = "customer:" + customerId.toUpperCase();
                Object cached = toolContext.getState().get(cacheKey);
                if (cached != null) {
                    logger.info("[CACHE-CHECK] Customer data is already cached.");
                }
            }
        }
    }
}
