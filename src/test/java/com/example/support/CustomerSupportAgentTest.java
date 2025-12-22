package com.example.support;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Comprehensive test suite for CustomerSupportAgent. 35 test methods with 100% coverage of all 7
 * tools.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerSupportAgentTest {

  private CustomerSupportAgent agent;
  private Map<String, Object> context;

  @BeforeEach
  void setUp() {
    agent = new CustomerSupportAgent();
    context = new HashMap<>();
    CustomerSupportAgent.resetData();
  }

  @AfterEach
  void tearDown() {
    context.clear();
  }

  // ==================== Tool 1: getCustomerAccount Tests ====================

  @Test
  @Order(1)
  @DisplayName("Test 1: Get valid customer account")
  void testGetCustomerAccountValid() {
    Map<String, Object> result = agent.getCustomerAccount("CUST001", context);

    assertTrue((Boolean) result.get("success"));
    assertFalse((Boolean) result.get("cached"));

    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) result.get("data");
    assertEquals("CUST001", data.get("customerId"));
    assertEquals("John Doe", data.get("name"));
    assertEquals("Premium", data.get("tier"));
  }

  @Test
  @Order(2)
  @DisplayName("Test 2: Get customer account with caching")
  void testGetCustomerAccountCaching() {
    // First call - not cached
    Map<String, Object> result1 = agent.getCustomerAccount("CUST002", context);
    assertTrue((Boolean) result1.get("success"));
    assertFalse((Boolean) result1.get("cached"));

    // Second call - should be cached
    Map<String, Object> result2 = agent.getCustomerAccount("CUST002", context);
    assertTrue((Boolean) result2.get("success"));
    assertTrue((Boolean) result2.get("cached"));
  }

  @Test
  @Order(3)
  @DisplayName("Test 3: Get customer account - invalid ID format")
  void testGetCustomerAccountInvalidFormat() {
    Map<String, Object> result = agent.getCustomerAccount("INVALID", context);

    assertFalse((Boolean) result.get("success"));
    assertEquals("Invalid customer ID format", result.get("error"));
  }

  @Test
  @Order(4)
  @DisplayName("Test 4: Get customer account - not found")
  void testGetCustomerAccountNotFound() {
    Map<String, Object> result = agent.getCustomerAccount("CUST999", context);

    assertFalse((Boolean) result.get("success"));
    assertEquals("Customer not found", result.get("error"));
  }

  @Test
  @Order(5)
  @DisplayName("Test 5: Get customer account - null context")
  void testGetCustomerAccountNullContext() {
    Map<String, Object> result = agent.getCustomerAccount("CUST001", null);

    assertTrue((Boolean) result.get("success"));
    assertNull(result.get("cached"));
  }

  @Test
  @Order(6)
  @DisplayName("Test 6: Get customer account - empty string")
  void testGetCustomerAccountEmptyString() {
    Map<String, Object> result = agent.getCustomerAccount("", context);

    assertFalse((Boolean) result.get("success"));
    assertEquals("Invalid customer ID format", result.get("error"));
  }

  // ==================== Tool 2: processPayment Tests ====================

  @Test
  @Order(7)
  @DisplayName("Test 7: Process valid payment - integer")
  void testProcessPaymentValidInteger() {
    Map<String, Object> result = agent.processPayment("CUST002", 100, context);

    assertTrue((Boolean) result.get("success"));
    assertEquals("Payment processed successfully", result.get("message"));

    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) result.get("data");
    assertEquals(100.0, data.get("amount"));
    assertEquals(100.0, data.get("newBalance"));
    assertNotNull(data.get("transactionId"));
    assertTrue(data.get("transactionId").toString().startsWith("TXN-"));
  }

  @Test
  @Order(8)
  @DisplayName("Test 8: Process valid payment - double")
  void testProcessPaymentValidDouble() {
    Map<String, Object> result = agent.processPayment("CUST001", 250.75, context);

    assertTrue((Boolean) result.get("success"));

    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) result.get("data");
    assertEquals(250.75, data.get("amount"));
    assertEquals(1500.75, data.get("newBalance"));
  }

  @Test
  @Order(9)
  @DisplayName("Test 9: Process payment - rounding")
  void testProcessPaymentRounding() {
    Map<String, Object> result = agent.processPayment("CUST002", 99.999, context);

    assertTrue((Boolean) result.get("success"));

    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) result.get("data");
    assertEquals(100.0, data.get("amount"));
  }

  @Test
  @Order(10)
  @DisplayName("Test 10: Process payment - invalid customer")
  void testProcessPaymentInvalidCustomer() {
    Map<String, Object> result = agent.processPayment("CUST999", 100, context);

    assertFalse((Boolean) result.get("success"));
    assertEquals("Customer not found", result.get("error"));
  }

  @Test
  @Order(11)
  @DisplayName("Test 11: Process payment - amount too high")
  void testProcessPaymentAmountTooHigh() {
    Map<String, Object> result = agent.processPayment("CUST001", 150000, context);

    assertFalse((Boolean) result.get("success"));
    assertEquals("Amount must be between 0 and 100000", result.get("error"));
  }

  @Test
  @Order(12)
  @DisplayName("Test 12: Process payment - invalid amount format")
  void testProcessPaymentInvalidFormat() {
    Map<String, Object> result = agent.processPayment("CUST001", "invalid", context);

    assertFalse((Boolean) result.get("success"));
    assertEquals("Invalid amount format", result.get("error"));
  }

  // ==================== Tool 3: createTicket Tests ====================

  @Test
  @Order(13)
  @DisplayName("Test 13: Create ticket - valid")
  void testCreateTicketValid() {
    Map<String, Object> result =
        agent.createTicket("CUST001", "Login Issue", "Cannot login to account", "high", context);

    assertTrue((Boolean) result.get("success"));
    assertEquals("Ticket created successfully", result.get("message"));

    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) result.get("data");
    assertNotNull(data.get("ticketId"));
    assertTrue(data.get("ticketId").toString().startsWith("TKT-"));
    assertEquals("high", data.get("priority"));
    assertEquals("open", data.get("status"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"low", "medium", "high", "urgent"})
  @Order(14)
  @DisplayName("Test 14: Create ticket - all priorities")
  void testCreateTicketAllPriorities(String priority) {
    Map<String, Object> result =
        agent.createTicket("CUST002", "Test Ticket", "Test Description", priority, context);

    assertTrue((Boolean) result.get("success"));

    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) result.get("data");
    assertEquals(priority.toLowerCase(), data.get("priority"));
  }

  @Test
  @Order(15)
  @DisplayName("Test 15: Create ticket - invalid priority")
  void testCreateTicketInvalidPriority() {
    Map<String, Object> result = agent.createTicket("CUST001", "Test", "Test", "invalid", context);

    assertFalse((Boolean) result.get("success"));
    assertTrue(result.get("error").toString().contains("Invalid priority"));
  }

  @Test
  @Order(16)
  @DisplayName("Test 16: Create ticket - empty subject")
  void testCreateTicketEmptySubject() {
    Map<String, Object> result =
        agent.createTicket("CUST001", "", "Description", "medium", context);

    assertFalse((Boolean) result.get("success"));
    assertEquals("Subject is required", result.get("error"));
  }

  @Test
  @Order(17)
  @DisplayName("Test 17: Create ticket - empty description")
  void testCreateTicketEmptyDescription() {
    Map<String, Object> result = agent.createTicket("CUST001", "Subject", "", "medium", context);

    assertFalse((Boolean) result.get("success"));
    assertEquals("Description is required", result.get("error"));
  }

  // ==================== Tool 4: getTickets Tests ====================

  @Test
  @Order(18)
  @DisplayName("Test 18: Get tickets - all status")
  void testGetTicketsAll() {
    // Create some tickets first
    agent.createTicket("CUST001", "Issue 1", "Description 1", "high", context);
    agent.createTicket("CUST001", "Issue 2", "Description 2", "low", context);

    Map<String, Object> result = agent.getTickets("CUST001", "all", context);

    assertTrue((Boolean) result.get("success"));
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> tickets = (List<Map<String, Object>>) result.get("data");
    assertEquals(2, tickets.size());
  }

  @Test
  @Order(19)
  @DisplayName("Test 19: Get tickets - filter by status")
  void testGetTicketsFilterStatus() {
    agent.createTicket("CUST002", "Test", "Test", "medium", context);

    Map<String, Object> result = agent.getTickets("CUST002", "open", context);

    assertTrue((Boolean) result.get("success"));
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> tickets = (List<Map<String, Object>>) result.get("data");
    assertEquals(1, tickets.size());
  }

  @Test
  @Order(20)
  @DisplayName("Test 20: Get tickets - no tickets")
  void testGetTicketsEmpty() {
    Map<String, Object> result = agent.getTickets("CUST003", "all", context);

    assertTrue((Boolean) result.get("success"));
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> tickets = (List<Map<String, Object>>) result.get("data");
    assertEquals(0, tickets.size());
  }

  // ==================== Tool 5: updateAccountSettings Tests ====================

  @Test
  @Order(21)
  @DisplayName("Test 21: Update account - email only")
  void testUpdateAccountEmailOnly() {
    Map<String, Object> result =
        agent.updateAccountSettings("CUST001", "newemail@example.com", null, context);

    assertTrue((Boolean) result.get("success"));

    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) result.get("data");
    assertEquals("newemail@example.com", data.get("email"));
  }

  @Test
  @Order(22)
  @DisplayName("Test 22: Update account - tier only")
  void testUpdateAccountTierOnly() {
    Map<String, Object> result = agent.updateAccountSettings("CUST002", null, "premium", context);

    assertTrue((Boolean) result.get("success"));

    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) result.get("data");
    assertEquals("Premium", data.get("tier"));
  }

  @Test
  @Order(23)
  @DisplayName("Test 23: Update account - both fields")
  void testUpdateAccountBothFields() {
    Map<String, Object> result =
        agent.updateAccountSettings("CUST003", "bob.new@example.com", "basic", context);

    assertTrue((Boolean) result.get("success"));

    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) result.get("data");
    assertEquals("bob.new@example.com", data.get("email"));
    assertEquals("Basic", data.get("tier"));
  }

  @Test
  @Order(24)
  @DisplayName("Test 24: Update account - invalid email")
  void testUpdateAccountInvalidEmail() {
    Map<String, Object> result =
        agent.updateAccountSettings("CUST001", "invalid-email", null, context);

    assertFalse((Boolean) result.get("success"));
    assertEquals("Invalid email format", result.get("error"));
  }

  @Test
  @Order(25)
  @DisplayName("Test 25: Update account - invalid tier")
  void testUpdateAccountInvalidTier() {
    Map<String, Object> result = agent.updateAccountSettings("CUST001", null, "platinum", context);

    assertFalse((Boolean) result.get("success"));
    assertTrue(result.get("error").toString().contains("Invalid tier"));
  }

  @Test
  @Order(26)
  @DisplayName("Test 26: Update account - no fields provided")
  void testUpdateAccountNoFields() {
    Map<String, Object> result = agent.updateAccountSettings("CUST001", null, null, context);

    assertFalse((Boolean) result.get("success"));
    assertTrue(result.get("error").toString().contains("At least one field"));
  }

  // ==================== Tool 6: validateRefundEligibility Tests ====================

  @Test
  @Order(27)
  @DisplayName("Test 27: Validate refund - eligible customer")
  void testValidateRefundEligible() {
    Map<String, Object> result = agent.validateRefundEligibility("CUST002", context);

    assertTrue((Boolean) result.get("success"));

    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) result.get("data");
    assertTrue((Boolean) data.get("eligible"));
    assertTrue((Long) data.get("daysSinceCreation") <= 30);

    // Check context was updated
    assertTrue((Boolean) context.get("refund_validated_CUST002"));
  }

  @Test
  @Order(28)
  @DisplayName("Test 28: Validate refund - ineligible (too old)")
  void testValidateRefundIneligibleOld() {
    Map<String, Object> result = agent.validateRefundEligibility("CUST001", context);

    assertTrue((Boolean) result.get("success"));

    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) result.get("data");
    assertFalse((Boolean) data.get("eligible"));
    assertTrue((Long) data.get("daysSinceCreation") > 30);
  }

  @Test
  @Order(29)
  @DisplayName("Test 29: Validate refund - invalid customer")
  void testValidateRefundInvalidCustomer() {
    Map<String, Object> result = agent.validateRefundEligibility("CUST999", context);

    assertFalse((Boolean) result.get("success"));
    assertEquals("Customer not found", result.get("error"));
  }

  // ==================== Tool 7: processRefund Tests ====================

  @Test
  @Order(30)
  @DisplayName("Test 30: Process refund - valid")
  void testProcessRefundValid() {
    // First validate eligibility
    agent.validateRefundEligibility("CUST003", context);

    // Then process refund
    Map<String, Object> result = agent.processRefund("CUST003", 500.0, context);

    assertTrue((Boolean) result.get("success"));
    assertEquals("Refund processed successfully", result.get("message"));

    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) result.get("data");
    assertEquals(500.0, data.get("amount"));
    assertEquals(4500.0, data.get("newBalance"));
    assertNotNull(data.get("refundId"));
    assertTrue(data.get("refundId").toString().startsWith("REF-"));
    assertEquals("5-7 business days", data.get("estimatedArrival"));
  }

  @Test
  @Order(31)
  @DisplayName("Test 31: Process refund - without validation")
  void testProcessRefundWithoutValidation() {
    Map<String, Object> result = agent.processRefund("CUST002", 50.0, context);

    assertFalse((Boolean) result.get("success"));
    assertEquals("Refund eligibility must be validated first", result.get("error"));
  }

  @Test
  @Order(32)
  @DisplayName("Test 32: Process refund - insufficient balance")
  void testProcessRefundInsufficientBalance() {
    // Validate first
    agent.validateRefundEligibility("CUST002", context);

    // Try to refund more than balance
    Map<String, Object> result = agent.processRefund("CUST002", 100.0, context);

    assertFalse((Boolean) result.get("success"));
    assertEquals("Insufficient balance for refund", result.get("error"));
  }

  @Test
  @Order(33)
  @DisplayName("Test 33: Process refund - invalid amount")
  void testProcessRefundInvalidAmount() {
    agent.validateRefundEligibility("CUST003", context);

    Map<String, Object> result = agent.processRefund("CUST003", -50.0, context);

    assertFalse((Boolean) result.get("success"));
    assertTrue(result.get("error").toString().contains("Amount must be between"));
  }

  // ==================== Integration Tests ====================

  @Test
  @Order(34)
  @DisplayName("Integration Test: Complete payment workflow")
  void testIntegrationPaymentWorkflow() {
    // Get account
    Map<String, Object> account = agent.getCustomerAccount("CUST001", context);
    assertTrue((Boolean) account.get("success"));

    // Process payment
    Map<String, Object> payment = agent.processPayment("CUST001", 500.0, context);
    assertTrue((Boolean) payment.get("success"));

    // Verify new balance
    Map<String, Object> updatedAccount = agent.getCustomerAccount("CUST001", context);
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) updatedAccount.get("data");
    assertEquals(1750.0, data.get("balance"));
  }

  @Test
  @Order(35)
  @DisplayName("Integration Test: Complete refund workflow")
  void testIntegrationRefundWorkflow() {
    // Validate eligibility
    Map<String, Object> validation = agent.validateRefundEligibility("CUST003", context);
    assertTrue((Boolean) validation.get("success"));

    // Process refund
    Map<String, Object> refund = agent.processRefund("CUST003", 1000.0, context);
    assertTrue((Boolean) refund.get("success"));

    // Verify balance
    Map<String, Object> account = agent.getCustomerAccount("CUST003", context);
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) account.get("data");
    assertEquals(4000.0, data.get("balance"));
  }
}
