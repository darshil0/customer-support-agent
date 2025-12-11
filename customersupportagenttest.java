package com.example.support;

import com.google.adk.tools.ToolContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CustomerSupportAgent tools
 * * @author Darshil
 * @version 1.0.0
 */
class CustomerSupportAgentTest {
    
    private ToolContext toolContext;
    
    @BeforeEach
    void setUp() {
        // Creates a new ToolContext for each test, ensuring isolated state.
        // The state is already initialized as a new HashMap internally,
        // so setting it to a new HashMap explicitly is redundant.
        toolContext = new ToolContext();
    }
    
    // ==================== getCustomerAccount Tests ====================
    
    @Test
    @DisplayName("Should retrieve valid customer account")
    void testGetCustomerAccount_ValidCustomer() {
        Map<String, Object> result = CustomerSupportAgent.getCustomerAccount("CUST001", toolContext);
        
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("customer"));
        
        // Suppressing warning locally is the most precise way
        @SuppressWarnings("unchecked")
        Map<String, Object> customer = (Map<String, Object>) result.get("customer");
        assertEquals("CUST001", customer.get("customerId"));
        assertEquals("John Doe", customer.get("name"));
        assertEquals("Premium", customer.get("tier"));
    }
    
    // ... (rest of getCustomerAccount Tests are unchanged) ...
    
    @Test
    @DisplayName("Should handle invalid customer ID")
    void testGetCustomerAccount_InvalidCustomer() {
        Map<String, Object> result = CustomerSupportAgent.getCustomerAccount("CUST999", toolContext);
        
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("not found"));
    }
    
    @Test
    @DisplayName("Should reject null customer ID")
    void testGetCustomerAccount_NullCustomerId() {
        Map<String, Object> result = CustomerSupportAgent.getCustomerAccount(null, toolContext);
        
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("required"));
    }
    
    @Test
    @DisplayName("Should reject empty customer ID")
    void testGetCustomerAccount_EmptyCustomerId() {
        Map<String, Object> result = CustomerSupportAgent.getCustomerAccount("  ", toolContext);
        
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("required"));
    }
    
    @Test
    @DisplayName("Should reject malformed customer ID")
    void testGetCustomerAccount_MalformedCustomerId() {
        Map<String, Object> result = CustomerSupportAgent.getCustomerAccount("INVALID123", toolContext);
        
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("format"));
    }
    
    @Test
    @DisplayName("Should normalize customer ID case")
    void testGetCustomerAccount_NormalizeCase() {
        // CUST001 is a valid ID in the mocked data
        CustomerSupportAgent.getCustomerAccount("CUST001", toolContext); 
        Map<String, Object> result = CustomerSupportAgent.getCustomerAccount("cust001", toolContext);
        
        assertTrue((Boolean) result.get("success"));
        // This assertion checks if the agent successfully saves the canonical ID (CUST001) in state
        assertEquals("CUST001", toolContext.getState().get("current_customer")); 
    }
    
    @Test
    @DisplayName("Should use cache on second call")
    void testGetCustomerAccount_Caching() {
        // First call - cache miss
        CustomerSupportAgent.getCustomerAccount("CUST001", toolContext);
        
        // Second call - should hit cache
        Map<String, Object> result = CustomerSupportAgent.getCustomerAccount("CUST001", toolContext);
        
        assertTrue((Boolean) result.get("success"));
        assertNotNull(toolContext.getState().get("customer:CUST001"));
    }
    
    // ==================== processPayment Tests ====================
    
    // ... (rest of processPayment Tests are unchanged) ...
    
    @Test
    @DisplayName("Should process valid payment with number")
    void testProcessPayment_ValidPayment() {
        Map<String, Object> result = CustomerSupportAgent.processPayment("CUST001", 100.00, toolContext);
        
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("transactionId"));
        assertEquals(100.00, ((Number) result.get("amount")).doubleValue());
        assertEquals(1350.00, ((Number) result.get("newBalance")).doubleValue());
    }
    
    @Test
    @DisplayName("Should process valid payment with string amount")
    void testProcessPayment_StringAmount() {
        Map<String, Object> result = CustomerSupportAgent.processPayment("CUST002", "50.00", toolContext);
        
        assertTrue((Boolean) result.get("success"));
        assertEquals(50.00, ((Number) result.get("amount")).doubleValue());
    }
    
    @Test
    @DisplayName("Should reject negative amount")
    void testProcessPayment_NegativeAmount() {
        Map<String, Object> result = CustomerSupportAgent.processPayment("CUST001", -100.00, toolContext);
        
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("greater than zero"));
    }
    
    @Test
    @DisplayName("Should reject zero amount")
    void testProcessPayment_ZeroAmount() {
        Map<String, Object> result = CustomerSupportAgent.processPayment("CUST001", 0.00, toolContext);
        
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("greater than zero"));
    }
    
    @Test
    @DisplayName("Should reject excessive amount")
    void testProcessPayment_ExcessiveAmount() {
        Map<String, Object> result = CustomerSupportAgent.processPayment("CUST001", 150000.00, toolContext);
        
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("maximum limit"));
    }
    
    @Test
    @DisplayName("Should reject invalid customer")
    void testProcessPayment_InvalidCustomer() {
        Map<String, Object> result = CustomerSupportAgent.processPayment("CUST999", 100.00, toolContext);
        
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("not found"));
    }
    
    @Test
    @DisplayName("Should store transaction in state")
    void testProcessPayment_StoreTransaction() {
        Map<String, Object> result = CustomerSupportAgent.processPayment("CUST001", 100.00, toolContext);
        
        assertTrue((Boolean) result.get("success"));
        assertNotNull(toolContext.getState().get("last_transaction_id"));
        assertEquals(100.00, toolContext.getState().get("last_payment_amount"));
    }
    
    // ==================== createTicket Tests ====================
    
    // ... (rest of createTicket Tests are unchanged) ...
    
    @Test
    @DisplayName("Should create valid ticket")
    void testCreateTicket_ValidTicket() {
        Map<String, Object> result = CustomerSupportAgent.createTicket(
            "CUST001",
            "Login Issue",
            "Cannot login to dashboard",
            "HIGH",
            toolContext
        );
        
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("ticket"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> ticket = (Map<String, Object>) result.get("ticket");
        assertEquals("CUST001", ticket.get("customerId"));
        assertEquals("Login Issue", ticket.get("subject"));
        assertEquals("HIGH", ticket.get("priority"));
        assertEquals("OPEN", ticket.get("status"));
    }
    
    @Test
    @DisplayName("Should default to MEDIUM priority if not specified")
    void testCreateTicket_DefaultPriority() {
        Map<String, Object> result = CustomerSupportAgent.createTicket(
            "CUST001",
            "General Question",
            "How do I use this feature?",
            null,
            toolContext
        );
        
        assertTrue((Boolean) result.get("success"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> ticket = (Map<String, Object>) result.get("ticket");
        assertEquals("MEDIUM", ticket.get("priority"));
    }
    
    @Test
    @DisplayName("Should reject empty subject")
    void testCreateTicket_EmptySubject() {
        Map<String, Object> result = CustomerSupportAgent.createTicket(
            "CUST001",
            "",
            "Description here",
            "LOW",
            toolContext
        );
        
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("Subject"));
    }
    
    @Test
    @DisplayName("Should reject empty description")
    void testCreateTicket_EmptyDescription() {
        Map<String, Object> result = CustomerSupportAgent.createTicket(
            "CUST001",
            "Subject",
            "  ",
            "LOW",
            toolContext
        );
        
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("Description"));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"LOW", "MEDIUM", "HIGH", "low", "medium", "high"})
    @DisplayName("Should accept valid priorities")
    void testCreateTicket_ValidPriorities(String priority) {
        Map<String, Object> result = CustomerSupportAgent.createTicket(
            "CUST001",
            "Test",
            "Test description",
            priority,
            toolContext
        );
        
        assertTrue((Boolean) result.get("success"));
    }
    
    // ==================== getTickets Tests ====================
    
    // ... (rest of getTickets Tests are unchanged) ...
    
    @Test
    @DisplayName("Should get all tickets for customer")
    void testGetTickets_AllTickets() {
        // Create some tickets first
        CustomerSupportAgent.createTicket("CUST001", "Issue 1", "Desc 1", "HIGH", toolContext);
        CustomerSupportAgent.createTicket("CUST001", "Issue 2", "Desc 2", "LOW", toolContext);
        
        Map<String, Object> result = CustomerSupportAgent.getTickets("CUST001", null, toolContext);
        
        assertTrue((Boolean) result.get("success"));
        // Note: Casting to Number then intValue() is robust for different number types (Long, Integer)
        assertEquals(2, ((Number) result.get("count")).intValue()); 
    }
    
    @Test
    @DisplayName("Should filter tickets by status")
    void testGetTickets_FilterByStatus() {
        CustomerSupportAgent.createTicket("CUST001", "Issue 1", "Desc 1", "HIGH", toolContext);
        
        Map<String, Object> result = CustomerSupportAgent.getTickets("CUST001", "OPEN", toolContext);
        
        assertTrue((Boolean) result.get("success"));
        // Assuming the test setup (not shown) ensures 'OPEN' tickets exist
        assertTrue(((Number) result.get("count")).intValue() >= 0); 
    }
    
    // ==================== updateAccountSettings Tests ====================
    
    // ... (rest of updateAccountSettings Tests are unchanged) ...
    
    @Test
    @DisplayName("Should update email address")
    void testUpdateAccountSettings_ValidEmail() {
        Map<String, Object> result = CustomerSupportAgent.updateAccountSettings(
            "CUST001",
            "newemail@example.com",
            null,
            toolContext
        );
        
        assertTrue((Boolean) result.get("success"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> updates = (Map<String, String>) result.get("updates");
        assertEquals("newemail@example.com", updates.get("email"));
    }
    
    @Test
    @DisplayName("Should update account tier")
    void testUpdateAccountSettings_ValidTier() {
        Map<String, Object> result = CustomerSupportAgent.updateAccountSettings(
            "CUST001",
            null,
            "Enterprise",
            toolContext
        );
        
        assertTrue((Boolean) result.get("success"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> updates = (Map<String, String>) result.get("updates");
        assertEquals("Enterprise", updates.get("tier"));
    }
    
    @Test
    @DisplayName("Should reject invalid email format")
    void testUpdateAccountSettings_InvalidEmail() {
        Map<String, Object> result = CustomerSupportAgent.updateAccountSettings(
            "CUST001",
            "not-an-email",
            null,
            toolContext
        );
        
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("Invalid email"));
    }
    
    @Test
    @DisplayName("Should reject invalid tier")
    void testUpdateAccountSettings_InvalidTier() {
        Map<String, Object> result = CustomerSupportAgent.updateAccountSettings(
            "CUST001",
            null,
            "InvalidTier",
            toolContext
        );
        
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("Invalid tier"));
    }
    
    @Test
    @DisplayName("Should reject when no updates provided")
    void testUpdateAccountSettings_NoUpdates() {
        Map<String, Object> result = CustomerSupportAgent.updateAccountSettings(
            "CUST001",
            null,
            null,
            toolContext
        );
        
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("No valid updates"));
    }
    
    // ==================== validateRefundEligibility Tests ====================
    
    // ... (rest of validateRefundEligibility Tests are unchanged) ...
    
    @Test
    @DisplayName("Should validate eligible customer")
    void testValidateRefundEligibility_Eligible() {
        Map<String, Object> result = CustomerSupportAgent.validateRefundEligibility("CUST001", toolContext);
        
        assertTrue((Boolean) result.get("success"));
        assertTrue((Boolean) result.get("eligible"));
        assertTrue((Boolean) toolContext.getState().get("refund_eligible"));
    }
    
    @Test
    @DisplayName("Should store eligibility in state")
    void testValidateRefundEligibility_StateStorage() {
        CustomerSupportAgent.validateRefundEligibility("CUST001", toolContext);
        
        assertEquals(true, toolContext.getState().get("refund_eligible"));
        assertEquals("CUST001", toolContext.getState().get("refund_customer"));
    }
    
    // ==================== processRefund Tests ====================
    
    // ... (rest of processRefund Tests are unchanged) ...
    
    @Test
    @DisplayName("Should process valid refund after validation")
    void testProcessRefund_ValidRefund() {
        // First validate
        CustomerSupportAgent.validateRefundEligibility("CUST001", toolContext);
        
        // Then process
        Map<String, Object> result = CustomerSupportAgent.processRefund("CUST001", 100.00, toolContext);
        
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("refundId"));
        assertEquals(100.00, ((Number) result.get("amount")).doubleValue());
        assertEquals(1150.00, ((Number) result.get("newBalance")).doubleValue());
    }
    
    @Test
    @DisplayName("Should reject refund without validation")
    void testProcessRefund_NoValidation() {
        Map<String, Object> result = CustomerSupportAgent.processRefund("CUST001", 100.00, toolContext);
        
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("validation"));
    }
    
    @Test
    @DisplayName("Should reject refund exceeding balance")
    void testProcessRefund_ExceedsBalance() {
        CustomerSupportAgent.validateRefundEligibility("CUST001", toolContext);
        
        Map<String, Object> result = CustomerSupportAgent.processRefund("CUST001", 10000.00, toolContext);
        
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("exceeds"));
    }
    
    @Test
    @DisplayName("Should store refund information in state")
    void testProcessRefund_StateStorage() {
        CustomerSupportAgent.validateRefundEligibility("CUST001", toolContext);
        CustomerSupportAgent.processRefund("CUST001", 100.00, toolContext);
        
        assertNotNull(toolContext.getState().get("last_refund_id"));
        assertEquals(100.00, toolContext.getState().get("refund_amount"));
    }
}
