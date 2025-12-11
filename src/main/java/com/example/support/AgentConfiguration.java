package com.example.support;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.tools.FunctionTool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfiguration {

    // --- Dependency Injection via Constructor (Spring Best Practice) ---
    private final CustomerSupportAgent customerSupportAgent;

    public AgentConfiguration(CustomerSupportAgent customerSupportAgent) {
        this.customerSupportAgent = customerSupportAgent;
    }

    /**
     * Defines the Root Agent for the customer support system.
     * This agent acts as a Router, deciding which sub-agent (tool) to invoke.
     */
    @Bean
    public BaseAgent rootCustomerSupportAgent() {
        return LlmAgent.builder()
                // Use a descriptive name for the main entry agent
                .name("customer-support-agent")
                .description("The main router agent for customer inquiries.")
                .model("gemini-1.5-flash")
                .instruction("You are a helpful customer support agent for Acme Corp. Your job is to analyze the user's request and delegate the task to one of your specialized sub-agents (tools) to handle billing, technical, account, or refund requests. You must only use the tools provided.")
                // The sub-agents are passed as tools to the root LLMAgent,
                // allowing it to act as a router/planner.
                .tools(
                        createBillingAgent(),
                        createTechnicalSupportAgent(),
                        createAccountAgent(),
                        createRefundWorkflow())
                .build();
    }
    
    // --- Sub-Agents Defined Below ---

    private BaseAgent createBillingAgent() {
        return LlmAgent.builder()
                .name("billing-agent")
                .description("Handles billing and payment inquiries. Use this agent for customer balance checks, payment processing, or retrieving payment history tickets.")
                .tools(
                        // Create FunctionTools by referencing the CustomerSupportAgent instance and method name
                        FunctionTool.create(customerSupportAgent, "getCustomerAccount"),
                        FunctionTool.create(customerSupportAgent, "processPayment"),
                        FunctionTool.create(customerSupportAgent, "getTickets"))
                .build();
    }

    private BaseAgent createTechnicalSupportAgent() {
        return LlmAgent.builder()
                .name("technical-support-agent")
                .description("Assists with technical issues. Use this agent to create new support tickets or retrieve existing ticket details.")
                .tools(
                        FunctionTool.create(customerSupportAgent, "getCustomerAccount"),
                        FunctionTool.create(customerSupportAgent, "createTicket"),
                        FunctionTool.create(customerSupportAgent, "getTickets"))
                .build();
    }

    private BaseAgent createAccountAgent() {
        return LlmAgent.builder()
                .name("account-agent")
                .description("Manages customer account settings. Use this agent for updating customer contact information or subscription tiers.")
                .tools(
                        FunctionTool.create(customerSupportAgent, "getCustomerAccount"),
                        FunctionTool.create(customerSupportAgent, "updateAccountSettings"))
                .build();
    }

    /**
     * Creates a SequentialAgent that enforces a two-step process:
     * 1. Refund eligibility validation
     * 2. Refund processing
     * 
     */
    private BaseAgent createRefundWorkflow() {
        LlmAgent validator =
                LlmAgent.builder()
                        .name("refund-validator")
                        .instruction("Your sole task is to determine the customer ID and then call the validateRefundEligibility tool.")
                        .tools(FunctionTool.create(customerSupportAgent, "validateRefundEligibility"))
                        .build();

        LlmAgent processor =
                LlmAgent.builder()
                        .name("refund-processor")
                        .instruction("Your sole task is to process the refund by calling the processRefund tool, using information passed from the previous step.")
                        .tools(FunctionTool.create(customerSupportAgent, "processRefund"))
                        .build();

        return SequentialAgent.builder()
                .name("refund-processor-workflow")
                .description("A two-step sequential workflow to process customer refunds: first validation, then processing. Use this for all refund requests.")
                .subAgents(validator, processor)
                .build();
    }
}
