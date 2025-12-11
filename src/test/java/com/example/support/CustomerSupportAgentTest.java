// =========================================================================
// 1. CustomerSupportAgent.java (The Tool Implementations)
// =========================================================================
package com.example.support;

import com.google.adk.tools.ToolContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * A component containing the business logic methods exposed as tools to the LLM agents.
 * This class uses static mock data for demonstration purposes.
 */
@Component
public class CustomerSupportAgent {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final List<String> VALID_TIERS = Arrays.asList("Basic", "Premium", "Enterprise");
    private static final List<String> VALID_PRIORITIES = Arrays.asList("LOW", "MEDIUM", "HIGH");
    private static final Pattern CUSTOMER_ID_PATTERN = Pattern.compile("^CUST\\d{3}$");
    
    // --- Mock Database (Static for state persistence across calls in a session) ---
    private static Map<String, Map<String, Object>> CUSTOMER_DB;
    private static Map<String, List<Map<String, Object>>> TICKET_DB;
    private static AtomicInteger ticketIdCounter = new AtomicInteger(1000);

    // Initializer for mock data
    static {
        initializeMockData();
    }
    
    // Public method to reset data for testing
    public static void resetMockData() {
        initializeMockData();
    }

    private static void initializeMockData() {
        // CUST001: Premium, Large Balance, Ineligible for refund (old payment)
        CUSTOMER_DB = new HashMap<>();
        CUSTOMER_DB.put("CUST001", new HashMap<>(Map.of(
            "customerId", "CUST001",
            "name", "John Doe",
            "email", "john.doe@acme.com",
            "tier", "Premium",
            "accountBalance", 1250.00,
            "lastPaymentDate", LocalDate.now().minusDays(45).format(DATE_FORMATTER) // Ineligible
        )));

        // CUST002: Basic, No Balance, Eligible for refund (recent payment)
        CUSTOMER_DB.put("CUST002", new HashMap<>(Map.of(
            "customerId", "CUST002",
            "name", "Jane Smith",
            "email", "jane.smith@acme.com",
            "tier", "Basic",
            "accountBalance", 0.00,
            "lastPaymentDate", LocalDate.now().minusDays(5).format(DATE_FORMATTER) // Eligible
        )));

        // CUST003: Enterprise, Large Balance, Eligible for refund (recent payment)
        CUSTOMER_DB.put("CUST003", new HashMap<>(Map.of(
            "customerId", "CUST003",
            "name", "Bob Johnson",
            "email", "bob.j@acme.com",
            "tier", "Enterprise",
            "accountBalance", 5000.00,
            "lastPaymentDate", LocalDate.now().minusDays(10).format(DATE_FORMATTER) // Eligible
        )));

        TICKET_DB = new HashMap<>();
        TICKET_DB.put("CUST001", new ArrayList<>(List.of(
            Map.of("ticketId", "Ticket-100", "subject", "Login Fail", "status", "CLOSED")
        )));
        
        ticketIdCounter.set(1001); // Reset counter
    }

    // --- Helper for validation ---
    private Map<String, Object> validationError(String customerId, String message) {
        return Map.of("success", false, "customerId", customerId, "error", message);
    }
    
    private Map<String, Object> getCustomerRecord(String customerId) {
        if (customerId == null || !CUSTOMER_ID_PATTERN.matcher(customerId).matches()) {
            return null;
        }
        return CUSTOMER_DB.get(customerId);
    }
    
    // --- Tool Methods ---

    /**
     * Retrieves customer account details.
     * @param customerId The ID of the customer (e.g., CUST001).
     * @param toolContext The context object (unused in this mock, but required by ADK signature).
     * @return A map containing success status and customer data or an error message.
     */
    public Map<String, Object> getCustomerAccount(String customerId, ToolContext toolContext) {
        if (customerId == null) {
            return validationError(customerId, "Customer ID is required.");
        }
        if (!CUSTOMER_ID_PATTERN.matcher(customerId).matches()) {
            return validationError(customerId, "Invalid customer ID format. Must be CUST###.");
        }
        
        Map<String, Object> customer = getCustomerRecord(customerId);
        if (customer == null) {
            return validationError(customerId, "Customer ID " + customerId + " not found.");
        }

        return Map.of("success", true, "customer", customer);
    }

