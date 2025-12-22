package com.example.support;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Core business logic for customer support operations. Provides 7 tools for multi-agent system. */
@Component
public class CustomerSupportAgent {

  // Mock customer database
  private static final Map<String, Map<String, Object>> CUSTOMERS = new HashMap<>();

  // Mock tickets storage
  private static final List<Map<String, Object>> TICKETS = new ArrayList<>();

  static {
    initializeMockData();
  }

  /** Initializes mock customer data for testing. */
  private static void initializeMockData() {
    // Customer 1: John Doe
    Map<String, Object> cust1 = new HashMap<>();
    cust1.put("customerId", "CUST001");
    cust1.put("name", "John Doe");
    cust1.put("email", "john.doe@example.com");
    cust1.put("tier", "Premium");
    cust1.put("balance", 1250.00);
    cust1.put("accountCreated", LocalDateTime.now().minusDays(45));
    cust1.put("status", "active");
    CUSTOMERS.put("CUST001", cust1);

    // Customer 2: Jane Smith
    Map<String, Object> cust2 = new HashMap<>();
    cust2.put("customerId", "CUST002");
    cust2.put("name", "Jane Smith");
    cust2.put("email", "jane.smith@example.com");
    cust2.put("tier", "Basic");
    cust2.put("balance", 0.00);
    cust2.put("accountCreated", LocalDateTime.now().minusDays(5));
    cust2.put("status", "active");
    CUSTOMERS.put("CUST002", cust2);

    // Customer 3: Bob Johnson
    Map<String, Object> cust3 = new HashMap<>();
    cust3.put("customerId", "CUST003");
    cust3.put("name", "Bob Johnson");
    cust3.put("email", "bob.johnson@example.com");
    cust3.put("tier", "Enterprise");
    cust3.put("balance", 5000.00);
    cust3.put("accountCreated", LocalDateTime.now().minusDays(10));
    cust3.put("status", "active");
    CUSTOMERS.put("CUST003", cust3);
  }

  /**
   * Tool 1: Get customer account details.
   *
   * @param customerId the customer ID
   * @param context tool context for caching
   * @return customer account data
   */
  public Map<String, Object> getCustomerAccount(String customerId, Map<String, Object> context) {
    Map<String, Object> result = new HashMap<>();

    // Validation
    if (!ValidationUtils.isValidCustomerId(customerId)) {
      result.put("success", false);
      result.put("error", "Invalid customer ID format");
      return result;
    }

    // Check cache
    String cacheKey = "customer_" + customerId;
    if (context != null && context.containsKey(cacheKey)) {
      result.put("success", true);
      result.put("data", context.get(cacheKey));
      result.put("cached", true);
      return result;
    }

    // Retrieve customer
    Map<String, Object> customer = CUSTOMERS.get(customerId);
    if (customer == null) {
      result.put("success", false);
      result.put("error", "Customer not found");
      return result;
    }

    // Cache and return
    if (context != null) {
      context.put(cacheKey, new HashMap<>(customer));
      result.put("cached", false);
    }

    result.put("success", true);
    result.put("data", new HashMap<>(customer));
    return result;
  }

  /**
   * Tool 2: Process payment.
   *
   * @param customerId the customer ID
   * @param amount the payment amount
   * @param context tool context
   * @return payment result
   */
  public Map<String, Object> processPayment(
      String customerId, Object amount, Map<String, Object> context) {
    Map<String, Object> result = new HashMap<>();

    // Validation
    if (!ValidationUtils.isValidCustomerId(customerId)) {
      result.put("success", false);
      result.put("error", "Invalid customer ID");
      return result;
    }

    double paymentAmount;
    try {
      if (amount instanceof Number) {
        paymentAmount = ((Number) amount).doubleValue();
      } else {
        paymentAmount = Double.parseDouble(amount.toString());
      }
      paymentAmount = ValidationUtils.roundAmount(paymentAmount);
    } catch (Exception e) {
      result.put("success", false);
      result.put("error", "Invalid amount format");
      return result;
    }

    if (!ValidationUtils.isValidAmount(paymentAmount)) {
      result.put("success", false);
      result.put("error", "Amount must be between 0 and 100000");
      return result;
    }

    // Get customer
    Map<String, Object> customer = CUSTOMERS.get(customerId);
    if (customer == null) {
      result.put("success", false);
      result.put("error", "Customer not found");
      return result;
    }

    // Process payment
    double currentBalance = ((Number) customer.get("balance")).doubleValue();
    double newBalance = ValidationUtils.roundAmount(currentBalance + paymentAmount);
    customer.put("balance", newBalance);

    // Generate transaction ID
    String transactionId = TransactionIdGenerator.generate();

    // Clear cache
    if (context != null) {
      context.remove("customer_" + customerId);
    }

    Map<String, Object> data = new HashMap<>();
    data.put("transactionId", transactionId);
    data.put("amount", paymentAmount);
    data.put("newBalance", newBalance);
    data.put("timestamp", LocalDateTime.now().toString());

    result.put("success", true);
    result.put("data", data);
    result.put("message", "Payment processed successfully");
    return result;
  }

