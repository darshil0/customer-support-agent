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
 * Customer Support Multi-Agent System
 * 
 * A production-ready implementation of an intelligent customer support system
 * using Google Agent Development Kit (ADK) for Java.
 * 
 * @author Darshil
 * @version 1.0.0
 */
public class CustomerSupportAgent {
    
    private static final Logger logger = Logger.getLogger(CustomerSupportAgent.class.getName());
    private static final String MODEL_NAME = "gemini-2.0-flash-exp";
    
    // Mock database for demonstration
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
        customer1.put("lastPaymentDate", "2024-11-25");
        mockDatabase.put("CUST001", customer1);
        
        // Customer 2: Basic tier
        Map<String, Object> customer2 = new HashMap<>();
        customer2.put("customerId", "CUST002");
        customer2.put("name", "Jane Smith");
        customer2.put("email", "jane.smith@example.com");
        customer2.put("accountBalance", 0.00);
        customer2.put("tier", "Basic");
        customer2.put("accountStatus", "Active");
        customer2.put("lastPaymentDate", "2024-12-01");
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
    
    /**
     * Create the billing specialist agent
     */
    private static BaseAgent createBillingAgent() {
        return LlmAgent.builder()
            .name("billing-agent")
            .description("Handles all billing, payment, and invoice queries")
            .model(MODEL_NAME)
            .instruction(
                "You are a billing specialist. Handle:\n" +
                "- Account balance inquiries\n" +
                "- Payment processing\n" +
                "- Invoice generation\n" +
                "- Payment history\n" +
                "- Outstanding balances\n\n" +
                "Always:\n" +
                "- Confirm customer identity first\n" +
                "- Be clear about amounts and transaction IDs\n" +
                "- Explain fees or charges\n" +
                "- Provide payment confirmation"
            )
            .tools(
                "getCustomerAccount",
                "processPayment",
                "getTickets"
            )
            .build();
    }
    
    /**
     * Create the technical support agent
     */
    private static BaseAgent createTechnicalSupportAgent() {
        return LlmAgent.builder()
            .name("technical-support-agent")
            .description("Handles technical issues and troubleshooting")
            .model(MODEL_NAME)
            .instruction(
                "You are a technical support specialist. Handle:\n" +
                "- Login issues\n" +
                "- Application errors\n" +
                "- Feature questions\n" +
                "- Bug reports\n" +
                "- System troubleshooting\n\n" +
                "Always:\n" +
                "- Gather detailed information about the issue\n" +
                "- Ask about error messages\n" +
                "- Create support tickets for tracking\n" +
                "- Provide clear troubleshooting steps\n" +
                "- Escalate complex issues to engineering"
            )
            .tools(
                "getCustomerAccount",
                "createTicket",
                "getTickets"
            )
            .build();
    }
    
    /**
     * Create the account management agent
     */
    private static BaseAgent createAccountAgent() {
        return LlmAgent.builder()
            .name("account-agent")
            .description("Manages account settings and profile updates")
            .model(MODEL_NAME)
            .instruction(
                "You are an account management specialist. Handle:\n" +
                "- Profile updates\n" +
                "- Email changes\n" +
                "- Password resets\n" +
                "- Account settings\n" +
                "- Tier upgrades/downgrades\n\n" +
                "Always:\n" +
                "- Verify customer identity\n" +
                "- Confirm changes before applying\n" +
                "- Explain implications of changes\n" +
                "- Send confirmation of updates"
            )
            .tools(
                "getCustomerAccount",
                "updateAccountSettings"
            )
            .build();
    }
    
    /**
     * Create the refund processing workflow (sequential agent)
     */
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
    
    /**
     * Create the refund validator agent (step 1)
     */
    private static BaseAgent createRefundValidatorAgent() {
        return LlmAgent.builder()
            .name("refund-validator")
            .description("Validates refund eligibility")
            .model(MODEL_NAME)
            .instruction(
                "You validate refund requests. Check:\n" +
                "1. Customer tier and eligibility\n" +
                "2. Payment date (must be within 30 days)\n" +
                "3. Account status\n" +
                "4. Refund reason\n\n" +
                "If eligible, pass to next step.\n" +
                "If not eligible, explain why and suggest alternatives."
            )
            .tools("validateRefundEligibility")
            .outputKey("validation_result")
            .build();
    }
    