    /**
     * Processes a payment and updates the customer's balance.
     * @param customerId The customer ID.
     * @param amount The payment amount.
     * @param toolContext The context object.
     * @return A map with success status, transaction ID, and new balance.
     */
    public Map<String, Object> processPayment(String customerId, Double amount, ToolContext toolContext) {
        if (amount == null || amount <= 0) {
            return validationError(customerId, "Amount must be a positive value.");
        }
        
        Map<String, Object> customer = getCustomerRecord(customerId);
        if (customer == null) {
            return validationError(customerId, "Customer ID " + customerId + " not found.");
        }

        // Update mock DB
        Double currentBalance = (Double) customer.get("accountBalance");
        Double newBalance = currentBalance + amount;
        
        customer.put("accountBalance", newBalance);
        customer.put("lastPaymentDate", LocalDate.now().format(DATE_FORMATTER));

        return Map.of(
            "success", true,
            "transactionId", "TXN-" + System.currentTimeMillis(),
            "newBalance", newBalance
        );
    }

    /**
     * Creates a new support ticket.
     * @param customerId The customer ID.
     * @param subject The ticket subject.
     * @param description The ticket description.
     * @param priority The ticket priority (LOW, MEDIUM, HIGH).
     * @param toolContext The context object.
     * @return A map with success status and the new ticket details.
     */
    public Map<String, Object> createTicket(String customerId, String subject, String description, String priority, ToolContext toolContext) {
        if (customerId == null || subject == null || description == null) {
            return validationError(customerId, "Customer ID, Subject, and Description are required.");
        }
        if (!VALID_PRIORITIES.contains(priority)) {
            return validationError(customerId, "Invalid priority. Must be one of: " + VALID_PRIORITIES);
        }

        Map<String, Object> customer = getCustomerRecord(customerId);
        if (customer == null) {
            return validationError(customerId, "Customer ID " + customerId + " not found.");
        }
        
        // Generate new ticket and save to mock DB
        String newTicketId = "Ticket-" + ticketIdCounter.getAndIncrement();
        Map<String, Object> newTicket = Map.of(
            "ticketId", newTicketId,
            "subject", subject,
            "description", description,
            "priority", priority,
            "status", "OPEN",
            "createdDate", LocalDate.now().format(DATE_FORMATTER)
        );

        TICKET_DB.computeIfAbsent(customerId, k -> new ArrayList<>()).add(newTicket);
        
        return Map.of("success", true, "ticket", newTicket);
    }

    /**
     * Retrieves a list of customer tickets filtered by status.
     * @param customerId The customer ID.
     * @param status Optional filter for ticket status (e.g., "OPEN", "CLOSED").
     * @param toolContext The context object.
     * @return A map with success status and a list of tickets.
     */
    public Map<String, Object> getTickets(String customerId, String status, ToolContext toolContext) {
        if (customerId == null) {
            return validationError(customerId, "Customer ID is required.");
        }
        if (getCustomerRecord(customerId) == null) {
            return validationError(customerId, "Customer ID " + customerId + " not found.");
        }

        List<Map<String, Object>> allTickets = TICKET_DB.getOrDefault(customerId, Collections.emptyList());
        
        List<Map<String, Object>> filteredTickets = allTickets.stream()
            .filter(ticket -> status == null || status.isEmpty() || status.equalsIgnoreCase((String) ticket.get("status")))
            .toList();

        return Map.of("success", true, "count", filteredTickets.size(), "tickets", filteredTickets);
    }

    /**
     * Updates customer account settings (email or tier).
     * @param customerId The customer ID.
     * @param email New email address (optional).
     * @param tier New subscription tier (optional).
     * @param toolContext The context object.
     * @return A map with success status and a description of changes.
     */
    public Map<String, Object> updateAccountSettings(String customerId, String email, String tier, ToolContext toolContext) {
        Map<String, Object> customer = getCustomerRecord(customerId);
        if (customer == null) {
            return validationError(customerId, "Customer ID " + customerId + " not found.");
        }

        if (email == null && tier == null) {
            return validationError(customerId, "No valid updates provided. Specify email or tier.");
        }
        
        Map<String, String> updates = new HashMap<>();

        if (email != null && !email.isBlank()) {
            customer.put("email", email);
            updates.put("email", email);
        }

        if (tier != null && !tier.isBlank()) {
            if (!VALID_TIERS.contains(tier)) {
                return validationError(customerId, "Invalid tier specified. Must be one of: " + VALID_TIERS);
            }
            customer.put("tier", tier);
            updates.put("tier", tier);
        }

        return Map.of("success", true, "message", "Account settings updated successfully", "updates", updates);
    }