  /**
   * Tool 3: Create support ticket.
   *
   * @param customerId the customer ID
   * @param subject ticket subject
   * @param description ticket description
   * @param priority ticket priority
   * @param context tool context
   * @return ticket creation result
   */
  public Map<String, Object> createTicket(
      String customerId,
      String subject,
      String description,
      String priority,
      Map<String, Object> context) {
    Map<String, Object> result = new HashMap<>();

    // Validation
    if (!ValidationUtils.isValidCustomerId(customerId)) {
      result.put("success", false);
      result.put("error", "Invalid customer ID");
      return result;
    }

    if (subject == null || subject.trim().isEmpty()) {
      result.put("success", false);
      result.put("error", "Subject is required");
      return result;
    }

    if (description == null || description.trim().isEmpty()) {
      result.put("success", false);
      result.put("error", "Description is required");
      return result;
    }

    if (!ValidationUtils.isValidPriority(priority)) {
      result.put("success", false);
      result.put("error", "Invalid priority. Must be: low, medium, high, or urgent");
      return result;
    }

    // Check customer exists
    if (!CUSTOMERS.containsKey(customerId)) {
      result.put("success", false);
      result.put("error", "Customer not found");
      return result;
    }

    // Create ticket
    String ticketId = TransactionIdGenerator.generateTicketId();
    Map<String, Object> ticket = new HashMap<>();
    ticket.put("ticketId", ticketId);
    ticket.put("customerId", customerId);
    ticket.put("subject", ValidationUtils.sanitize(subject));
    ticket.put("description", ValidationUtils.sanitize(description));
    ticket.put("priority", priority.toLowerCase());
    ticket.put("status", "open");
    ticket.put("created", LocalDateTime.now().toString());

    TICKETS.add(ticket);

    result.put("success", true);
    result.put("data", new HashMap<>(ticket));
    result.put("message", "Ticket created successfully");
    return result;
  }

  /**
   * Tool 4: Get tickets for a customer.
   *
   * @param customerId the customer ID
   * @param status filter by status (optional, use "all" for all)
   * @param context tool context
   * @return list of tickets
   */
  public Map<String, Object> getTickets(
      String customerId, String status, Map<String, Object> context) {
    Map<String, Object> result = new HashMap<>();

    // Validation
    if (!ValidationUtils.isValidCustomerId(customerId)) {
      result.put("success", false);
      result.put("error", "Invalid customer ID");
      return result;
    }

    if (!ValidationUtils.isValidStatus(status)) {
      result.put("success", false);
      result.put("error", "Invalid status. Must be: open, closed, pending, or all");
      return result;
    }

    // Check customer exists
    if (!CUSTOMERS.containsKey(customerId)) {
      result.put("success", false);
      result.put("error", "Customer not found");
      return result;
    }

    // Filter tickets
    String normalizedStatus = status.toLowerCase();
    List<Map<String, Object>> filteredTickets =
        TICKETS.stream()
            .filter(t -> t.get("customerId").equals(customerId))
            .filter(t -> normalizedStatus.equals("all") || t.get("status").equals(normalizedStatus))
            .map(HashMap::new)
            .collect(Collectors.toList());

    result.put("success", true);
    result.put("data", filteredTickets);
    result.put("count", filteredTickets.size());
    return result;
  }

  /**
   * Tool 5: Update account settings.
   *
   * @param customerId the customer ID
   * @param email new email (optional, null to keep current)
   * @param tier new tier (optional, null to keep current)
   * @param context tool context
   * @return update result
   */
  public Map<String, Object> updateAccountSettings(
      String customerId, String email, String tier, Map<String, Object> context) {
    Map<String, Object> result = new HashMap<>();

    // Validation
    if (!ValidationUtils.isValidCustomerId(customerId)) {
      result.put("success", false);
      result.put("error", "Invalid customer ID");
      return result;
    }

    if (email == null && tier == null) {
      result.put("success", false);
      result.put("error", "At least one field (email or tier) must be provided");
      return result;
    }

    if (email != null && !ValidationUtils.isValidEmail(email)) {
      result.put("success", false);
      result.put("error", "Invalid email format");
      return result;
    }

    if (tier != null && !ValidationUtils.isValidTier(tier)) {
      result.put("success", false);
      result.put("error", "Invalid tier. Must be: basic, premium, or enterprise");
      return result;
    }

    // Get customer
    Map<String, Object> customer = CUSTOMERS.get(customerId);
    if (customer == null) {
      result.put("success", false);
      result.put("error", "Customer not found");
      return result;
    }

    // Update fields
    Map<String, String> updates = new HashMap<>();
    if (email != null) {
      customer.put("email", email);
      updates.put("email", email);
    }
    if (tier != null) {
      String normalizedTier = tier.substring(0, 1).toUpperCase() + tier.substring(1).toLowerCase();
      customer.put("tier", normalizedTier);
      updates.put("tier", normalizedTier);
    }

    // Clear cache
    if (context != null) {
      context.remove("customer_" + customerId);
    }

    result.put("success", true);
    result.put("data", updates);
    result.put("message", "Account settings updated successfully");
    return result;
  }

