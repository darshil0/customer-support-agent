package com.example.support;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.tools.FunctionTool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Agent Configuration
 * Defines the hierarchical multi-agent architecture for customer support.
 * 
 * @author Darshil
 * @version 1.0.2 (Fixed)
 */
@Configuration
public class AgentConfiguration {

    private final CustomerSupportAgent customerSupportAgent;

    public AgentConfiguration(CustomerSupportAgent customerSupportAgent) {
        this.customerSupportAgent = customerSupportAgent;
    }

    /**
     * Root orchestrator agent that routes queries to specialized sub-agents
     */
    @Bean
    public BaseAgent rootCustomerSupportAgent() {
        return LlmAgent.builder()
                .name("customer-support-orchestrator")
                .description("Main router agent for customer inquiries")
                .model("gemini-1.5-flash")
                .instruction(
                    "You are a helpful customer support agent for Acme Corp. " +
                    "Analyze the customer's request and delegate to the appropriate specialist:\n" +
                    "- billing-agent: For payments, balances, invoices\n" +
                    "- technical-support-agent: For technical issues, bugs, login problems\n" +
                    "- account-agent: For account settings, profile updates\n" +
                    "- refund-processor-workflow: For refund requests\n" +
                    "Always greet the customer warmly and explain who you're connecting them with."
                )
                .tools(
                        createBillingAgent(),
                        createTechnicalSupportAgent(),
                        createAccountAgent(),
                        createRefundWorkflow()
                )
                .build();
    }
    
    private BaseAgent createBillingAgent() {
        return LlmAgent.builder()
                .name("billing-agent")
                .description("Handles billing and payment inquiries")
                .model("gemini-1.5-flash")
                .instruction(
                    "You are a billing specialist. Handle queries about payments, balances, and invoices. " +
                    "Always confirm the customer's ID before processing transactions. " +
                    "After successful payments, provide the new balance and transaction ID."
                )
                .tools(
                        FunctionTool.create(customerSupportAgent, "getCustomerAccount"),
                        FunctionTool.create(customerSupportAgent, "processPayment"),
                        FunctionTool.create(customerSupportAgent, "getTickets")
                )
                .build();
    }

    private BaseAgent createTechnicalSupportAgent() {
        return LlmAgent.builder()
                .name("technical-support-agent")
                .description("Handles technical issues and troubleshooting")
                .model("gemini-1.5-flash")
                .instruction(
                    "You are a technical support specialist. Troubleshoot technical issues. " +
                    "If the issue cannot be immediately resolved, create a detailed support ticket. " +
                    "Inform the customer of the ticket ID and expected response time."
                )
                .tools(
                        FunctionTool.create(customerSupportAgent, "getCustomerAccount"),
                        FunctionTool.create(customerSupportAgent, "createTicket"),
                        FunctionTool.create(customerSupportAgent, "getTickets")
                )
                .build();
    }

    private BaseAgent createAccountAgent() {
        return LlmAgent.builder()
                .name("account-agent")
                .description("Manages account settings and profile updates")
                .model("gemini-1.5-flash")
                .instruction(
                    "You are an account management specialist. Handle changes to email, tier status, " +
                    "and general profile settings. Only update settings if the customer explicitly " +
                    "provides the new value. Send confirmation of all updates."
                )
                .tools(
                        FunctionTool.create(customerSupportAgent, "getCustomerAccount"),
                        FunctionTool.create(customerSupportAgent, "updateAccountSettings")
                )
                .build();
    }

    /**
     * Sequential workflow for refund processing
     * Step 1: Validate eligibility
     * Step 2: Process refund (only if eligible)
     */
    private BaseAgent createRefundWorkflow() {
        LlmAgent validator = LlmAgent.builder()
                .name("refund-validator")
                .description("Validates refund eligibility")
                .model("gemini-1.5-flash")
                .instruction(
                    "Validate refund requests by calling validateRefundEligibility. " +
                    "Store the result in ToolContext. If not eligible, explain the reasons. " +
                    "If eligible, proceed to the next step."
                )
                .tools(FunctionTool.create(customerSupportAgent, "validateRefundEligibility"))
                .outputKey("validation_result")
                .build();

        LlmAgent processor = LlmAgent.builder()
                .name("refund-processor")
                .description("Processes approved refunds")
                .model("gemini-1.5-flash")
                .instruction(
                    "Process approved refunds by checking ToolContext for 'refund_eligible' flag. " +
                    "If eligible, call processRefund. Explain refund processing times (5-7 business days)."
                )
                .tools(FunctionTool.create(customerSupportAgent, "processRefund"))
                .build();

        return SequentialAgent.builder()
                .name("refund-processor-workflow")
                .description("Two-step sequential workflow for refund processing: validation then processing")
                .subAgents(validator, processor)
                .build();
    }
}
