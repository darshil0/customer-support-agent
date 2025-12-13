package com.example.support;

import com.google.adk.tools.ToolContext;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.springframework.stereotype.Component;

/**
 * Customer Support Agent - Tool Implementation Contains all business logic methods exposed as tools
 * to LLM agents.
 *
 * @author Darshil
 * @version 1.0.2 (Fixed)
 */
@Component
public class CustomerSupportAgent {

  private static final Logger logger = Logger.getLogger(CustomerSupportAgent.class.getName());
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  // Mock databases
  private static Map<String, Map<String, Object>> mockDatabase = new ConcurrentHashMap<>();
  private static Map<String, List<Map<String, Object>>> ticketDatabase = new ConcurrentHashMap<>();
  private static AtomicInteger ticketIdCounter = new AtomicInteger(1000);

  // Store initial state for test resetting
  private static Map<String, Map<String, Object>> initialMockData;

  static {
    initializeMockData();
  }

  /** Initialize mock customer data */
  private static void initializeMockData() {
    mockDatabase.clear();
    ticketDatabase.clear();
    ticketIdCounter.set(1000);

    // CUST001: Premium, Large Balance, Ineligible for refund (old payment)
    Map<String, Object> customer1 = new HashMap<>();
    customer1.put("customerId", "CUST001");
    customer1.put("name", "John Doe");
    customer1.put("email", "john.doe@acme.com");
    customer1.put("accountBalance", 1250.00);
    customer1.put("tier", "Premium");
    customer1.put("accountStatus", "Active");
    customer1.put("lastPaymentDate", LocalDate.now().minusDays(45).format(DATE_FORMATTER));
    mockDatabase.put("CUST001", customer1);

    // CUST002: Basic, No Balance, Eligible for refund (recent payment)
    Map<String, Object> customer2 = new HashMap<>();
    customer2.put("customerId", "CUST002");
    customer2.put("name", "Jane Smith");
    customer2.put("email", "jane.smith@acme.com");
    customer2.put("accountBalance", 0.00);
    customer2.put("tier", "Basic");
    customer2.put("accountStatus", "Active");
    customer2.put("lastPaymentDate", LocalDate.now().minusDays(5).format(DATE_FORMATTER));
    mockDatabase.put("CUST002", customer2);

    // CUST003: Enterprise, Large Balance, Eligible for refund (recent payment)
    Map<String, Object> customer3 = new HashMap<>();
    customer3.put("customerId", "CUST003");
    customer3.put("name", "Bob Johnson");
    customer3.put("email", "bob.johnson@acme.com");
    customer3.put("accountBalance", 5000.00);
    customer3.put("tier", "Enterprise");
    customer3.put("accountStatus", "Active");
    customer3.put("lastPaymentDate", LocalDate.now().minusDays(10).format(DATE_FORMATTER));
    mockDatabase.put("CUST003", customer3);

    // Initialize with one existing ticket
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
    logger.info("Mock database initialized with " + mockDatabase.size() + " customers");
  }

  /** Reset mock data for testing */
  public static void resetMockData() {
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
    ticketIdCounter.set(1001);
    logger.info("Mock database reset for testing");
  }

  /** Deep copy helper for mock data */
  private static Map<String, Map<String, Object>> deepCopy(
      Map<String, Map<String, Object>> original) {
    Map<String, Map<String, Object>> copy = new ConcurrentHashMap<>();
    for (Map.Entry<String, Map<String, Object>> entry : original.entrySet()) {
      copy.put(entry.getKey(), new HashMap<>(entry.getValue()));
    }
    return copy;
  }

  // ==================== TOOL METHODS ====================

