package com.example.support;

import static org.junit.jupiter.api.Assertions.*;

import com.google.adk.tools.ToolContext;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CustomerSupportAgentTest {

  private CustomerSupportAgent customerSupportAgent;

  @Mock private ToolContext toolContext;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    customerSupportAgent = new CustomerSupportAgent();
  }

  @Test
  public void testGetCustomerAccount() {
    Map<String, Object> result = customerSupportAgent.getCustomerAccount("123", toolContext);
    assertEquals("123", result.get("customerId"));
    assertEquals(1250.00, result.get("balance"));
  }

  @Test
  public void testGetCustomerAccount_nullId() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          customerSupportAgent.getCustomerAccount(null, toolContext);
        });
  }

  @Test
  public void testProcessPayment() {
    Map<String, Object> result = customerSupportAgent.processPayment("123", 100.00, toolContext);
    assertTrue(result.containsKey("transactionId"));
    assertEquals(1150.00, result.get("newBalance"));
  }

  @Test
  public void testProcessPayment_invalidAmount() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          customerSupportAgent.processPayment("123", -100.00, toolContext);
        });
  }

  @Test
  public void testCreateTicket() {
    Map<String, Object> result =
        customerSupportAgent.createTicket("123", "Test issue", toolContext);
    assertTrue(result.containsKey("ticketId"));
    assertEquals("OPEN", result.get("status"));
  }

  @Test
  public void testCreateTicket_nullIssue() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          customerSupportAgent.createTicket("123", null, toolContext);
        });
  }

  @Test
  public void testGetTickets() {
    Map<String, Object> result = customerSupportAgent.getTickets("123", "OPEN", toolContext);
    assertTrue(result.containsKey("tickets"));
  }

  @Test
  public void testGetTickets_nullCustomerId() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          customerSupportAgent.getTickets(null, "OPEN", toolContext);
        });
  }

  @Test
  public void testUpdateAccountSettings() {
    Map<String, String> settings = new HashMap<>();
    settings.put("theme", "dark");
    Map<String, Object> result =
        customerSupportAgent.updateAccountSettings("123", settings, toolContext);
    assertEquals("success", result.get("status"));
  }

  @Test
  public void testUpdateAccountSettings_nullSettings() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          customerSupportAgent.updateAccountSettings("123", null, toolContext);
        });
  }

  @Test
  public void testValidateRefundEligibility() {
    customerSupportAgent.getCustomerAccount("123", toolContext);
    Map<String, Object> result = customerSupportAgent.validateRefundEligibility("123", toolContext);
    assertTrue((Boolean) result.get("isEligible"));
  }

  @Test
  public void testValidateRefundEligibility_notEligible() {
    Map<String, Object> result = customerSupportAgent.validateRefundEligibility("456", toolContext);
    assertFalse((Boolean) result.get("isEligible"));
  }

  @Test
  public void testProcessRefund() {
    Map<String, Object> result = customerSupportAgent.processRefund("123", 50.00, toolContext);
    assertTrue(result.containsKey("refundId"));
    assertEquals("PROCESSED", result.get("status"));
  }

  @Test
  public void testProcessRefund_invalidAmount() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          customerSupportAgent.processRefund("123", -50.00, toolContext);
        });
  }
}
