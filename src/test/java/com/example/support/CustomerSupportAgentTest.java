package com.example.support;

import static org.junit.jupiter.api.Assertions.*;

import com.google.adk.agents.InvocationContext;
import com.google.adk.events.EventActions;
import com.google.adk.sessions.Session;
import com.google.adk.sessions.State;
import com.google.adk.tools.ToolContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

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

    // Mock the context hierarchy required by ToolContext
    state = new State(new ConcurrentHashMap<>());
    Session session = Mockito.mock(Session.class);
    Mockito.when(session.state()).thenReturn(state);

    InvocationContext invocationContext = Mockito.mock(InvocationContext.class);
    Mockito.when(invocationContext.session()).thenReturn(session);

    // Use the builder to construct the ToolContext
    toolContext =
        ToolContext.builder(invocationContext).actions(Mockito.mock(EventActions.class)).build();

    CustomerSupportAgent.resetMockData();
  }

  // ==================== getCustomerAccount Tests ====================

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
    Map<String, Object> result =
        agent.createTicket(
            "CUST001", "Login Issue", "Cannot login to dashboard", "HIGH", toolContext);

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
    Map<String, Object> result =
        agent.createTicket(
            "CUST001", "General Question", "How do I use this feature?", null, toolContext);

    assertTrue((Boolean) result.get("success"));

    @SuppressWarnings("unchecked")
    Map<String, Object> ticket = (Map<String, Object>) result.get("ticket");
    assertEquals("MEDIUM", ticket.get("priority"));
  }

  @Test
  @DisplayName("Should reject empty subject")
  void testCreateTicket_EmptySubject() {
    Map<String, Object> result =
        agent.createTicket("CUST001", "", "Description here", "LOW", toolContext);

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
    Map<String, Object> result =
        agent.createTicket("CUST001", "Test", "Test description", priority, toolContext);

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
    Map<String, Object> result =
        agent.updateAccountSettings("CUST001", "newemail@example.com", null, toolContext);

    assertTrue((Boolean) result.get("success"));

    @SuppressWarnings("unchecked")
    Map<String, String> updates = (Map<String, String>) result.get("updates");
    assertEquals("newemail@example.com", updates.get("email"));
  }

  @Test
  @DisplayName("Should update account tier")
  void testUpdateAccountSettings_ValidTier() {
    Map<String, Object> result =
        agent.updateAccountSettings("CUST001", null, "Enterprise", toolContext);

    assertTrue((Boolean) result.get("success"));

    @SuppressWarnings("unchecked")
    Map<String, String> updates = (Map<String, String>) result.get("updates");
    assertEquals("Enterprise", updates.get("tier"));
  }

  @Test
  @DisplayName("Should reject invalid email format")
  void testUpdateAccountSettings_InvalidEmail() {
    Map<String, Object> result =
        agent.updateAccountSettings("CUST001", "not-an-email", null, toolContext);

    assertFalse((Boolean) result.get("success"));
    assertTrue(result.get("error").toString().contains("Invalid email"));
  }

  @Test
  @DisplayName("Should reject invalid tier")
  void testUpdateAccountSettings_InvalidTier() {
    Map<String, Object> result =
        agent.updateAccountSettings("CUST001", null, "InvalidTier", toolContext);

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

  // ==================== processRefund Tests ====================

  @Test
  @DisplayName("Should reject refund without validation")
  void testProcessRefund_NoValidation() {
    Map<String, Object> result = agent.processRefund("CUST001", 100.00, toolContext);

    assertFalse((Boolean) result.get("success"));
  }

  @Test
  @DisplayName("Should reject refund for mismatched customer ID")
  void testProcessRefund_CustomerIdMismatch() {
    agent.validateRefundEligibility("CUST002", toolContext);

    Map<String, Object> result = agent.processRefund("CUST003", 50.00, toolContext);

    assertFalse((Boolean) result.get("success"));
  }
}