  @Schema(description = "Retrieve detailed customer account information")
  public Map<String, Object> getCustomerAccount(
      @Schema(description = "Customer ID (format: CUST###)") String customerId,
      ToolContext toolContext) {
    Map<String, Object> result = new HashMap<>();

    try {
      logger.info("[TOOL] getCustomerAccount called for: " + customerId);

      customerId = ValidationUtils.validateCustomerId(customerId);

      String cacheKey = "customer:" + customerId;
      @SuppressWarnings("unchecked")
      Map<String, Object> cachedResult = (Map<String, Object>) toolContext.state().get(cacheKey);

      if (cachedResult != null) {
        logger.info("[CACHE-HIT] Retrieved from cache: " + customerId);
        return cachedResult;
      }

      Map<String, Object> customer = mockDatabase.get(customerId);
      if (customer == null) {
        result.put("success", false);
        result.put("error", "Customer not found: " + customerId);
        return result;
      }

      result.put("success", true);
      result.put("customer", new HashMap<>(customer));

      toolContext.state().put(cacheKey, result);
      toolContext.state().put("current_customer", customerId);

      logger.info("[SUCCESS] Customer retrieved: " + customerId);
      return result;

    } catch (IllegalArgumentException e) {
      result.put("success", false);
      result.put("error", e.getMessage());
      return result;
    } catch (Exception e) {
      logger.severe("[ERROR] getCustomerAccount failed: " + e.getMessage());
      result.put("success", false);
      result.put("error", "System error: " + e.getMessage());
      return result;
    }
  }

  @Schema(description = "Process a customer payment")
  public Map<String, Object> processPayment(
      @Schema(description = "Customer ID") String customerId,
      @Schema(description = "Payment amount (USD)") Object amountObj,
      ToolContext toolContext) {
    Map<String, Object> result = new HashMap<>();

    try {
      logger.info("[TOOL] processPayment called for: " + customerId);

      customerId = ValidationUtils.validateCustomerId(customerId);
      double amount = ValidationUtils.validateAmount(amountObj);

      Map<String, Object> customer = mockDatabase.get(customerId);
      if (customer == null) {
        result.put("success", false);
        result.put("error", "Customer not found: " + customerId);
        return result;
      }

      String transactionId = TransactionIdGenerator.generateTransactionId("TXN");

      double currentBalance = ((Number) customer.get("accountBalance")).doubleValue();
      double newBalance = currentBalance + amount;
      customer.put("accountBalance", Math.round(newBalance * 100.0) / 100.0);
      customer.put("lastPaymentDate", LocalDate.now().format(DATE_FORMATTER));

      toolContext.state().put("last_transaction_id", transactionId);
      toolContext.state().put("last_payment_amount", amount);

      result.put("success", true);
      result.put("transactionId", transactionId);
      result.put("amount", amount);
      result.put("previousBalance", currentBalance);
      result.put("newBalance", newBalance);
      result.put("message", "Payment processed successfully");

      logger.info("[SUCCESS] Payment processed: " + transactionId);
      return result;

    } catch (IllegalArgumentException e) {
      result.put("success", false);
      result.put("error", e.getMessage());
      return result;
    } catch (Exception e) {
      logger.severe("[ERROR] processPayment failed: " + e.getMessage());
      result.put("success", false);
      result.put("error", "System error: " + e.getMessage());
      return result;
    }
  }

  @Schema(description = "Create a new support ticket")
  public Map<String, Object> createTicket(
      @Schema(description = "Customer ID") String customerId,
      @Schema(description = "Ticket subject") String subject,
      @Schema(description = "Ticket description") String description,
      @Schema(description = "Priority (LOW, MEDIUM, HIGH)") String priority,
      ToolContext toolContext) {
    Map<String, Object> result = new HashMap<>();

    try {
      logger.info("[TOOL] createTicket called for: " + customerId);

      customerId = ValidationUtils.validateCustomerId(customerId);

      if (subject == null || subject.trim().isEmpty()) {
        throw new IllegalArgumentException("Subject is required");
      }
      if (description == null || description.trim().isEmpty()) {
        throw new IllegalArgumentException("Description is required");
      }

      priority = ValidationUtils.validatePriority(priority);
      String ticketId = TransactionIdGenerator.generateTicketId();

      Map<String, Object> ticket = new HashMap<>();
      ticket.put("ticketId", ticketId);
      ticket.put("customerId", customerId);
      ticket.put("subject", subject.trim());
      ticket.put("description", description.trim());
      ticket.put("priority", priority);
      ticket.put("status", "OPEN");
      ticket.put("createdAt", LocalDate.now().format(DATE_FORMATTER));

      ticketDatabase.computeIfAbsent(customerId, k -> new ArrayList<>()).add(ticket);

      result.put("success", true);
      result.put("ticket", ticket);
      result.put("message", "Ticket created successfully");

      logger.info("[SUCCESS] Ticket created: " + ticketId);
      return result;

    } catch (IllegalArgumentException e) {
      result.put("success", false);
      result.put("error", e.getMessage());
      return result;
    } catch (Exception e) {
      logger.severe("[ERROR] createTicket failed: " + e.getMessage());
      result.put("success", false);
      result.put("error", "System error: " + e.getMessage());
      return result;
    }
  }