    /**
     * Refund Workflow Step 1: Validates if a customer is eligible for a refund.
     * @param customerId The customer ID.
     * @param toolContext The context object to store eligibility state.
     * @return A map containing success status, eligibility, and reasons.
     */
    public Map<String, Object> validateRefundEligibility(String customerId, ToolContext toolContext) {
        Map<String, Object> customer = getCustomerRecord(customerId);
        if (customer == null) {
            // Note: Validation succeeds even on failure, but sets eligible=false
            toolContext.getState().put("refund_eligible", false); 
            return Map.of("success", true, "eligible", false, "reasons", List.of("Customer ID " + customerId + " not found."));
        }

        LocalDate lastPaymentDate = LocalDate.parse((String) customer.get("lastPaymentDate"), DATE_FORMATTER);
        LocalDate cutOffDate = LocalDate.now().minusDays(30);
        
        List<String> reasons = new ArrayList<>();
        boolean eligible = true;

        if (lastPaymentDate.isBefore(cutOffDate)) {
            reasons.add("Last payment was more than 30 days ago.");
            eligible = false;
        }

        // Store the result in the ToolContext state for the next sequential agent step
        toolContext.getState().put("refund_eligible", eligible);
        
        return Map.of("success", true, "eligible", eligible, "reasons", reasons);
    }

    /**
     * Refund Workflow Step 2: Processes the refund, checking the eligibility flag from the context.
     * @param customerId The customer ID.
     * @param amount The refund amount.
     * @param toolContext The context object containing eligibility state.
     * @return A map with success status and refund details.
     */
    public Map<String, Object> processRefund(String customerId, Double amount, ToolContext toolContext) {
        if (toolContext.getState().get("refund_eligible") == null) {
            return validationError(customerId, "Refund validation must be completed first.");
        }
        
        Boolean isEligible = (Boolean) toolContext.getState().get("refund_eligible");
        if (!isEligible) {
            return validationError(customerId, "The customer is not eligible for a refund.");
        }
        
        if (amount == null || amount <= 0) {
            return validationError(customerId, "Amount must be a positive value.");
        }
        
        Map<String, Object> customer = getCustomerRecord(customerId);
        if (customer == null) {
            return validationError(customerId, "Customer ID " + customerId + " not found.");
        }

        // Refund logic
        Double currentBalance = (Double) customer.get("accountBalance");
        Double newBalance = currentBalance - amount;
        
        if (newBalance < -500.00) { // arbitrary max refund limit for demo
             return validationError(customerId, "Refund amount exceeds maximum allowable limit relative to balance.");
        }

        customer.put("accountBalance", newBalance);

        // Clear the eligibility flag after use
        toolContext.getState().remove("refund_eligible");

        return Map.of(
            "success", true,
            "refundId", "REF-" + System.currentTimeMillis(),
            "newBalance", newBalance,
            "message", "Refund processed successfully. Funds will appear in 5-7 business days."
        );
    }
}


