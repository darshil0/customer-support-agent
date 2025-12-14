package com.example.support;

import com.google.adk.tools.ToolContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Customer Support Agent Tool Implementation.
 *
 * <p>Contains the core business logic tools exposed to LLM agents — including account queries,
 * payments, ticket management, and refund workflows.
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

  public CustomerSupportAgent() {
    initializeMockData();
  }

  @PostConstruct
  private void init() {
    initializeMockData();
  }

  // --------------------------- Initialization ---------------------------

  private void initializeMockData() {
    mockDatabase.clear();
    ticketDatabase.clear();
    ticketIdCounter.set(1000);

    mockDatabase.putAll(
        Map.of(
            "CUST001",
                customer(
                    "CUST001",
                    "John Doe",
                    "john.doe@acme.com",
                    1250.00,
                    "Premium",
                    "Active",
                    LocalDate.now().minusDays(45)),
            "CUST002",
                customer(
                    "CUST002",
                    "Jane Smith",
                    "jane.smith@acme.com",
                    0.00,
                    "Basic",
                    "Active",
                    LocalDate.now().minusDays(5)),
            "CUST003",
                customer(
                    "CUST003",
                    "Bob Johnson",
                    "bob.johnson@acme.com",
                    5000.00,
                    "Enterprise",
                    "Active",
                    LocalDate.now().minusDays(10))));

    ticketDatabase.put(
        "CUST001",
        new ArrayList<>(
            List.of(
                new HashMap<>(
                    Map.of(
                        "ticketId", "TICKET-1000",
                        "customerId", "CUST001",
                        "subject", "Login Issue",
                        "description", "Cannot access dashboard",
                        "priority", "HIGH",
                        "status", "CLOSED",
                        "createdAt", LocalDate.now().minusDays(7).format(DATE_FORMATTER))))));

    initialMockData = deepCopy(mockDatabase);
    logger.info("Mock data initialized with {} customers", mockDatabase.size());
  }

  private Map<String, Object> customer(
      String id,
      String name,
      String email,
      double balance,
      String tier,
      String status,
      LocalDate lastPayment) {
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
    ticketDatabase.put(
        "CUST001",
        new ArrayList<>(
            List.of(
                new HashMap<>(
                    Map.of(
                        "ticketId", "TICKET-1000",
                        "customerId", "CUST001",
                        "subject", "Login Issue",
                        "description", "Cannot access dashboard",
                        "priority", "HIGH",
                        "status", "CLOSED",
                        "createdAt", LocalDate.now().minusDays(7).format(DATE_FORMATTER))))));
    ticketIdCounter.set(1000);
    logger.info("Mock databases reset.");
  }

  private Map<String, Map<String, Object>> deepCopy(Map<String, Map<String, Object>> original) {
    Map<String, Map<String, Object>> copy = new ConcurrentHashMap<>();
    original.forEach((k, v) -> copy.put(k, new HashMap<>(v)));
    return copy;
  }

  private static Map<String, Object> error(String message) {
    return Map.of("success", false, "error", message);
  }

  private static double round(double v) {
    return Math.round(v * 100.0) / 100.0;
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

  @Schema(description = "Create a new support ticket for a customer")
  public Map<String, Object> createTicket(
      String customerId,
      String subject,
      String description,
      String priority,
      ToolContext toolContext) {
    try {
      logger.info("Creating ticket for {}", customerId);
      customerId = ValidationUtils.validateCustomerId(customerId);
      if (subject == null || subject.trim().isEmpty()) {
        return error("Subject is required");
      }
      if (description == null || description.trim().isEmpty()) {
        return error("Description is required");
      }
      String finalPriority =
          (priority == null || priority.isBlank()) ? "MEDIUM" : priority.trim().toUpperCase();

      Map<String, Object> ticket = new HashMap<>();
      String ticketId = "TICKET-" + ticketIdCounter.incrementAndGet();
      ticket.put("ticketId", ticketId);
      ticket.put("customerId", customerId);
      ticket.put("subject", subject);
      ticket.put("description", description);
      ticket.put("priority", finalPriority);
      ticket.put("status", "OPEN");
      ticket.put("createdAt", LocalDate.now().format(DATE_FORMATTER));

      ticketDatabase.computeIfAbsent(customerId, k -> new ArrayList<>()).add(ticket);

      toolContext.state().put("last_ticket_id", ticketId);
      return Map.of("success", true, "ticket", ticket);
    } catch (Exception e) {
      logger.error("Error in createTicket", e);
      return error(e.getMessage());
    }
  }

  @Schema(
      description =
          "Retrieve a list of support tickets for a customer, with an optional status filter")
  public Map<String, Object> getTickets(String customerId, String status, ToolContext toolContext) {
    try {
      logger.info("Fetching tickets for {}", customerId);
      customerId = ValidationUtils.validateCustomerId(customerId);

      List<Map<String, Object>> tickets = ticketDatabase.getOrDefault(customerId, List.of());
      List<Map<String, Object>> filteredTickets =
          (status == null || status.isBlank())
              ? tickets
              : tickets.stream()
                  .filter(t -> status.equalsIgnoreCase((String) t.get("status")))
                  .toList();

      return Map.of("success", true, "count", filteredTickets.size(), "tickets", filteredTickets);
    } catch (Exception e) {
      logger.error("Error in getTickets", e);
      return error(e.getMessage());
    }
  }

  @Schema(description = "Update customer account settings, such as email or account tier")
  public Map<String, Object> updateAccountSettings(
      String customerId, String email, String tier, ToolContext toolContext) {
    try {
      logger.info("Updating settings for {}", customerId);
      customerId = ValidationUtils.validateCustomerId(customerId);
      Map<String, Object> customer = mockDatabase.get(customerId);
      if (customer == null) {
        return error("Customer not found: " + customerId);
      }

      Map<String, String> updates = new HashMap<>();
      if (email != null && !email.isBlank()) {
        String validatedEmail = ValidationUtils.validateEmail(email);
        customer.put("email", validatedEmail);
        updates.put("email", validatedEmail);
      }
      if (tier != null && !tier.isBlank()) {
        String validatedTier = tier.substring(0, 1).toUpperCase() + tier.substring(1).toLowerCase();
        customer.put("tier", validatedTier);
        updates.put("tier", validatedTier);
      }

      if (updates.isEmpty()) {
        return error("No valid updates provided");
      }

      return Map.of("success", true, "customerId", customerId, "updates", updates);
    } catch (Exception e) {
      logger.error("Error in updateAccountSettings", e);
      return error(e.getMessage());
    }
  }

  @Schema(
      description =
          "Validate if a customer is eligible for a refund based on account status and last payment date")
  public Map<String, Object> validateRefundEligibility(String customerId, ToolContext toolContext) {
    try {
      logger.info("Validating refund eligibility for {}", customerId);
      customerId = ValidationUtils.validateCustomerId(customerId);
      Map<String, Object> customer = mockDatabase.get(customerId);
      if (customer == null) {
        return error("Customer not found: " + customerId);
      }

      List<String> reasons = new ArrayList<>();
      boolean isEligible = true;

      if (!"Active".equals(customer.get("accountStatus"))) {
        isEligible = false;
        reasons.add("Account is not active");
      }

      LocalDate lastPayment = LocalDate.parse((String) customer.get("lastPaymentDate"));
      if (lastPayment.isBefore(LocalDate.now().minusDays(30))) {
        isEligible = false;
        reasons.add("Last payment was more than 30 days ago");
      }

      toolContext.state().put("refund_eligible", isEligible);
      toolContext.state().put("refund_customer", customerId);

      return Map.of("success", true, "eligible", isEligible, "reasons", reasons);
    } catch (Exception e) {
      logger.error("Error in validateRefundEligibility", e);
      return error(e.getMessage());
    }
  }

  @Schema(description = "Process a refund for a customer, requires prior eligibility validation")
  public Map<String, Object> processRefund(String customerId, Object amountObj, ToolContext ctx) {
    try {
      logger.info("Processing refund for {}", customerId);
      customerId = ValidationUtils.validateCustomerId(customerId);
      double amount = ValidationUtils.validateAmount(amountObj);

      Boolean eligible = (Boolean) ctx.state().get("refund_eligible");
      String validatedCustomer = (String) ctx.state().get("refund_customer");

      if (eligible == null || !eligible || !customerId.equals(validatedCustomer)) {
        return error("Refund validation failed or customer mismatch. Please run validation first.");
      }

      Map<String, Object> customer = mockDatabase.get(customerId);
      double current = ((Number) customer.get("accountBalance")).doubleValue();

      if (amount > current) {
        return error("Refund amount exceeds current balance of $" + current);
      }

      double newBalance = round(current - amount);
      customer.put("accountBalance", newBalance);

      // Clear state after successful refund
      ctx.state().remove("refund_eligible");
      ctx.state().remove("refund_customer");

      String refundId = TransactionIdGenerator.generateTransactionId("REF");
      return Map.of(
          "success", true,
          "refundId", refundId,
          "amount", amount,
          "previousBalance", current,
          "newBalance", newBalance);
    } catch (Exception e) {
      logger.error("Error in processRefund", e);
      return error(e.getMessage());
    }
  }
}
