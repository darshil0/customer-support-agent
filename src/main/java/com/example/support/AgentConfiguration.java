package com.example.support;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.tools.FunctionTool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AgentConfiguration defines the hierarchical multi-agent architecture for customer support,
 * including billing, technical, account, and refund workflows.
 *
 * @author Darshil
 * @version 1.0.4
 */
@Configuration
public class AgentConfiguration {

  private final CustomerSupportAgent customerSupportAgent;

  public AgentConfiguration(CustomerSupportAgent customerSupportAgent) {
    this.customerSupportAgent = customerSupportAgent;
  }

  /** Root orchestrator agent that routes queries to specialized sub-agents. */
  @Bean
  public BaseAgent rootCustomerSupportAgent() {
    return LlmAgent.builder()
        .name("customer-support-orchestrator")
        .description("Main router agent for customer inquiries")
        .model("gemini-1.5-flash")
        .instruction(
            "You are a helpful customer support agent for Acme Corp. "
                + "Analyze the customer's request and delegate it to the appropriate specialist:\n"
                + "- billing-agent: For payments, balances, invoices\n"
                + "- technical-support-agent: For technical issues, bugs, login problems\n"
                + "- account-agent: For account settings, profile updates\n"
                + "- refund-processor-workflow: For refund requests\n"
                + "Always greet the customer warmly and explain who you're connecting them with.")
        .subAgents(
            createBillingAgent(),
            createTechnicalSupportAgent(),
            createAccountAgent(),
            createRefundWorkflow())
        .build();
  }

  private LlmAgent createBillingAgent() {
    return LlmAgent.builder()
        .name("billing-agent")
        .description("Handles billing and payment inquiries")
        .model("gemini-1.5-flash")
        .instruction(
            "You are a billing specialist. Handle queries about payments, balances, and invoices. "
                + "Always confirm the customer's ID before processing transactions. "
                + "After successful payments, provide the new balance and transaction ID.")
        .tools(
            FunctionTool.create(customerSupportAgent, "getCustomerAccount"),
            FunctionTool.create(customerSupportAgent, "processPayment"),
            FunctionTool.create(customerSupportAgent, "getTickets"))
        .build();
  }

  private LlmAgent createTechnicalSupportAgent() {
    return LlmAgent.builder()
        .name("technical-support-agent")
        .description("Handles technical issues and troubleshooting")
        .model("gemini-1.5-flash")
        .instruction(
            "You are a technical support specialist. Troubleshoot customer issues. "
                + "If the issue cannot be resolved immediately, create a detailed support ticket. "
                + "Inform the customer of the ticket ID and expected response time.")
        .tools(
            FunctionTool.create(customerSupportAgent, "getCustomerAccount"),
            FunctionTool.create(customerSupportAgent, "createTicket"),
            FunctionTool.create(customerSupportAgent, "getTickets"))
        .build();
  }

  private LlmAgent createAccountAgent() {
    return LlmAgent.builder()
        .name("account-agent")
        .description("Manages account settings and profile updates")
        .model("gemini-1.5-flash")
        .instruction(
            "You are an account management specialist. Handle changes to email, tier status, "
                + "and general profile settings. Update values only when explicitly provided "
                + "by the customer, and always send a confirmation after updating.")
        .tools(
            FunctionTool.create(customerSupportAgent, "getCustomerAccount"),
            FunctionTool.create(customerSupportAgent, "updateAccountSettings"))
        .build();
  }

  /**
   * Sequential workflow for refund processing. Step 1: Validate eligibility. Step 2: Process refund
   * (if eligible).
   */
  private SequentialAgent createRefundWorkflow() {
    LlmAgent validator =
        LlmAgent.builder()
            .name("refund-validator")
            .description("Validates refund eligibility")
            .model("gemini-1.5-flash")
            .instruction(
                "Validate refund requests by calling 'validateRefundEligibility'. "
                    + "Store the result in ToolContext as 'validation_result'. "
                    + "If not eligible, explain why. If eligible, continue to the next step.")
            .tools(FunctionTool.create(customerSupportAgent, "validateRefundEligibility"))
            .outputKey("validation_result")
            .build();

    LlmAgent processor =
        LlmAgent.builder()
            .name("refund-processor")
            .description("Processes approved refunds")
            .model("gemini-1.5-flash")
            .instruction(
                "Process approved refunds by checking ToolContext for the 'refund_eligible' flag. "
                    + "If eligible, call 'processRefund' and inform the customer that processing "
                    + "takes 5–7 business days.")
            .tools(FunctionTool.create(customerSupportAgent, "processRefund"))
            .build();

    return SequentialAgent.builder()
        .name("refund-processor-workflow")
        .description("Sequential two-step refund workflow: validate → process")
        .subAgents(validator, processor)
        .build();
  }
}