// =========================================================================
// 2. AgentConfiguration.java (The Agent Definitions)
// =========================================================================
package com.example.support;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.tools.FunctionTool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfiguration {

    // --- Dependency Injection via Constructor (Spring Best Practice) ---
    private final CustomerSupportAgent customerSupportAgent;

    public AgentConfiguration(CustomerSupportAgent customerSupportAgent) {
        this.customerSupportAgent = customerSupportAgent;
    }

    /**
     * Defines the Root Agent for the customer support system.
     * This agent acts as a Router, deciding which sub-agent (tool) to invoke.
     */
    @Bean
    public BaseAgent rootCustomerSupportAgent() {
        return LlmAgent.builder()
                // Use a descriptive name for the main entry agent
                .name("customer-support-agent")
                .description("The main router agent for customer inquiries.")
                .model("gemini-1.5-flash")
                .instruction("You are a helpful customer support agent for Acme Corp. Your job is to analyze the user's request and delegate the task to one of your specialized sub-agents (tools) to handle billing, technical, account, or refund requests. You must only use the tools provided.")
                .tools(
                        createBillingAgent(),
                        createTechnicalSupportAgent(),
                        createAccountAgent(),
                        createRefundWorkflow())
                .build();
    }
    
    // --- Sub-Agents Defined Below ---

    private BaseAgent createBillingAgent() {
        return LlmAgent.builder()
                .name("billing-agent")
                .description("Handles billing and payment inquiries. Use this agent for customer balance checks, payment processing, or retrieving payment history tickets.")
                .tools(
                        FunctionTool.create(customerSupportAgent, "getCustomerAccount"),
                        FunctionTool.create(customerSupportAgent, "processPayment"),
                        FunctionTool.create(customerSupportAgent, "getTickets"))
                .build();
    }

    private BaseAgent createTechnicalSupportAgent() {
        return LlmAgent.builder()
                .name("technical-support-agent")
                .description("Assists with technical issues. Use this agent to create new support tickets or retrieve existing ticket details.")
                .tools(
                        FunctionTool.create(customerSupportAgent, "getCustomerAccount"),
                        FunctionTool.create(customerSupportAgent, "createTicket"),
                        FunctionTool.create(customerSupportAgent, "getTickets"))
                .build();
    }

    private BaseAgent createAccountAgent() {
        return LlmAgent.builder()
                .name("account-agent")
                .description("Manages customer account settings. Use this agent for updating customer contact information or subscription tiers.")
                .tools(
                        FunctionTool.create(customerSupportAgent, "getCustomerAccount"),
                        FunctionTool.create(customerSupportAgent, "updateAccountSettings"))
                .build();
    }

    /**
     * Creates a SequentialAgent that enforces a two-step process:
     * 1. Refund eligibility validation
     * 2. Refund processing
     */
    private BaseAgent createRefundWorkflow() {
        LlmAgent validator =
                LlmAgent.builder()
                        .name("refund-validator")
                        .instruction("Your sole task is to determine the customer ID and then call the validateRefundEligibility tool.")
                        .tools(FunctionTool.create(customerSupportAgent, "validateRefundEligibility"))
                        // CRITICAL: Must include outputKey to pass state to the Sequential Agent
                        .outputKey("validation_result") 
                        .build();

        LlmAgent processor =
                LlmAgent.builder()
                        .name("refund-processor")
                        .instruction("Your sole task is to process the refund by calling the processRefund tool, using information passed from the previous step.")
                        .tools(FunctionTool.create(customerSupportAgent, "processRefund"))
                        .build();

        return SequentialAgent.builder()
                .name("refund-processor-workflow")
                .description("A two-step sequential workflow to process customer refunds: first validation, then processing. Use this for all refund requests.")
                .subAgents(validator, processor)
                .build();
    }
}


// =========================================================================
// 3. CustomerSupportAgentUnifiedTest.java (Unit & Integration Tests)
// =========================================================================
package com.example.support;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.adk.agents.BaseAgent;
import com.google.adk.testing.AgentTestHarness;
import com.google.adk.testing.AgentTestResult;
import com.google.adk.tools.ToolContext;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// --- Root Test Class for Unit Tests (Standard Junit Setup) ---
@DisplayName("Customer Support Agent Test Suite")
public class CustomerSupportAgentUnifiedTest {

    // Shared setup for all Unit Tests
    @Mock 
    private ToolContext toolContext;
    @Mock 
    private Map<String, Object> stateMap;

    @BeforeEach
    public void setUp() {
        // Initialize Mocks for the Unit Test context
        MockitoAnnotations.openMocks(this);
        
        // Configure the mock ToolContext to return a state map
        when(toolContext.getState()).thenReturn(stateMap);

        // Reset the static mock database before every unit test.
        CustomerSupportAgent.resetMockData(); 
    }

    // ====================================================================
    // 1. UNIT TESTS: Testing Isolated Tool Logic using Mockito
    // ====================================================================
    @Nested
    @DisplayName("1. Tool Logic Unit Tests (Mockito)")
    class ToolLogicUnitTests {

        // --- Data Retrieval/Validation Tests ---

        @Test
        @DisplayName("1.1 Successfully retrieve a valid customer account")
        public void testGetCustomerAccount_success() {
            Map<String, Object> result = CustomerSupportAgent.getCustomerAccount("CUST001", toolContext);
            assertTrue((Boolean) result.get("success"));
            @SuppressWarnings("unchecked")
            Map<String, Object> customer = (Map<String, Object>) result.get("customer");
            assertEquals(1250.00, customer.get("accountBalance")); 
        }

        @Test
        @DisplayName("1.2 Fail when Customer ID does not exist")
        public void testGetCustomerAccount_nonExistentId() {
            Map<String, Object> result = CustomerSupportAgent.getCustomerAccount("CUST999", toolContext);
            assertFalse((Boolean) result.get("success"));
            assertTrue(result.get("error").toString().contains("Customer ID CUST999 not found"));
        }