  @Schema(description = "Retrieve support tickets for a customer")
  public Map<String, Object> getTickets(
      @Schema(description = "Customer ID") String customerId,
      @Schema(description = "Filter by status") String status,
      ToolContext toolContext) {
    Map<String, Object> result = new HashMap<>();

    try {
      logger.info("[TOOL] getTickets called for: " + customerId);

      customerId = ValidationUtils.validateCustomerId(customerId);

      List<Map<String, Object>> customerTickets = new ArrayList<>();
      String statusFilter =
          (status != null && !status.trim().isEmpty()) ? status.toUpperCase() : null;

      List<Map<String, Object>> allTickets =
          ticketDatabase.getOrDefault(customerId, Collections.emptyList());

      for (Map<String, Object> ticket : allTickets) {
        String ticketStatus = (String) ticket.get("status");
        if (statusFilter == null || statusFilter.equalsIgnoreCase(ticketStatus)) {
          customerTickets.add(ticket);
        }
      }

      result.put("success", true);
      result.put("tickets", customerTickets);
      result.put("count", customerTickets.size());

      logger.info("[SUCCESS] Retrieved " + customerTickets.size() + " tickets");
      return result;

    } catch (IllegalArgumentException e) {
      result.put("success", false);
      result.put("error", e.getMessage());
      return result;
    } catch (Exception e) {
      logger.severe("[ERROR] getTickets failed: " + e.getMessage());
      result.put("success", false);
      result.put("error", "System error: " + e.getMessage());
      return result;
    }
  }

  @Schema(description = "Update customer account settings")
  public Map<String, Object> updateAccountSettings(
      @Schema(description = "Customer ID") String customerId,
      @Schema(description = "New email address") String email,
      @Schema(description = "New tier (Basic, Premium, Enterprise)") String tier,
      ToolContext toolContext) {
    Map<String, Object> result = new HashMap<>();

    try {
      logger.info("[TOOL] updateAccountSettings called for: " + customerId);

      customerId = ValidationUtils.validateCustomerId(customerId);

      Map<String, Object> customer = mockDatabase.get(customerId);
      if (customer == null) {
        result.put("success", false);
        result.put("error", "Customer not found: " + customerId);
        return result;
      }

      Map<String, String> updates = new HashMap<>();

      if (email != null && !email.trim().isEmpty()) {
        String validatedEmail = ValidationUtils.validateEmail(email);
        customer.put("email", validatedEmail);
        updates.put("email", validatedEmail);
      }

      if (tier != null && !tier.trim().isEmpty()) {
        String properTier = ValidationUtils.validateTier(tier);
        customer.put("tier", properTier);
        updates.put("tier", properTier);
      }

      if (updates.isEmpty()) {
        result.put("success", false);
        result.put("error", "No valid updates provided");
        return result;
      }

      result.put("success", true);
      result.put("updates", updates);
      result.put("message", "Account settings updated successfully");

      logger.info("[SUCCESS] Account updated: " + customerId);
      return result;

    } catch (IllegalArgumentException e) {
      result.put("success", false);
      result.put("error", e.getMessage());
      return result;
    } catch (Exception e) {
      logger.severe("[ERROR] updateAccountSettings failed: " + e.getMessage());
      result.put("success", false);
      result.put("error", "System error: " + e.getMessage());
      return result;
    }
  }

