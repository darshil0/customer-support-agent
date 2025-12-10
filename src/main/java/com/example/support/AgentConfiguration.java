package com.example.support;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.tools.FunctionTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfiguration {

  @Autowired private CustomerSupportAgent customerSupportAgent;

  @Bean
  public BaseAgent customerSupportAgent() {
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

  private BaseAgent createBillingAgent() {
    return LlmAgent.builder()
        .name("billing-agent")
        .description("Handles billing and payment inquiries")
        .tools(
            FunctionTool.create(customerSupportAgent, "getCustomerAccount"),
            FunctionTool.create(customerSupportAgent, "processPayment"),
            FunctionTool.create(customerSupportAgent, "getTickets"))
        .build();
  }

  private BaseAgent createTechnicalSupportAgent() {
    return LlmAgent.builder()
        .name("technical-support-agent")
        .description("Assists with technical issues")
        .tools(
            FunctionTool.create(customerSupportAgent, "getCustomerAccount"),
            FunctionTool.create(customerSupportAgent, "createTicket"),
            FunctionTool.create(customerSupportAgent, "getTickets"))
        .build();
  }

  private BaseAgent createAccountAgent() {
    return LlmAgent.builder()
        .name("account-agent")
        .description("Manages account settings")
        .tools(
            FunctionTool.create(customerSupportAgent, "getCustomerAccount"),
            FunctionTool.create(customerSupportAgent, "updateAccountSettings"))
        .build();
  }

  private BaseAgent createRefundWorkflow() {
    LlmAgent validator =
        LlmAgent.builder()
            .name("refund-validator")
            .instruction("Validate if the customer is eligible for a refund.")
            .tools(FunctionTool.create(customerSupportAgent, "validateRefundEligibility"))
            .build();

    LlmAgent processor =
        LlmAgent.builder()
            .name("refund-processor")
            .instruction("Process the refund for the customer.")
            .tools(FunctionTool.create(customerSupportAgent, "processRefund"))
            .build();

    return SequentialAgent.builder()
        .name("refund-processor-workflow")
        .description("A workflow to process customer refunds.")
        .subAgents(validator, processor)
        .build();
  }
}