        // --- Billing/Payment Tests ---
        @Test
        @DisplayName("1.3 Successfully process a payment")
        public void testProcessPayment_success() {
            Map<String, Object> result = CustomerSupportAgent.processPayment("CUST001", 100.00, toolContext);
            assertTrue((Boolean) result.get("success"));
            assertEquals(1350.00, result.get("newBalance")); 
        }

        @Test
        @DisplayName("1.4 Fail when processing payment with invalid amount")
        public void testProcessPayment_invalidAmount() {
            Map<String, Object> result = CustomerSupportAgent.processPayment("CUST001", -100.00, toolContext);
            assertFalse((Boolean) result.get("success"));
            assertTrue(result.get("error").toString().contains("Amount must be a positive value"));
        }

        // --- Technical Support Tests ---
        @Test
        @DisplayName("1.5 Successfully create a new ticket")
        public void testCreateTicket_success() {
            Map<String, Object> result = CustomerSupportAgent.createTicket(
                "CUST002", "Login Issue", "Cannot access my account after reset.", "HIGH", toolContext
            );
            assertTrue((Boolean) result.get("success"));
            @SuppressWarnings("unchecked")
            Map<String, Object> ticket = (Map<String, Object>) result.get("ticket");
            assertEquals("OPEN", ticket.get("status")); 
        }
        
        @Test
        @DisplayName("1.6 Fail creating ticket with invalid priority")
        public void testCreateTicket_invalidPriority() {
            Map<String, Object> result = CustomerSupportAgent.createTicket(
                "CUST002", "Subject", "Description", "URGENT", toolContext
            );
            assertFalse((Boolean) result.get("success"));
            assertTrue(result.get("error").toString().contains("Invalid priority"));
        }

        // --- Account Management Tests ---
        @Test
        @DisplayName("1.7 Fail updating account settings with invalid tier")
        public void testUpdateAccountSettings_invalidTier() {
            Map<String, Object> result = CustomerSupportAgent.updateAccountSettings(
                "CUST001", null, "GOLD", toolContext 
            );
            assertFalse((Boolean) result.get("success"));
            assertTrue(result.get("error").toString().contains("Invalid tier specified"));
        }

        // --- Refund Workflow Tests (State Management) ---
        @Test
        @DisplayName("1.8 Successfully validate eligible customer and write state (CUST002)")
        public void testValidateRefundEligibility_eligible() {
            Map<String, Object> result = CustomerSupportAgent.validateRefundEligibility("CUST002", toolContext);
            assertTrue((Boolean) result.get("eligible")); 
            // Verify state update for SequentialAgent
            verify(stateMap).put("refund_eligible", true); 
        }

        @Test
        @DisplayName("1.9 Fail validation for ineligible customer and write state (CUST001)")
        public void testValidateRefundEligibility_notEligible() {
            Map<String, Object> result = CustomerSupportAgent.validateRefundEligibility("CUST001", toolContext);
            assertFalse((Boolean) result.get("eligible")); 
            // Verify state update for SequentialAgent
            verify(stateMap).put("refund_eligible", false); 
        }

        @Test
        @DisplayName("1.10 Successfully process refund after validation")
        public void testProcessRefund_success() {
            when(stateMap.get("refund_eligible")).thenReturn(true);
            Map<String, Object> result = CustomerSupportAgent.processRefund("CUST003", 50.00, toolContext);
            assertTrue((Boolean) result.get("success"));
            assertEquals(4950.00, result.get("newBalance")); 
        }

        @Test
        @DisplayName("1.11 Fail processing refund without prior validation flag")
        public void testProcessRefund_noValidation() {
            when(stateMap.get("refund_eligible")).thenReturn(null);
            Map<String, Object> result = CustomerSupportAgent.processRefund("CUST003", 50.00, toolContext);
            assertFalse((Boolean) result.get("success"));
            assertTrue(result.get("error").toString().contains("Refund validation must be completed first"));
        }
    }

    // ====================================================================
    // 2. INTEGRATION TESTS: Testing Agent Architecture using AgentTestHarness
    // ====================================================================
    @Nested
    @SpringBootTest(classes = {CustomerSupportAgent.class, AgentConfiguration.class})
    @DisplayName("2. Agent Architecture Integration Tests (Harness)")
    class AgentArchitectureIntegrationTests {