  /**
   * Tool 6: Validate refund eligibility.
   *
   * @param customerId the customer ID
   * @param context tool context
   * @return eligibility result
   */
  public Map<String, Object> validateRefundEligibility(
      String customerId, Map<String, Object> context) {
    Map<String, Object> result = new HashMap<>();

    // Validation
    if (!ValidationUtils.isValidCustomerId(customerId)) {
      result.put("success", false);
      result.put("error", "Invalid customer ID");
      return result;
    }

    // Get customer
    Map<String, Object> customer = CUSTOMERS.get(customerId);
    if (customer == null) {
      result.put("success", false);
      result.put("error", "Customer not found");
      return result;
    }

    // Check eligibility criteria
    LocalDateTime accountCreated = (LocalDateTime) customer.get("accountCreated");
    long daysSinceCreation = ChronoUnit.DAYS.between(accountCreated, LocalDateTime.now());
    String status = (String) customer.get("status");

    boolean eligible = daysSinceCreation <= 30 && "active".equals(status);

    Map<String, Object> data = new HashMap<>();
    data.put("eligible", eligible);
    data.put("daysSinceCreation", daysSinceCreation);
    data.put("accountStatus", status);

    if (eligible) {
      data.put("message", "Customer is eligible for refund");
      // Store validation state
      if (context != null) {
        context.put("refund_validated_" + customerId, true);
      }
    } else {
      if (daysSinceCreation > 30) {
        data.put("message", "Account is older than 30 days");
      } else {
        data.put("message", "Account status is not active");
      }
    }

    result.put("success", true);
    result.put("data", data);
    return result;
  }

  /**
   * Tool 7: Process refund.
   *
   * @param customerId the customer ID
   * @param amount refund amount
   * @param context tool context
   * @return refund result
   */
  public Map<String, Object> processRefund(
      String customerId, Object amount, Map<String, Object> context) {
    Map<String, Object> result = new HashMap<>();

    // Validation
    if (!ValidationUtils.isValidCustomerId(customerId)) {
      result.put("success", false);
      result.put("error", "Invalid customer ID");
      return result;
    }

    // Check validation state
    if (context == null || !Boolean.TRUE.equals(context.get("refund_validated_" + customerId))) {
      result.put("success", false);
      result.put("error", "Refund eligibility must be validated first");
      return result;
    }

    double refundAmount;
    try {
      if (amount instanceof Number) {
        refundAmount = ((Number) amount).doubleValue();
      } else {
        refundAmount = Double.parseDouble(amount.toString());
      }
      refundAmount = ValidationUtils.roundAmount(refundAmount);
    } catch (Exception e) {
      result.put("success", false);
      result.put("error", "Invalid amount format");
      return result;
    }

    if (!ValidationUtils.isValidAmount(refundAmount)) {
      result.put("success", false);
      result.put("error", "Amount must be between 0 and 100000");
      return result;
    }

    // Get customer
    Map<String, Object> customer = CUSTOMERS.get(customerId);
    if (customer == null) {
      result.put("success", false);
      result.put("error", "Customer not found");
      return result;
    }

    // Check sufficient balance
    double currentBalance = ((Number) customer.get("balance")).doubleValue();
    if (currentBalance < refundAmount) {
      result.put("success", false);
      result.put("error", "Insufficient balance for refund");
      return result;
    }

    // Process refund
    double newBalance = ValidationUtils.roundAmount(currentBalance - refundAmount);
    customer.put("balance", newBalance);

    String refundId = TransactionIdGenerator.generateRefundId();

    // Clear cache and validation state
    if (context != null) {
      context.remove("customer_" + customerId);
      context.remove("refund_validated_" + customerId);
    }

    Map<String, Object> data = new HashMap<>();
    data.put("refundId", refundId);
    data.put("amount", refundAmount);
    data.put("newBalance", newBalance);
    data.put("processedAt", LocalDateTime.now().toString());
    data.put("estimatedArrival", "5-7 business days");

    result.put("success", true);
    result.put("data", data);
    result.put("message", "Refund processed successfully");
    return result;
  }

  /** Resets all mock data (for testing). */
  public static void resetData() {
    CUSTOMERS.clear();
    TICKETS.clear();
    initializeMockData();
    TransactionIdGenerator.resetCounter();
  }
}
