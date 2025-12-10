package com.example.support;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;
import com.google.adk.tools.ToolContext;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomerSupportAgent {

  public static BaseAgent createAgent() {
    return LlmAgent.builder()
        .name("customer-support-agent")
        .description("A helpful customer support agent")
        .model("gemini-1.5-flash")
        .instruction("You are a helpful customer support agent...")
        .tools(
            createBillingAgent(),
            createTechnicalSupportAgent(),
            createAccountAgent(),
            createRefundWorkflow())
        .build();
  }

  private static BaseAgent createBillingAgent() {
    return LlmAgent.builder()
        .name("billing-agent")
        .description("Handles billing and payment inquiries")
        .tools(
            FunctionTool.create(CustomerSupportAgent.class, "getCustomerAccount"),
            FunctionTool.create(CustomerSupportAgent.class, "processPayment"),
            FunctionTool.create(CustomerSupportAgent.class, "getTickets"))
        .build();
  }

  private static BaseAgent createTechnicalSupportAgent() {
    return LlmAgent.builder()
        .name("technical-support-agent")
        .description("Assists with technical issues")
        .tools(
            FunctionTool.create(CustomerSupportAgent.class, "getCustomerAccount"),
            FunctionTool.create(CustomerSupportAgent.class, "createTicket"),
            FunctionTool.create(CustomerSupportAgent.class, "getTickets"))
        .build();
  }

  private static BaseAgent createAccountAgent() {
    return LlmAgent.builder()
        .name("account-agent")
        .description("Manages account settings")
        .tools(
            FunctionTool.create(CustomerSupportAgent.class, "getCustomerAccount"),
            FunctionTool.create(CustomerSupportAgent.class, "updateAccountSettings"))
        .build();
  }

  private static BaseAgent createRefundWorkflow() {
    LlmAgent validator =
        LlmAgent.builder()
            .name("refund-validator")
            .instruction("Validate if the customer is eligible for a refund.")
            .tools(FunctionTool.create(CustomerSupportAgent.class, "validateRefundEligibility"))
            .build();

    LlmAgent processor =
        LlmAgent.builder()
            .name("refund-processor")
            .instruction("Process the refund for the customer.")
            .tools(FunctionTool.create(CustomerSupportAgent.class, "processRefund"))
            .build();

    return SequentialAgent.builder()
        .name("refund-processor-workflow")
        .description("A workflow to process customer refunds.")
        .subAgents(validator, processor)
        .build();
  }

  @Schema(description = "Get customer account details.")
  public static Map<String, Object> getCustomerAccount(
      @Schema(description = "Customer ID") String customerId, ToolContext toolContext) {
    // Mock implementation
    Map<String, Object> account = new HashMap<>();
    account.put("customerId", customerId);
    account.put("balance", 1250.00);
    return account;
  }

  @Schema(description = "Process a payment.")
  public static Map<String, Object> processPayment(
      @Schema(description = "Customer ID") String customerId,
      @Schema(description = "Payment amount") double amount,
      ToolContext toolContext) {
    // Mock implementation
    Map<String, Object> result = new HashMap<>();
    result.put("transactionId", "TXN-" + UUID.randomUUID());
    result.put("newBalance", 1250.00 - amount);
    return result;
  }

  @Schema(description = "Create a support ticket.")
  public static Map<String, Object> createTicket(
      @Schema(description = "Customer ID") String customerId,
      @Schema(description = "Issue description") String issue,
      ToolContext toolContext) {
    // Mock implementation
    Map<String, Object> ticket = new HashMap<>();
    ticket.put("ticketId", "TKT-" + UUID.randomUUID());
    ticket.put("status", "OPEN");
    return ticket;
  }

  @Schema(description = "Get support tickets for a customer.")
  public static Map<String, Object> getTickets(
      @Schema(description = "Customer ID") String customerId,
      @Schema(description = "Ticket status") String status,
      ToolContext toolContext) {
    // Mock implementation
    Map<String, Object> result = new HashMap<>();
    result.put("tickets", new String[] {"TKT-123", "TKT-456"});
    return result;
  }

  @Schema(description = "Update account settings.")
  public static Map<String, Object> updateAccountSettings(
      @Schema(description = "Customer ID") String customerId,
      @Schema(description = "New settings") Map<String, String> settings,
      ToolContext toolContext) {
    // Mock implementation
    Map<String, Object> result = new HashMap<>();
    result.put("status", "success");
    return result;
  }

  @Schema(description = "Validate refund eligibility.")
  public static Map<String, Object> validateRefundEligibility(
      @Schema(description = "Customer ID") String customerId, ToolContext toolContext) {
    // Mock implementation
    Map<String, Object> result = new HashMap<>();
    result.put("isEligible", true);
    result.put("reason", "Customer is premium tier.");
    return result;
  }

  @Schema(description = "Process a refund.")
  public static Map<String, Object> processRefund(
      @Schema(description = "Customer ID") String customerId,
      @Schema(description = "Refund amount") double amount,
      ToolContext toolContext) {
    // Mock implementation
    Map<String, Object> result = new HashMap<>();
    result.put("refundId", "REF-" + UUID.randomUUID());
    result.put("status", "PROCESSED");
    return result;
  }
}