  @Schema(description = "Validate if a customer is eligible for a refund")
  public Map<String, Object> validateRefundEligibility(
      @Schema(description = "Customer ID") String customerId, ToolContext toolContext) {
    Map<String, Object> result = new HashMap<>();

    try {
      logger.info("[TOOL] validateRefundEligibility called for: " + customerId);

      customerId = ValidationUtils.validateCustomerId(customerId);

      Map<String, Object> customer = mockDatabase.get(customerId);
      if (customer == null) {
        result.put("success", false);
        result.put("error", "Customer not found: " + customerId);
        toolContext.state().put("refund_eligible", false);
        return result;
      }

      List<String> reasons = new ArrayList<>();
      boolean eligible = true;

      if (!"Active".equals(customer.get("accountStatus"))) {
        eligible = false;
        reasons.add("Account is not active");
      }

      String lastPaymentDateStr = (String) customer.get("lastPaymentDate");
      if (lastPaymentDateStr != null) {
        LocalDate lastPayment = LocalDate.parse(lastPaymentDateStr, DATE_FORMATTER);
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        if (lastPayment.isBefore(thirtyDaysAgo)) {
          eligible = false;
          reasons.add("Last payment was more than 30 days ago");
        }
      } else {
        eligible = false;
        reasons.add("No payment history found");
      }

      result.put("success", true);
      result.put("eligible", eligible);
      result.put("tier", customer.get("tier"));
      result.put("reasons", reasons);

      toolContext.state().put("refund_eligible", eligible);
      toolContext.state().put("refund_customer", customerId);

      logger.info("[SUCCESS] Validation complete: " + customerId + " - Eligible: " + eligible);
      return result;

    } catch (IllegalArgumentException e) {
      result.put("success", false);
      result.put("error", e.getMessage());
      // toolContext.getState().put("refund_eligible", false);
      return result;
    } catch (Exception e) {
      logger.severe("[ERROR] validateRefundEligibility failed: " + e.getMessage());
      result.put("success", false);
      result.put("error", "System error: " + e.getMessage());
      // toolContext.getState().put("refund_eligible", false);
      return result;
    }
  }

  @Schema(description = "Process a refund for an eligible customer")
  public Map<String, Object> processRefund(
      @Schema(description = "Customer ID") String customerId,
      @Schema(description = "Refund amount") Object amountObj,
      ToolContext toolContext) {
    Map<String, Object> result = new HashMap<>();

    try {
      logger.info("[TOOL] processRefund called for: " + customerId);

      Boolean eligible = (Boolean) toolContext.state().get("refund_eligible");
      String contextCustomerId = (String) toolContext.state().get("refund_customer");

      if (eligible == null || !eligible) {
        result.put("success", false);
        result.put(
            "error", "Refund validation must be completed first and customer must be eligible");
        return result;
      }

      customerId = ValidationUtils.validateCustomerId(customerId);

      if (!customerId.equals(contextCustomerId)) {
        result.put("success", false);
        result.put("error", "Customer ID mismatch with validation");
        return result;
      }

      double amount = ValidationUtils.validateAmount(amountObj);

      Map<String, Object> customer = mockDatabase.get(customerId);
      if (customer == null) {
        result.put("success", false);
        result.put("error", "Customer not found: " + customerId);
        return result;
      }

      double currentBalance = ((Number) customer.get("accountBalance")).doubleValue();
      if (amount > currentBalance) {
        result.put("success", false);
        result.put("error", "Refund amount exceeds current balance");
        return result;
      }

      String refundId = TransactionIdGenerator.generateTransactionId("REF");

      double newBalance = currentBalance - amount;
      customer.put("accountBalance", Math.round(newBalance * 100.0) / 100.0);

      toolContext.state().put("last_refund_id", refundId);
      toolContext.state().put("refund_amount", amount);
      toolContext.state().remove("refund_eligible");
      toolContext.state().remove("refund_customer");

      result.put("success", true);
      result.put("refundId", refundId);
      result.put("amount", amount);
      result.put("previousBalance", currentBalance);
      result.put("newBalance", newBalance);
      result.put(
          "message",
          "Refund processed successfully. Funds will be returned within 5-7 business days");

      logger.info("[SUCCESS] Refund processed: " + refundId);
      return result;

    } catch (IllegalArgumentException e) {
      result.put("success", false);
      result.put("error", e.getMessage());
      return result;
    } catch (Exception e) {
      logger.severe("[ERROR] processRefund failed: " + e.getMessage());
      result.put("success", false);
      result.put("error", "System error: " + e.getMessage());
      return result;
    }
  }
}
