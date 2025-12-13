package com.example.support;

import static org.junit.jupiter.api.Assertions.*;

import com.google.adk.sessions.State;
import com.google.adk.tools.ToolContext;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Comprehensive unit tests for CustomerSupportAgent tools
 *
 * @author Darshil
 * @version 1.0.3 (Fully Fixed - All tests enabled)
 */
@DisplayName("Customer Support Agent Test Suite")
class CustomerSupportAgentTest {

    private CustomerSupportAgent agent;
    private ToolContext toolContext;
    private State state;

    @BeforeEach
    void setUp() {
        agent = new CustomerSupportAgent();
        toolContext = new ToolContext();
        CustomerSupportAgent.resetMockData();
    }

    // ==================== getCustomerAccount Tests ====================

    @Test
    @DisplayName("Should retrieve valid customer account")
    void testGetCustomerAccount_ValidCustomer() {
        Map<String, Object> result = agent.getCustomerAccount("CUST001", toolContext);

        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("customer"));

        @SuppressWarnings("unchecked")
        Map<String, Object> customer = (Map<String, Object>) result.get("customer");
        assertEquals("CUST001", customer.get("customerId"));
        assertEquals("John Doe", customer.get("name"));
        assertEquals("Premium", customer.get("tier"));
        assertEquals(1250.00, ((Number) customer.get("accountBalance")).doubleValue());
    }

    @Test
    @DisplayName("Should handle invalid customer ID")
    void testGetCustomerAccount_InvalidCustomer() {
        Map<String, Object> result = agent.getCustomerAccount("CUST999", toolContext);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("not found"));
    }

    @Test
    @DisplayName("Should reject null customer ID")
    void testGetCustomerAccount_NullCustomerId() {
        Map<String, Object> result = agent.getCustomerAccount(null, toolContext);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("required"));
    }

    @Test
    @DisplayName("Should reject empty customer ID")
    void testGetCustomerAccount_EmptyCustomerId() {
        Map<String, Object> result = agent.getCustomerAccount("  ", toolContext);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("required"));
    }

    @Test
    @DisplayName("Should reject malformed customer ID")
    void testGetCustomerAccount_MalformedCustomerId() {
        Map<String, Object> result = agent.getCustomerAccount("INVALID123", toolContext);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("format"));
    }

    // ==================== processPayment Tests ====================

    @Test
    @DisplayName("Should process valid payment with number")
    void testProcessPayment_ValidPayment() {
        Map<String, Object> result = agent.processPayment("CUST001", 100.00, toolContext);

        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("transactionId"));
        assertEquals(100.00, ((Number) result.get("amount")).doubleValue());
        assertEquals(1350.00, ((Number) result.get("newBalance")).doubleValue());
    }

    @Test
    @DisplayName("Should process valid payment with string amount")
    void testProcessPayment_StringAmount() {
        Map<String, Object> result = agent.processPayment("CUST002", "50.00", toolContext);

        assertTrue((Boolean) result.get("success"));
        assertEquals(50.00, ((Number) result.get("amount")).doubleValue());
    }

    @Test
    @DisplayName("Should reject negative amount")
    void testProcessPayment_NegativeAmount() {
        Map<String, Object> result = agent.processPayment("CUST001", -100.00, toolContext);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("greater than zero"));
    }

    @Test
    @DisplayName("Should reject zero amount")
    void testProcessPayment_ZeroAmount() {
        Map<String, Object> result = agent.processPayment("CUST001", 0.00, toolContext);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("greater than zero"));
    }

    @Test
    @DisplayName("Should reject excessive amount")
    void testProcessPayment_ExcessiveAmount() {
        Map<String, Object> result = agent.processPayment("CUST001", 150000.00, toolContext);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("maximum"));
    }

    @Test
    @DisplayName("Should reject invalid customer")
    void testProcessPayment_InvalidCustomer() {
        Map<String, Object> result = agent.processPayment("CUST999", 100.00, toolContext);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("not found"));
    }

    // ==================== createTicket Tests ====================

    @Test
    @DisplayName("Should create valid ticket")
    void testCreateTicket_ValidTicket() {
        Map<String, Object> result = agent.createTicket("CUST001", "Login Issue", "Cannot login to dashboard", "HIGH", toolContext);

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
        Map<String, Object> result = agent.createTicket("CUST001", "General Question", "How do I use this feature?", null, toolContext);

        assertTrue((Boolean) result.get("success"));

        @SuppressWarnings("unchecked")
        Map<String, Object> ticket = (Map<String, Object>) result.get("ticket");
        assertEquals("MEDIUM", ticket.get("priority"));
    }

    @Test
    @DisplayName("Should reject empty subject")
    void testCreateTicket_EmptySubject() {
        Map<String, Object> result = agent.createTicket("CUST001", "", "Description here", "LOW", toolContext);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("Subject"));
    }

    @Test
    @DisplayName("Should reject empty description")
    void testCreateTicket_EmptyDescription() {
        Map<String, Object> result = agent.createTicket("CUST001", "Subject", "  ", "LOW", toolContext);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("Description"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"LOW", "MEDIUM", "HIGH", "low", "medium", "high"})
    @DisplayName("Should accept valid priorities")
    void testCreateTicket_ValidPriorities(String priority) {
        Map<String, Object> result = agent.createTicket("CUST001", "Test", "Test description", priority, toolContext);

        assertTrue((Boolean) result.get("success"));
    }

    // ==================== getTickets Tests ====================

    @Test
    @DisplayName("Should get all tickets for customer")
    void testGetTickets_AllTickets() {
        agent.createTicket("CUST001", "Issue 1", "Desc 1", "HIGH", toolContext);
        agent.createTicket("CUST001", "Issue 2", "Desc 2", "LOW", toolContext);

        Map<String, Object> result = agent.getTickets("CUST001", null, toolContext);

        assertTrue((Boolean) result.get("success"));
        assertEquals(3, ((Number) result.get("count")).intValue()); // 2 new + 1 existing
    }

    @Test
    @DisplayName("Should filter tickets by status")
    void testGetTickets_FilterByStatus() {
        agent.createTicket("CUST001", "Issue 1", "Desc 1", "HIGH", toolContext);

        Map<String, Object> result = agent.getTickets("CUST001", "OPEN", toolContext);

        assertTrue((Boolean) result.get("success"));
        assertTrue(((Number) result.get("count")).intValue() >= 1);
    }

    // ==================== updateAccountSettings Tests ====================

    @Test
    @DisplayName("Should update email address")
    void testUpdateAccountSettings_ValidEmail() {
        Map<String, Object> result = agent.updateAccountSettings("CUST001", "newemail@example.com", null, toolContext);

        assertTrue((Boolean) result.get("success"));

        @SuppressWarnings("unchecked")
        Map<String, String> updates = (Map<String, String>) result.get("updates");
        assertEquals("newemail@example.com", updates.get("email"));
    }

    @Test
    @DisplayName("Should update account tier")
    void testUpdateAccountSettings_ValidTier() {
        Map<String, Object> result = agent.updateAccountSettings("CUST001", null, "Enterprise", toolContext);

        assertTrue((Boolean) result.get("success"));

        @SuppressWarnings("unchecked")
        Map<String, String> updates = (Map<String, String>) result.get("updates");
        assertEquals("Enterprise", updates.get("tier"));
    }

    @Test
    @DisplayName("Should reject invalid email format")
    void testUpdateAccountSettings_InvalidEmail() {
        Map<String, Object> result = agent.updateAccountSettings("CUST001", "not-an-email", null, toolContext);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("Invalid email"));
    }

    @Test
    @DisplayName("Should reject invalid tier")
    void testUpdateAccountSettings_InvalidTier() {
        Map<String, Object> result = agent.updateAccountSettings("CUST001", null, "InvalidTier", toolContext);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("Invalid tier"));
    }

    @Test
    @DisplayName("Should reject when no updates provided")
    void testUpdateAccountSettings_NoUpdates() {
        Map<String, Object> result = agent.updateAccountSettings("CUST001", null, null, toolContext);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("No valid updates"));
    }

    // ==================== validateRefundEligibility Tests ====================

    @Test
    @DisplayName("Should validate eligible customer (CUST002)")
    void testValidateRefundEligibility_Eligible() {
        Map<String, Object> result = agent.validateRefundEligibility("CUST002", toolContext);

        assertTrue((Boolean) result.get("success"));
        assertTrue((Boolean) result.get("eligible"));
    }

    @Test
    @DisplayName("Should validate ineligible customer (CUST001)")
    void testValidateRefundEligibility_NotEligible() {
        Map<String, Object> result = agent.validateRefundEligibility("CUST001", toolContext);

        assertTrue((Boolean) result.get("success"));
        assertFalse((Boolean) result.get("eligible"));
    }

    // ==================== processRefund Tests ====================

    @Test
    @DisplayName("Should process valid refund after validation (CUST003)")
    void testProcessRefund_ValidRefund() {
        agent.validateRefundEligibility("CUST003", toolContext);

        Map<String, Object> result = agent.processRefund("CUST003", 100.00, toolContext);

        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("refundId"));
        assertEquals(100.00, ((Number) result.get("amount")).doubleValue());
        assertEquals(4900.00, ((Number) result.get("newBalance")).doubleValue());
    }

    @Test
    @DisplayName("Should reject refund without validation")
    void testProcessRefund_NoValidation() {
        Map<String, Object> result = agent.processRefund("CUST001", 100.00, toolContext);

        assertFalse((Boolean) result.get("success"));
    }

    @Test
    @DisplayName("Should reject refund exceeding balance")
    void testProcessRefund_ExceedsBalance() {
        agent.validateRefundEligibility("CUST002", toolContext);

        Map<String, Object> result = agent.processRefund("CUST002", 100.00, toolContext);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error").toString().contains("exceeds"));
    }

    @Test
    @DisplayName("Should reject refund for mismatched customer ID")
    void testProcessRefund_CustomerIdMismatch() {
        agent.validateRefundEligibility("CUST002", toolContext);

        Map<String, Object> result = agent.processRefund("CUST003", 50.00, toolContext);

        assertFalse((Boolean) result.get("success"));
    }
}
