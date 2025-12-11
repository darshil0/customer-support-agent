package com.example.support;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.google.adk.tools.ToolContext;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CustomerSupportAgentTest {

    @Mock private ToolContext toolContext;
    @Mock private Map<String, Object> stateMap; // Mock the state map inside ToolContext

    @BeforeEach
    public void setUp() {
        // Initialize Mocks
        MockitoAnnotations.openMocks(this);
        
        // Configure the mock ToolContext to return a state map
        when(toolContext.getState()).thenReturn(stateMap);

        // FIX: Reset the static mock database before every test. 
        // This ensures test isolation (critical for production stability).
        CustomerSupportAgent.resetMockData(); 
    }

    // --- Data Retrieval Tests (getCustomerAccount) ---

    @Test
    @DisplayName("Successfully retrieve a valid customer account")
    public void testGetCustomerAccount_success() {
        // CUST001 exists in the mock database with initial balance 1250.00
        Map<String, Object> result = CustomerSupportAgent.getCustomerAccount("CUST001", toolContext);
        
        assertTrue((Boolean) result.get("success"), "Result should indicate success");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> customer = (Map<String, Object>) result.get("customer");
        
        assertEquals("CUST001", customer.get("customerId"));
        assertEquals(1250.00, customer.get("accountBalance")); 
    }

    @Test
    @DisplayName("Fail when Customer ID is null")
    public void testGetCustomerAccount_nullId() {
        Map<String, Object> result = CustomerSupportAgent.getCustomerAccount(null, toolContext);
        assertFalse((Boolean) result.get("success"), "Result should fail on null ID");
        assertTrue(result.get("error").toString().contains("Customer ID is required"));
    }
    
    @Test
    @DisplayName("Fail when Customer ID format is invalid")
    public void testGetCustomerAccount_invalidFormat() {
        Map<String, Object> result = CustomerSupportAgent.getCustomerAccount("123", toolContext);
        assertFalse((Boolean) result.get("success"), "Result should fail on invalid format");
        assertTrue(result.get("error").toString().contains("Invalid customer ID format"));
    }

    // --- Billing/Payment Tests (processPayment) ---

    @Test
    @DisplayName("Successfully process a payment")
    public void testProcessPayment_success() {
        // CUST001 starts with a balance of 1250.00. This test runs in isolation due to the fix.
        Map<String, Object> result = CustomerSupportAgent.processPayment("CUST001", 100.00, toolContext);
        
        assertTrue((Boolean) result.get("success"), "Payment processing should succeed");
        assertTrue(result.containsKey("transactionId"));
        // New balance should be 1250.00 + 100.00 = 1350.00
        assertEquals(1350.00, result.get("newBalance")); 
    }

    @Test
    @DisplayName("Fail when processing payment with invalid amount")
    public void testProcessPayment_invalidAmount() {
        Map<String, Object> result = CustomerSupportAgent.processPayment("CUST001", -100.00, toolContext);
        
        assertFalse((Boolean) result.get("success"), "Result should fail on negative amount");
        assertTrue(result.get("error").toString().contains("Amount must be a positive value"));
    }

    // --- Technical Support Tests (createTicket) ---

    @Test
    @DisplayName("Successfully create a new ticket")
    public void testCreateTicket_success() {
        // Correct signature: (customerId, subject, description, priority, toolContext)
        Map<String, Object> result = CustomerSupportAgent.createTicket(
            "CUST002", 
            "Login Issue", 
            "Cannot access my account after reset.", 
            "HIGH", 
            toolContext
        );
        
        assertTrue((Boolean) result.get("success"), "Ticket creation should succeed");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> ticket = (Map<String, Object>) result.get("ticket");
        
        assertTrue(ticket.containsKey("ticketId"));
        assertEquals("OPEN", ticket.get("status")); 
    }

    @Test
    @DisplayName("Fail creating ticket with null subject")
    public void testCreateTicket_nullSubject() {
        Map<String, Object> result = CustomerSupportAgent.createTicket(
            "CUST002", 
            null, // Subject is null
            "Description", 
            "MEDIUM", 
            toolContext
        );
        assertFalse((Boolean) result.get("success"), "Result should fail on null subject");
        assertTrue(result.get("error").toString().contains("Subject is required"));
    }

    // --- Data Retrieval Tests (getTickets) ---

    @Test
    @DisplayName("Successfully retrieve customer tickets")
    public void testGetTickets_success() {
        // First, create a ticket for CUST003 to ensure data is present
        CustomerSupportAgent.createTicket(
            "CUST003", "Feature Request", "Need new feature.", "LOW", toolContext
        );
        
        // Retrieve tickets for CUST003
        Map<String, Object> result = CustomerSupportAgent.getTickets("CUST003", "OPEN", toolContext);
        
        assertTrue((Boolean) result.get("success"), "Ticket retrieval should succeed");
        assertTrue(result.containsKey("tickets"));
        
        @SuppressWarnings("unchecked")
        int count = (Integer) result.get("count");
        // We know at least one was created above
        assertTrue(count > 0, "Should retrieve at least one ticket"); 
    }

    @Test
    @DisplayName("Fail getting tickets with null Customer ID")
    public void testGetTickets_nullCustomerId() {
        Map<String, Object> result = CustomerSupportAgent.getTickets(null, "OPEN", toolContext);
        assertFalse((Boolean) result.get("success"), "Result should fail on null Customer ID");
        assertTrue(result.get("error").toString().contains("Customer ID is required"));
    }

    // --- Account Management Tests (updateAccountSettings) ---

    @Test
    @DisplayName("Successfully update customer email and tier")
    public void testUpdateAccountSettings_success() {
        // CUST003 starts as Enterprise tier
        Map<String, Object> result = CustomerSupportAgent.updateAccountSettings(
            "CUST003", 
            "bob.new@example.com", 
            "Premium", 
            toolContext
        );
        
        assertTrue((Boolean) result.get("success"), "Settings update should succeed");
        assertEquals("Account settings updated successfully", result.get("message"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> updates = (Map<String, String>) result.get("updates");
        assertTrue(updates.containsKey("email"));
        assertTrue(updates.containsKey("tier"));
        assertEquals("Premium", updates.get("tier"));
    }

    @Test
    @DisplayName("Fail update settings when no valid update is provided")
    public void testUpdateAccountSettings_nullSettings() {
        Map<String, Object> result = CustomerSupportAgent.updateAccountSettings(
            "CUST001", 
            null, // email is null
            null, // tier is null
            toolContext
        );
        assertFalse((Boolean) result.get("success"), "Result should fail when no updates are provided");
        assertTrue(result.get("error").toString().contains("No valid updates provided"));
    }

    // --- Refund Workflow Tests (validateRefundEligibility & processRefund) ---

    @Test
    @DisplayName("Successfully validate eligible customer for refund (CUST002)")
    public void testValidateRefundEligibility_eligible() {
        // CUST002 is configured to be eligible (e.g., recent payment).
        Map<String, Object> result = CustomerSupportAgent.validateRefundEligibility("CUST002", toolContext);
        
        assertTrue((Boolean) result.get("success"), "Validation call should succeed");
        assertTrue((Boolean) result.get("eligible"), "CUST002 should be eligible"); 
    }

    @Test
    @DisplayName("Fail validation for customer not eligible (CUST001 - old payment)")
    public void testValidateRefundEligibility_notEligible() {
        // CUST001 is configured to be ineligible (e.g., old payment date).
        Map<String, Object> result = CustomerSupportAgent.validateRefundEligibility("CUST001", toolContext);
        
        assertTrue((Boolean) result.get("success"), "Validation call should succeed even if ineligible");
        assertFalse((Boolean) result.get("eligible"), "CUST001 should not be eligible"); 
        
        @SuppressWarnings("unchecked")
        java.util.List<String> reasons = (java.util.List<String>) result.get("reasons");
        assertTrue(reasons.contains("Last payment was more than 30 days ago"));
    }
    
    @Test
    @DisplayName("Successfully process refund after validation")
    public void testProcessRefund_success() {
        // 1. Manually set state for eligibility (mimics the sequential agent step)
        when(stateMap.get("refund_eligible")).thenReturn(true);
        // CUST003 starts with 5000.00
        
        // 2. Process refund
        Map<String, Object> result = CustomerSupportAgent.processRefund("CUST003", 50.00, toolContext);
        
        assertTrue((Boolean) result.get("success"), "Refund should process successfully");
        assertTrue(result.containsKey("refundId"));
        // New balance is 5000.00 - 50.00 = 4950.00
        assertEquals(4950.00, result.get("newBalance")); 
        assertTrue(result.get("message").toString().contains("Refund processed successfully"));
    }

    @Test
    @DisplayName("Fail processing refund without prior validation")
    public void testProcessRefund_noValidation() {
        // Mock state returns null for "refund_eligible"
        when(stateMap.get("refund_eligible")).thenReturn(null); 
        
        Map<String, Object> result = CustomerSupportAgent.processRefund("CUST003", 50.00, toolContext);
        
        assertFalse((Boolean) result.get("success"), "Result should fail without validation flag");
        assertTrue(result.get("error").toString().contains("Refund validation must be completed first"));
    }

    @Test
    @DisplayName("Fail processing refund with negative amount")
    public void testProcessRefund_invalidAmount() {
        when(stateMap.get("refund_eligible")).thenReturn(true);
        Map<String, Object> result = CustomerSupportAgent.processRefund("CUST003", -50.00, toolContext);
        
        assertFalse((Boolean) result.get("success"), "Result should fail on negative refund amount");
        assertTrue(result.get("error").toString().contains("Amount must be a positive value"));
    }
}