        // Dependencies injected by Spring Boot Test
        @Autowired private AgentTestHarness testHarness;
        @Autowired private BaseAgent rootCustomerSupportAgent;
        @Autowired private CustomerSupportAgent customerSupportAgent;

        private static final String JOHN_DOE_ID = "CUST001"; 
        private static final String JANE_SMITH_ID = "CUST002";

        @BeforeEach
        void setupIntegration() {
            // Reset state and set the root agent for the harness
            CustomerSupportAgent.resetMockData(); 
            testHarness.setAgent(rootCustomerSupportAgent);
        }

        // --- ROUTING TESTS ---

        @Test
        @DisplayName("2.1 Should route billing query to billing-agent")
        void testRoutingToBillingAgent() {
            AgentTestResult result = testHarness.process("I need to pay my balance for " + JOHN_DOE_ID);
            
            assertEquals(1, result.getFunctionCalls().size());
            assertTrue(result.getFunctionCalls().get(0).getToolName().contains("billing-agent"));
        }
        
        @Test
        @DisplayName("2.2 Should route technical query for general system issue")
        void testRoutingToTechnicalAgent_GeneralIssue() {
            AgentTestResult result = testHarness.process("My application is crashing, I need help.");
            
            assertEquals(1, result.getFunctionCalls().size());
            assertTrue(result.getFunctionCalls().get(0).getToolName().contains("technical-support-agent"));
        }

        @Test
        @DisplayName("2.3 Should route refund query to refund-processor-workflow")
        void testRoutingToRefundWorkflow() {
            AgentTestResult result = testHarness.process("I want a refund for " + JOHN_DOE_ID);
            
            assertEquals(1, result.getFunctionCalls().size());
            assertTrue(result.getFunctionCalls().get(0).getToolName().contains("refund-processor-workflow"));
        }

        // --- FULL E2E WORKFLOW TESTS ---

        @Test
        @DisplayName("2.4 E2E: Billing agent should process payment successfully and update balance")
        void testE2E_ProcessPaymentSuccess() {
            // Step 1: Trigger agent to route to payment
            AgentTestResult routeResult = testHarness.process("I need to pay $500 for " + JOHN_DOE_ID); 
            
            // Step 2: The agent executes tool calls
            AgentTestResult finalResult = testHarness.process(routeResult.getResponse(), "CUST001", "500");
            
            assertTrue(finalResult.getFinalResponse().contains("Payment processed successfully"));
            
            // Verification: Check if the balance was actually updated
            @SuppressWarnings("unchecked")
            Map<String, Object> accountInfo = (Map<String, Object>) customerSupportAgent.getCustomerAccount(JOHN_DOE_ID, null).get("customer");
            assertEquals(1750.00, accountInfo.get("accountBalance")); // 1250 + 500
        }

        @Test
        @DisplayName("2.5 E2E: Account agent should successfully update email")
        void testE2E_AccountAgentUpdateEmail() {
            final String NEW_EMAIL = "new.email@acme.com";
            
            // Step 1: Route to Account Agent
            AgentTestResult routeResult = testHarness.process(
                "I need to change my email for " + JANE_SMITH_ID + " to " + NEW_EMAIL
            );
            
            // Step 2: The agent executes the updateAccountSettings tool
            AgentTestResult finalResult = testHarness.process(
                routeResult.getResponse(), JANE_SMITH_ID, NEW_EMAIL, null 
            );
            
            assertTrue(finalResult.getFinalResponse().contains("updated successfully"));
            
            // Verification: Check if the email was actually updated in the mock DB
            @SuppressWarnings("unchecked")
            Map<String, Object> customer = (Map<String, Object>) customerSupportAgent.getCustomerAccount(JANE_SMITH_ID, null).get("customer");
            assertEquals(NEW_EMAIL, customer.get("email"));
        }

        @Test
        @DisplayName("2.6 E2E: Refund workflow should FAIL for ineligible customer (CUST001)")
        void testRefundWorkflowIneligible() {
            // CUST001 is ineligible (old payment)
            AgentTestResult result = testHarness.process("I want a refund for " + JOHN_DOE_ID);

            // Step 1: Sequential Agent calls refund-validator
            AgentTestResult validationResult = testHarness.process(result.getResponse());

            // The final response should clearly state the reason for ineligibility
            assertTrue(validationResult.getFinalResponse().contains("not eligible"));
            
            // Assert that the workflow terminated early (did not call processRefund)
            assertNull(testHarness.getToolContext().getState().get("last_refund_id")); 
        }
    }
}
