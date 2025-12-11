package com.example.support;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.google.adk.tools.ToolContext;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CustomerSupportAgentTest {

    // Since tool methods are static, we don't need to instantiate the class, 
    // but we use a mock for ToolContext.

    @Mock private ToolContext toolContext;
    @Mock private Map<String, Object> stateMap; // Mock the state map inside ToolContext

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Configure the mock ToolContext to return a state map
        when(toolContext.getState()).thenReturn(stateMap);
    }

    // --- Data Retrieval Tests (getCustomerAccount) ---

    @Test
    @DisplayName("Successfully retrieve a valid customer account")
    public void testGetCustomerAccount_success() {
        // CUST001 exists in the mock database
        Map<String, Object> result = CustomerSupportAgent.getCustomerAccount("CUST001", toolContext);
        
        assertTrue((Boolean) result.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, Object> customer = (Map<String, Object>) result.get("customer");
        
        assertEquals("CUST001", customer.get("customerId"));
        // Asserting against a specific key from the actual mock data structure
        assertEquals(1250.00, customer.get("accountBalance")); 
    }

    @Test
    @DisplayName("Fail when Customer ID is null")
    public void testGetCustomerAccount_nullId() {
        // Assertions.assertThrows is used for tools that throw exceptions on validation failures
        Map<String, Object> result = CustomerSupportAgent.getCustomerAccount(null, toolContext);
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("Customer ID is required"));
    }
    
    @Test
    @DisplayName("Fail when Customer ID format is invalid")
    public void testGetCustomerAccount_invalidFormat() {
        Map<String, Object> result = CustomerSupportAgent.getCustomerAccount("123", toolContext);
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("Invalid customer ID format"));
    }

    // --- Billing/Payment Tests (processPayment) ---

    @Test
    @DisplayName("Successfully process a payment")
    public void testProcessPayment_success() {
        // Note: CUST001 starts with a balance of 1250.00
        Map<String, Object> result = CustomerSupportAgent.processPayment("CUST001", 100.00, toolContext);
        
        assertTrue((Boolean) result.get("success"));
        assertTrue(result.containsKey("transactionId"));
        // New balance should be 1250.00 + 100.00 = 1350.00 (This test impacts the static mock DB)
        assertEquals(1350.00, result.get("newBalance")); 
    }

    @Test
    @DisplayName("Fail when processing payment with invalid amount")
    public void testProcessPayment_invalidAmount() {
        // The tool returns a map with "success: false" and "error" key on validation failure.
        Map<String, Object> result = CustomerSupportAgent.processPayment("CUST001", -100.00, toolContext);
        
        assertFalse((Boolean) result.get("success"));
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
        
        assertTrue((Boolean) result.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, Object> ticket = (Map<String, Object>) result.get("ticket");
        
        assertTrue(ticket.containsKey("ticketId"));
        assertEquals("OPEN", ticket.get("status")); // Asserting against the actual key and default value
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
        assertFalse((Boolean) result.get("success"));
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
        
        assertTrue((Boolean) result.get("success"));
        assertTrue(result.containsKey("tickets"));
        
        @SuppressWarnings("unchecked")
        int count = (Integer) result.get("count");
        assertTrue(count > 0); 
    }

    @Test
    @DisplayName("Fail getting tickets with null Customer ID")
    public void testGetTickets_nullCustomerId() {
        Map<String, Object> result = CustomerSupportAgent.getTickets(null, "OPEN", toolContext);
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("Customer ID is required"));
    }

    // --- Account Management Tests (updateAccountSettings) ---

    @Test
    @DisplayName("Successfully update customer email and tier")
    public void testUpdateAccountSettings_success() {
        // Correct signature: (customerId, email, tier, toolContext)
        Map<String, Object> result = CustomerSupportAgent.updateAccountSettings(
            "CUST003", 
            "bob.new@example.com", 
            "Premium", 
            toolContext
        );
        
        assertTrue((Boolean) result.get("success"));
        assertEquals("Account settings updated successfully", result.get("message"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> updates = (Map<String, String>) result.get("updates");
        assertTrue(updates.containsKey("email"));
        assertTrue(updates.containsKey("tier"));
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
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("No valid updates provided"));
    }

    // --- Refund Workflow Tests (validateRefundEligibility & processRefund) ---

    @Test
    @DisplayName("Successfully validate eligible customer for refund (CUST002)")
    public void testValidateRefundEligibility_eligible() {
        // CUST002 has a recent payment date (2024-12-01) and active status.
        // Mock ToolContext state is used implicitly by the tool.
        Map<String, Object> result = CustomerSupportAgent.validateRefundEligibility("CUST002", toolContext);
        
        assertTrue((Boolean) result.get("success"));
        assertTrue((Boolean) result.get("eligible")); // Correct key is 'eligible'
    }

    @Test
    @DisplayName("Fail validation for customer not eligible (CUST001 - old payment)")
    public void testValidateRefundEligibility_notEligible() {
        // CUST001 has an old lastPaymentDate ("2024-11-25"), which will fail the 30-day check as of 2025-12-10 (current time context).
        Map<String, Object> result = CustomerSupportAgent.validateRefundEligibility("CUST001", toolContext);
        
        assertTrue((Boolean) result.get("success"));
        assertFalse((Boolean) result.get("eligible")); 
        
        @SuppressWarnings("unchecked")
        java.util.List<String> reasons = (java.util.List<String>) result.get("reasons");
        assertTrue(reasons.contains("Last payment was more than 30 days ago"));
    }
    
    @Test
    @DisplayName("Successfully process refund after validation")
    public void testProcessRefund_success() {
        // 1. Manually set state for eligibility, as this is sequential workflow dependency
        when(stateMap.get("refund_eligible")).thenReturn(true);
        // Note: CUST003 starts with 5000.00
        
        // 2. Process refund
        Map<String, Object> result = CustomerSupportAgent.processRefund("CUST003", 50.00, toolContext);
        
        assertTrue((Boolean) result.get("success"));
        assertTrue(result.containsKey("refundId"));
        // New balance is 5000.00 - 50.00 = 4950.00
        assertEquals(4950.00, result.get("newBalance")); 
        assertTrue(result.get("message").toString().contains("Refund processed successfully"));
    }

    @Test
    @DisplayName("Fail processing refund without prior validation")
    public void testProcessRefund_noValidation() {
        // Default stateMap mock will return null for "refund_eligible"
        Map<String, Object> result = CustomerSupportAgent.processRefund("CUST003", 50.00, toolContext);
        
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("Refund validation must be completed first"));
    }

    @Test
    @DisplayName("Fail processing refund with negative amount")
    public void testProcessRefund_invalidAmount() {
        when(stateMap.get("refund_eligible")).thenReturn(true);
        Map<String, Object> result = CustomerSupportAgent.processRefund("CUST003", -50.00, toolContext);
        
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("Amount must be a positive value"));
    }
}
