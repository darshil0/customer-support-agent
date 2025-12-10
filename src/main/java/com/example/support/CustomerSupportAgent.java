package com.example.support;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.ToolContext;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This class contains the tool methods for the customer support agent. The methods are used by the
 * agent to perform various tasks such as getting customer account details, processing payments,
 * creating support tickets, and more. The class is annotated with @Component to allow Spring to
 * manage it as a bean. The tool methods are annotated with @Schema to provide a description of the
 * tool to the agent. The methods return a Map<String, Object> which is a structured representation
 * of the tool's output. The mock implementation of the tools can be replaced with actual
 * implementations that interact with a database or other external services.
 */
@Component
public class CustomerSupportAgent {

  private static final Logger logger = LoggerFactory.getLogger(CustomerSupportAgent.class);

  private final Map<String, LocalDate> lastTransactionDate = new HashMap<>();

  @Schema(description = "Get customer account details.")
  public Map<String, Object> getCustomerAccount(
      @Schema(description = "Customer ID") String customerId, ToolContext toolContext) {
    logger.info("Getting account details for customer: {}", customerId);
    if (customerId == null || customerId.isEmpty()) {
      logger.error("Customer ID is required.");
      throw new IllegalArgumentException("Customer ID is required.");
    }
    // Mock implementation
    Map<String, Object> account = new HashMap<>();
    account.put("customerId", customerId);
    account.put("balance", 1250.00);
    lastTransactionDate.put(customerId, LocalDate.now().minusDays(15));
    return account;
  }

  @Schema(description = "Process a payment.")
  public Map<String, Object> processPayment(
      @Schema(description = "Customer ID") String customerId,
      @Schema(description = "Payment amount") double amount,
      ToolContext toolContext) {
    logger.info("Processing payment for customer: {} with amount: {}", customerId, amount);
    if (customerId == null || customerId.isEmpty() || amount <= 0) {
      logger.error("Customer ID and positive amount are required.");
      throw new IllegalArgumentException("Customer ID and positive amount are required.");
    }
    // Mock implementation
    Map<String, Object> result = new HashMap<>();
    result.put("transactionId", "TXN-" + UUID.randomUUID());
    result.put("newBalance", 1250.00 - amount);
    lastTransactionDate.put(customerId, LocalDate.now());
    return result;
  }

  @Schema(description = "Create a support ticket.")
  public Map<String, Object> createTicket(
      @Schema(description = "Customer ID") String customerId,
      @Schema(description = "Issue description") String issue,
      ToolContext toolContext) {
    logger.info("Creating ticket for customer: {} with issue: {}", customerId, issue);
    if (customerId == null || customerId.isEmpty() || issue == null || issue.isEmpty()) {
      logger.error("Customer ID and issue description are required.");
      throw new IllegalArgumentException("Customer ID and issue description are required.");
    }
    // Mock implementation
    Map<String, Object> ticket = new HashMap<>();
    ticket.put("ticketId", "TKT-" + UUID.randomUUID());
    ticket.put("status", "OPEN");
    return ticket;
  }

  @Schema(description = "Get support tickets for a customer.")
  public Map<String, Object> getTickets(
      @Schema(description = "Customer ID") String customerId,
      @Schema(description = "Ticket status") String status,
      ToolContext toolContext) {
    logger.info("Getting tickets for customer: {} with status: {}", customerId, status);
    if (customerId == null || customerId.isEmpty()) {
      logger.error("Customer ID is required.");
      throw new IllegalArgumentException("Customer ID is required.");
    }
    // Mock implementation
    Map<String, Object> result = new HashMap<>();
    result.put("tickets", new String[] {"TKT-123", "TKT-456"});
    return result;
  }

  @Schema(description = "Update account settings.")
  public Map<String, Object> updateAccountSettings(
      @Schema(description = "Customer ID") String customerId,
      @Schema(description = "New settings") Map<String, String> settings,
      ToolContext toolContext) {
    logger.info("Updating account settings for customer: {}", customerId);
    if (customerId == null || customerId.isEmpty() || settings == null || settings.isEmpty()) {
      logger.error("Customer ID and settings are required.");
      throw new IllegalArgumentException("Customer ID and settings are required.");
    }
    // Mock implementation
    Map<String, Object> result = new HashMap<>();
    result.put("status", "success");
    return result;
  }

  @Schema(description = "Validate refund eligibility.")
  public Map<String, Object> validateRefundEligibility(
      @Schema(description = "Customer ID") String customerId, ToolContext toolContext) {
    logger.info("Validating refund eligibility for customer: {}", customerId);
    if (customerId == null || customerId.isEmpty()) {
      logger.error("Customer ID is required.");
      throw new IllegalArgumentException("Customer ID is required.");
    }
    // Mock implementation
    Map<String, Object> result = new HashMap<>();
    LocalDate lastTransaction =
        lastTransactionDate.getOrDefault(customerId, LocalDate.now().minusDays(90));
    if (lastTransaction.isAfter(LocalDate.now().minusDays(30))) {
      result.put("isEligible", true);
      result.put("reason", "Transaction is within 30 days.");
    } else {
      result.put("isEligible", false);
      result.put("reason", "Transaction is older than 30 days.");
    }
    return result;
  }

  @Schema(description = "Process a refund.")
  public Map<String, Object> processRefund(
      @Schema(description = "Customer ID") String customerId,
      @Schema(description = "Refund amount") double amount,
      ToolContext toolContext) {
    logger.info("Processing refund for customer: {} with amount: {}", customerId, amount);
    if (customerId == null || customerId.isEmpty() || amount <= 0) {
      logger.error("Customer ID and positive amount are required.");
      throw new IllegalArgumentException("Customer ID and positive amount are required.");
    }
    // Mock implementation
    Map<String, Object> result = new HashMap<>();
    result.put("refundId", "REF-" + UUID.randomUUID());
    result.put("status", "PROCESSED");
    return result;
  }
}