    /**
     * Create the refund processor agent (step 2)
     */
    private static BaseAgent createRefundProcessorAgent() {
        return LlmAgent.builder()
            .name("refund-processor")
            .description("Processes approved refunds")
            .model(MODEL_NAME)
            .instruction(
                "You process approved refunds. Actions:\n" +
                "1. Confirm validation passed\n" +
                "2. Get refund amount from customer\n" +
                "3. Process the refund\n" +
                "4. Provide confirmation and timeline\n\n" +
                "Always explain refund processing times (5-7 business days)."
            )
            .tools("processRefund")
            .build();
    }
    
    // ==================== TOOL METHODS ====================
    
    /**
     * Get customer account information
     */
    @Schema(description = "Retrieve detailed customer account information")
    public static Map<String, Object> getCustomerAccount(
        @Schema(description = "Customer ID (format: CUST###)") String customerId,
        ToolContext toolContext
    ) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("[TOOL] getCustomerAccount called for: " + customerId);
            
            // Validate input
            if (customerId == null || customerId.trim().isEmpty()) {
                result.put("success", false);
                result.put("error", "Customer ID is required");
                return result;
            }
            
            // Normalize input
            customerId = customerId.trim().toUpperCase();
            
            // Validate format
            if (!customerId.matches("CUST\\d{3}")) {
                result.put("success", false);
                result.put("error", "Invalid customer ID format. Expected: CUST###");
                return result;
            }
            
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
            
        } catch (Exception e) {
            logger.severe("[ERROR] getCustomerAccount failed: " + e.getMessage());
            result.put("success", false);
            result.put("error", "System error: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * Process a payment
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
            
            // Validate customer ID
            customerId = validateCustomerId(customerId);
            
            // Validate amount
            double amount = validateAmount(amountObj);
            
            // Get customer
            Map<String, Object> customer = mockDatabase.get(customerId);
            if (customer == null) {
                result.put("success", false);
                result.put("error", "Customer not found");
                return result;
            }
            
            // Generate transaction ID
            String transactionId = generateTransactionId("TXN");
            
            // Update balance
            double currentBalance = ((Number) customer.get("accountBalance")).doubleValue();
            double newBalance = currentBalance + amount;
            customer.put("accountBalance", newBalance);
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
            result.put("error", "System error: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * Create a support ticket
     */
    @Schema(description = "Create a new support ticket")
    public static Map<String, Object> createTicket(
        @Schema(description = "Customer ID") String customerId,
        @Schema(description = "Ticket subject") String subject,
        @Schema(description = "Ticket description") String description,
        @Schema(description = "Priority (LOW, MEDIUM, HIGH)") String priority,
        ToolContext toolContext
    ) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("[TOOL] createTicket called for: " + customerId);
            
            // Validate inputs
            customerId = validateCustomerId(customerId);
            
            if (subject == null || subject.trim().isEmpty()) {
                result.put("success", false);
                result.put("error", "Subject is required");
                return result;
            }
            
            if (description == null || description.trim().isEmpty()) {
                result.put("success", false);
                result.put("error", "Description is required");
                return result;
            }
            
            // Validate priority
            String[] validPriorities = {"LOW", "MEDIUM", "HIGH"};
            priority = (priority != null ? priority.toUpperCase() : "MEDIUM");
            boolean validPriority = false;
            for (String p : validPriorities) {
                if (p.equals(priority)) {
                    validPriority = true;
                    break;
                }
            }
            if (!validPriority) {
                priority = "MEDIUM";
            }
            
            // Generate ticket ID
            String ticketId = generateTransactionId("TKT");
            
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
            
        } catch (Exception e) {
            logger.severe("[ERROR] createTicket failed: " + e.getMessage());
            result.put("success", false);
            result.put("error", "System error: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * Get tickets for a customer
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
            
            // Validate customer ID
            customerId = validateCustomerId(customerId);
            
            // Filter tickets
            List<Map<String, Object>> customerTickets = new ArrayList<>();
            for (Map<String, Object> ticket : ticketDatabase.values()) {
                if (customerId.equals(ticket.get("customerId"))) {
                    if (status == null || status.trim().isEmpty() || 
                        status.equalsIgnoreCase((String) ticket.get("status"))) {
                        customerTickets.add(ticket);
                    }
                }
            }
            
            result.put("success", true);
            result.put("tickets", customerTickets);
            result.put("count", customerTickets.size());
            
            logger.info("[SUCCESS] Retrieved " + customerTickets.size() + " tickets");
            return result;
            
        } catch (Exception e) {
            logger.severe("[ERROR] getTickets failed: " + e.getMessage());
            result.put("success", false);
            result.put("error", "System error: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * Update account settings
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
            
            // Validate customer ID
            customerId = validateCustomerId(customerId);
            
            // Get customer
            Map<String, Object> customer = mockDatabase.get(customerId);
            if (customer == null) {
                result.put("success", false);
                result.put("error", "Customer not found");
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
                    result.put("error", "Invalid tier. Must be: Basic, Premium, or Enterprise");
                    return result;
                }
                
                // Capitalize properly
                String properTier = normalizedTier.charAt(0) + normalizedTier.substring(1).toLowerCase();
                customer.put("tier", properTier);
                updates.put("tier", properTier);
            }
            
            if (updates.isEmpty()) {
                result.put("success", false);
                result.put("error", "No valid updates provided");
                return result;
            }
            
            result.put("success", true);
            result.put("updates", updates);
            result.put("message", "Account settings updated successfully");
            
            logger.info("[SUCCESS] Account updated: " + customerId);
            return result;
            
        } catch (Exception e) {
            logger.severe("[ERROR] updateAccountSettings failed: " + e.getMessage());
            result.put("success", false);
            result.put("error", "System error: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * Validate refund eligibility
     */
    @Schema(description = "Validate if a customer is eligible for a refund")
    public static Map<String, Object> validateRefundEligibility(
        @Schema(description = "Customer ID") String customerId,
        ToolContext toolContext
    ) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("[TOOL] validateRefundEligibility called for: " + customerId);
            
            // Validate customer ID
            customerId = validateCustomerId(customerId);
            
            // Get customer
            Map<String, Object> customer = mockDatabase.get(customerId);
            if (customer == null) {
                result.put("success", false);
                result.put("error", "Customer not found");
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
            String lastPaymentDate = (String) customer.get("lastPaymentDate");
            if (lastPaymentDate != null) {
                LocalDateTime lastPayment = LocalDateTime.parse(lastPaymentDate + "T00:00:00");
                LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
                if (lastPayment.isBefore(thirtyDaysAgo)) {
                    eligible = false;
                    reasons.add("Last payment was more than 30 days ago");
                }
            }
            
            // Check tier (Premium and Enterprise get priority)
            String tier = (String) customer.get("tier");
            
            result.put("success", true);
            result.put("eligible", eligible);
            result.put("tier", tier);
            result.put("reasons", reasons);
            
            if (eligible) {
                result.put("message", "Customer is eligible for refund");
                toolContext.getState().put("refund_eligible", true);
                toolContext.getState().put("refund_customer", customerId);
            } else {
                result.put("message", "Customer is not eligible for refund");
                toolContext.getState().put("refund_eligible", false);
            }
            
            logger.info("[SUCCESS] Validation complete for: " + customerId + " - Eligible: " + eligible);
            return result;
            
        } catch (Exception e) {
            logger.severe("[ERROR] validateRefundEligibility failed: " + e.getMessage());
            result.put("success", false);
            result.put("error", "System error: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * Process a refund
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
            
            // Check if validation passed
            Boolean eligible = (Boolean) toolContext.getState().get("refund_eligible");
            if (eligible == null || !eligible) {
                result.put("success", false);
                result.put("error", "Refund validation must be completed first");
                return result;
            }
            
            // Validate customer ID
            customerId = validateCustomerId(customerId);
            
            // Validate amount
            double amount = validateAmount(amountObj);
            
            // Get customer
            Map<String, Object> customer = mockDatabase.get(customerId);
            if (customer == null) {
                result.put("success", false);
                result.put("error", "Customer not found");
                return result;
            }
            
            // Check if amount doesn't exceed balance
            double currentBalance = ((Number) customer.get("accountBalance")).doubleValue();
            if (amount > currentBalance) {
                result.put("success", false);
                result.put("error", "Refund amount exceeds account balance");
                return result;
            }
            
            // Generate refund ID
            String refundId = generateTransactionId("REF");
            
            // Process refund (deduct from balance)
            double newBalance = currentBalance - amount;
            customer.put("accountBalance", newBalance);
            
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
            
        } catch (Exception e) {
            logger.severe("[ERROR] processRefund failed: " + e.getMessage());
            result.put("success", false);
            result.put("error", "System error: " + e.getMessage());
            return result;
        }
    }
    
    // ==================== CALLBACK METHODS ====================
    
    /**
     * Before model callback - content safety filter
     */
    private static String beforeModelCallback(String input, String agentName, String invocationId) {
        logger.info("[CALLBACK] beforeModel - Agent: " + agentName + " | Invocation: " + invocationId);
        
        // Enhanced sensitive keyword detection with regex patterns
        String[] sensitivePatterns = {
            "password", "passwd", "pwd",
            "credit.?card", "cvv", "cvc",
            "ssn", "social.?security",
            "api.?key", "secret", "token"
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
        
        // Check for credit card numbers (simple pattern)
        if (lowerInput.matches(".*\\b\\d{16}\\b.*")) {
            logger.warning("[SECURITY] Blocked potential credit card number - Agent: " + agentName);
            throw new SecurityException("Please do not include credit card numbers in your request.");
        }
        
        // Check for SSN format
        if (lowerInput.matches(".*\\b\\d{3}-\\d{2}-\\d{4}\\b.*")) {
            logger.warning("[SECURITY] Blocked potential SSN - Agent: " + agentName);
            throw new SecurityException("Please do not include social security numbers in your request.");
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
        logger.fine("[RESPONSE-LENGTH] " + response.length() + " characters");
    }
    
    /**
     * Before tool callback - caching
     */
    private static void beforeToolCallback(String toolName, Map<String, Object> parameters, 
                                          ToolContext toolContext) {
        logger.info("[CALLBACK] beforeTool - Tool: " + toolName + " | Parameters: " + parameters);
        
        // Simple caching for getCustomerAccount
        if ("getCustomerAccount".equals(toolName)) {
            String customerId = (String) parameters.get("customerId");
            if (customerId != null) {
                String cacheKey = "customer:" + customerId;
                Object cached = toolContext.getState().get(cacheKey);
                if (cached != null) {
                    logger.info("[CACHE-HIT] " + cacheKey + " - Tool: " + toolName);
                } else {
                    logger.info("[CACHE-MISS] " + cacheKey + " - Tool: " + toolName);
                }
            }
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Validate customer ID format
     */
    private static String validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        
        String normalized = customerId.trim().toUpperCase();
        
        if (!normalized.matches("CUST\\d{3,}")) {
            throw new IllegalArgumentException(
                "Invalid customer ID format. Expected format: CUST### (e.g., CUST001)"
            );
        }
        
        return normalized;
    }
    
    /**
     * Validate and parse amount
     */
    private static double validateAmount(Object amountObj) {
        double amount;
        
        if (amountObj instanceof Number) {
            amount = ((Number) amountObj).doubleValue();
        } else if (amountObj instanceof String) {
            try {
                amount = Double.parseDouble((String) amountObj);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid amount format: " + amountObj);
            }
        } else {
            throw new IllegalArgumentException("Amount must be a number or numeric string");
        }
        
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        
        if (amount > 100000) {
            throw new IllegalArgumentException("Amount exceeds maximum limit of $100,000");
        }
        
        // Round to 2 decimal places
        return Math.round(amount * 100.0) / 100.0;
    }
    
    /**
     * Generate a unique transaction/ticket ID
     */
    private static String generateTransactionId(String prefix) {
        String uuid = UUID.randomUUID().toString().toUpperCase();
        String shortId = uuid.substring(0, 8);
        return prefix + "-" + shortId;
    }
}
