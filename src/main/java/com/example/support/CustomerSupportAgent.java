package com.example.support;

import com.google.adk.tools.ToolContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Customer Support Agent Tool Implementation.
 * <p>
 * Contains the core business logic tools exposed to LLM agents — including
 * account queries, payments, ticket management, and refund workflows.
 * </p>
 *
 * @author Dar
 * @version 1.0.4
 */
@Component
public class CustomerSupportAgent {

  private static final Logger logger = LoggerFactory.getLogger(CustomerSupportAgent.class);
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final Map<String, Map<String, Object>> mockDatabase = new ConcurrentHashMap<>();
  private final Map<String, List<Map<String, Object>>> ticketDatabase = new ConcurrentHashMap<>();
  private final AtomicInteger ticketIdCounter = new AtomicInteger(1000);
  private Map<String, Map<String, Object>> initialMockData;

  @PostConstruct
  private void init() {
    initializeMockData();
  }

  // --------------------------- Initialization ---------------------------

  private void initializeMockData() {
    mockDatabase.clear();
    ticketDatabase.clear();
    ticketIdCounter.set(1000);

    mockDatabase.putAll(Map.of(
        "CUST001", customer("CUST001", "John Doe", "john.doe@acme.com",
            1250.00, "Premium", "Active", LocalDate.now().minusDays(45)),
        "CUST002", customer("CUST002", "Jane Smith", "jane.smith@acme.com",
            0.00, "Basic", "Active", LocalDate.now().minusDays(5)),
        "CUST003", customer("CUST003", "Bob Johnson", "bob.johnson@acme.com",
            5000.00, "Enterprise", "Active", LocalDate.now().minusDays(10))
    ));

    ticketDatabase.put("CUST001", new ArrayList<>(List.of(
        new HashMap<>(Map.of(
            "ticketId", "TICKET-1000",
            "customerId", "CUST001",
            "subject", "Login Issue",
            "description", "Cannot access dashboard",
            "priority", "HIGH",
            "status", "CLOSED",
            "createdAt", LocalDate.now().minusDays(7).format(DATE_FORMATTER)))
    )));

    initialMockData = deepCopy(mockDatabase);
    logger.info("Mock data initialized with {} customers", mockDatabase.size());
  }

  private Map<String, Object> customer(
      String id, String name, String email, double balance,
      String tier, String status, LocalDate lastPayment) {
    Map<String, Object> c = new HashMap<>();
    c.put("customerId", id);
    c.put("name", name);
    c.put("email", email);
    c.put("accountBalance", balance);
    c.put("tier", tier);
    c.put("accountStatus", status);
    c.put("lastPaymentDate", lastPayment.format(DATE_FORMATTER));
    return c;
  }

  public void resetMockData() {
    mockDatabase.clear();
    mockDatabase.putAll(deepCopy(initialMockData));
    ticketDatabase.clear();
    initializeMockData();
    logger.info("Mock databases reset.");
  }

  private Map<String, Map<String, Object>> deepCopy(Map<String, Map<String, Object>> original) {
    Map<String, Map<String, Object>> copy = new ConcurrentHashMap<>();
    original.forEach((k, v) -> copy.put(k, new HashMap<>(v)));
    return copy;
  }

  // --------------------------- Tool Methods ---------------------------

  @Schema(description = "Retrieve detailed customer account information")
  public Map<String, Object> getCustomerAccount(String customerId, ToolContext toolContext) {
    try {
      logger.info("Fetching account info for {}", customerId);
      customerId = ValidationUtils.validateCustomerId(customerId);

      String cacheKey = "customer:" + customerId;
      @SuppressWarnings("unchecked")
      Map<String, Object> cached = (Map<String, Object>) toolContext.state().get(cacheKey);
      if (cached != null) {
        logger.debug("Cache hit for {}", customerId);
        return cached;
      }

      Map<String, Object> customer = mockDatabase.get(customerId);
      if (customer == null) {
        return error("Customer not found: " + customerId);
      }

      Map<String, Object> result = Map.of("success", true, "customer", new HashMap<>(customer));
      toolContext.state().put(cacheKey, result);
      toolContext.state().put("current_customer", customerId);
      return result;
    } catch (Exception e) {
      logger.error("Error in getCustomerAccount", e);
      return error(e.getMessage());
    }
  }

  @Schema(description = "Process a customer payment")
  public Map<String, Object> processPayment(String customerId, Object amountObj, ToolContext ctx) {
    try {
      logger.info("Processing payment for {}", customerId);
      customerId = ValidationUtils.validateCustomerId(customerId);
      double amount = ValidationUtils.validateAmount(amountObj);

      Map<String, Object> customer = mockDatabase.get(customerId);
      if (customer == null) return error("Customer not found: " + customerId);

      String txnId = TransactionIdGenerator.generateTransactionId("TXN");
      double current = ((Number) customer.get("accountBalance")).doubleValue();
      double newBalance = round(current + amount);

      customer.put("accountBalance", newBalance);
      customer.put("lastPaymentDate", LocalDate.now().format(DATE_FORMATTER));

      ctx.state().put("last_transaction_id", txnId);
      ctx.state().put("last_payment_amount", amount);

      return Map.of(
          "success", true,
          "transactionId", txnId,
          "amount", amount,
          "previousBalance", current,
          "newBalance", newBalance,
          "message", "Payment processed successfully");
    } catch (Exception e) {
      logger.error("Error in processPayment", e);
      return error(e.getMessage());
    }
  }

  private static Map<String, Object> error(String message) {
    return Map.of("success", false, "error", message);
  }

  private static double round(double v) {
    return Math.round(v * 100.0) / 100.0;
  }
}
