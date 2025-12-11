# Customer Support Multi-Agent System
A production-ready, intelligent customer support system built with **Google Agent Development Kit (ADK) for Java**, demonstrating enterprise-grade best practices for multi-agent orchestration.

## 🏗️ Architecture

This system implements a **hierarchical multi-agent architecture** following ADK best practices:

```
Root Orchestrator (LlmAgent)
├── Billing Agent (LlmAgent)
│   ├── getCustomerAccount
│   ├── processPayment
│   └── getTickets
├── Technical Support Agent (LlmAgent)
│   ├── getCustomerAccount
│   ├── createTicket
│   ├── getTickets
│   └── GoogleSearch
├── Account Agent (LlmAgent)
│   ├── getCustomerAccount
│   └── updateAccountSettings
└── Refund Processor (SequentialAgent)
    ├── Refund Validator (LlmAgent)
    │   └── validateRefundEligibility
    └── Refund Processor (LlmAgent)
        └── processRefund
```

## ✨ Key Features

### 🎯 Multi-Agent Patterns

- **Orchestrator Pattern**: Root agent intelligently routes requests to specialized sub-agents
- **Sequential Workflow**: Refund processing follows a structured validation → processing flow
- **Tool Specialization**: Each agent has access only to tools relevant to its domain
- **State Management**: Shared state enables seamless data flow between agents

### 🛡️ Safety & Quality

- **Content Safety Filter**: Blocks requests containing sensitive information (passwords, credit cards, SSN)
- **Logging Callback**: Comprehensive audit trail of all agent interactions
- **Caching Mechanism**: Intelligent caching of expensive operations to improve performance
- **Error Handling**: Robust error handling with detailed logging

### 🔧 Custom Tools

Six production-ready tools demonstrating best practices:

1. **getCustomerAccount**: Retrieve customer information with state storage
2. **processPayment**: Secure payment processing with validation
3. **createTicket**: Support ticket creation with auto-generated IDs
4. **getTickets**: Query tickets with status filtering
5. **updateAccountSettings**: Account modification with confirmation
6. **validateRefundEligibility**: Business logic for refund rules
7. **processRefund**: Secure refund processing with validation checks

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **Google API Key** with Gemini access

## 🚀 Quick Start

### 1. Set Up Environment

```bash
# Clone or create project directory
mkdir customer-support-agent
cd customer-support-agent

# Set your Google API key
export GOOGLE_API_KEY="your-api-key-here"

# On Windows:
# set GOOGLE_API_KEY=your-api-key-here
```

### 2. Project Structure

Create this structure:

```
customer-support-agent/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── com/
                └── example/
                    └── support/
                        ├── CustomerSupportAgent.java
                        └── App.java
```

### 3. Build the Project

```bash
# Compile the project
mvn clean compile

# Run tests (if you've added them)
mvn test

# Create deployable JAR
mvn package
```

### 4. Run the Agent

#### Option A: Interactive CLI Mode

```bash
mvn exec:java -Dexec.mainClass="com.example.support.App"
```

#### Option B: Web UI Mode (Recommended for Development)

```bash
mvn exec:java
```

Then open http://localhost:8000 in your browser.

## 💬 Usage Examples

### Basic Customer Query

```
You: I'm customer CUST001, what's my account balance?

Agent: Let me look up your account information...
[Tool: getCustomerAccount]
Your current account balance is $1,250.00. Your account is in good standing 
with premium tier status.
```

### Payment Processing

```
You: I need to process a payment of $100 for customer CUST002

Agent: I'll transfer you to our billing specialist...
[Delegating to billing-agent]
I can process that payment. Let me confirm the details:
- Customer: CUST002 (Jane Smith)
- Amount: $100.00
- Current Balance: $0.00

[Tool: processPayment]
Payment processed successfully! 
Transaction ID: TXN-A3F7B92C
New balance: $100.00
```

### Refund Request (Sequential Workflow)

```
You: I'd like to request a refund for customer CUST001

Agent: I'll initiate the refund process for you...
[Delegating to refund-processor-workflow]

Step 1: Validating eligibility...
[Tool: validateRefundEligibility]
✓ Customer is eligible (Premium tier)
✓ Payment made 15 days ago

Step 2: Processing refund...
How much would you like to refund?
```

### Technical Support

```
You: Customer CUST002 is having trouble logging in to the dashboard

Agent: Let me connect you with our technical support team...
[Delegating to technical-support-agent]

I can help with that. Let me gather some information:
- What error message are they seeing?
- Which browser are they using?

I'll create a support ticket to track this issue.
[Tool: createTicket]
Ticket TKT-9B3C4D1E created. Engineering will investigate within 2-4 hours.
```

## 🧪 Testing

### CLI Commands

While in CLI mode, you can use these special commands:

- `help` - Display available commands and examples
- `state` - Show current session state and variables
- `clear` - Clear session and start fresh
- `exit` or `quit` - Exit the application

### Test Customer IDs

The system comes with three pre-configured test customers:

- **CUST001**: John Doe (Premium tier, $1,250 balance)
- **CUST002**: Jane Smith (Basic tier, $0 balance)
- **CUST003**: Bob Wilson (Enterprise tier, $5,000 balance)

## 🏛️ Best Practices Demonstrated

### 1. Agent Design

✅ **Specialized Agents**: Each agent has a clear, focused responsibility
✅ **Hierarchical Structure**: Root orchestrator manages specialist delegation
✅ **Context Preservation**: State flows seamlessly between agents
✅ **Clear Instructions**: Each agent has explicit, well-defined system prompts

### 2. Tool Development

✅ **Type Safety**: All parameters use Java type hints
✅ **Documentation**: Comprehensive `@Schema` annotations for LLM understanding
✅ **State Management**: Tools use `ToolContext` for state access and modification
✅ **Error Handling**: Robust try-catch with meaningful error messages
✅ **Idempotency**: Tools can be safely called multiple times

### 3. Callbacks

✅ **Safety First**: `beforeModelCallback` filters sensitive content
✅ **Observability**: `afterAgentCallback` logs all interactions
✅ **Performance**: `beforeToolCallback` implements caching
✅ **Separation of Concerns**: Callbacks handle cross-cutting concerns

### 4. Sequential Workflows

✅ **SequentialAgent**: Used for multi-step processes requiring order
✅ **outputKey**: Enables clean data passing between steps
✅ **Validation Before Action**: Refund validation before processing

### 5. Production Readiness

✅ **Structured Logging**: All operations logged with context
✅ **Transaction IDs**: Unique identifiers for all operations
✅ **Input Validation**: Comprehensive validation of all inputs
✅ **Mock Data Layer**: Easy to replace with real database

## 🚢 Deployment Options

### Local Development

```bash
# Use the built-in dev server
mvn exec:java
```

### Docker Container

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/customer-support-agent-1.0.0.jar app.jar
ENV GOOGLE_API_KEY=""
EXPOSE 8000
CMD ["java", "-jar", "app.jar"]
```

### Google Cloud Run

```bash
# Build and deploy
gcloud run deploy customer-support-agent \
  --source . \
  --platform managed \
  --region us-central1 \
  --set-env-vars GOOGLE_API_KEY=${GOOGLE_API_KEY}
```

### Vertex AI Agent Engine

The agent is compatible with Vertex AI Agent Engine Runtime for fully managed hosting with automatic scaling.

## 📊 Monitoring & Observability

### Built-in Logging

All operations are logged with:
- Timestamp
- Agent name
- Invocation ID
- Session ID
- Tool calls and parameters
- State changes

Example log output:

```
[2025-12-10 14:23:15] Agent: billing-agent | Invocation: inv_abc123 | Session: sess_xyz789
State keys: [current_customer, last_transaction_id]
[TOOL] getCustomerAccount(CUST001) - Invocation: inv_abc123
[CACHE-MISS] cache:getCustomerAccount:{customerId=CUST001} - Tool: getCustomerAccount
[SECURITY] PASSED: No sensitive keywords detected - Agent: billing-agent
```

### Production Monitoring

For production deployments, integrate with:
- **Google Cloud Logging**: Structured log aggregation
- **Cloud Trace**: Distributed tracing
- **Cloud Monitoring**: Metrics and alerting
- **Error Reporting**: Automatic error detection

## 🔒 Security Considerations

### Implemented Safeguards

- ✅ Input sanitization via content safety filter
- ✅ Sensitive keyword detection
- ✅ Transaction ID generation (prevents replay attacks)
- ✅ State isolation between sessions
- ✅ Tool access control (agents only get relevant tools)

### Production Recommendations

- [ ] Add authentication and authorization
- [ ] Implement rate limiting
- [ ] Use secrets manager for API keys
- [ ] Add PII detection and redaction
- [ ] Enable audit logging to persistent storage
- [ ] Implement HTTPS/TLS
- [ ] Add request signing

## 🧩 Extension Ideas

### Add New Agents

```java
private static BaseAgent createAnalyticsAgent() {
    return LlmAgent.builder()
        .name("analytics-agent")
        .description("Provides customer insights and analytics")
        .model("gemini-2.0-flash")
        .instruction("Analyze customer data and provide insights...")
        .tools(/* custom analytics tools */)
        .build();
}
```

### Add New Tools

```java
@Schema(description = "Send email notification to customer")
public static Map<String, Object> sendEmail(
    @Schema(description = "Recipient email") String toEmail,
    @Schema(description = "Email subject") String subject,
    @Schema(description = "Email body") String body,
    ToolContext toolContext
) {
    // Implementation
}
```

### Add Parallel Execution

```java
private static BaseAgent createParallelValidator() {
    return ParallelAgent.builder()
        .name("parallel-validator")
        .subAgents(
            creditCheckAgent,
            fraudCheckAgent,
            inventoryCheckAgent
        )
        .build();
}
```

## 📚 Resources

- **ADK Documentation**: https://google.github.io/adk-docs/
- **Java ADK GitHub**: https://github.com/google/adk-java
- **Sample Agents**: https://github.com/google/adk-samples
- **Codelabs**: https://codelabs.developers.google.com/adk-java-getting-started

## 🤝 Contributing

To extend this system:

1. Add new tools in `CustomerSupportAgent.java`
2. Create specialized agents for new domains
3. Implement new callback patterns for your needs
4. Add comprehensive logging
5. Write unit tests for tools and callbacks

## 📝 License

This example code is provided as-is for educational purposes.

## 💡 Credits

Built following best practices from:
- Google ADK Documentation
- ADK Community Examples
- Enterprise agent design patterns

---

**Built with ❤️ using Google Agent Development Kit for Java by Darshil**

## V1.0.0

- Refactored the agent creation logic to improve code clarity and maintainability.
- Added robust error handling, input validation, and logging to all tool methods.
- Added comprehensive unit tests for all tool methods.
- Formatted the code to ensure consistency and adherence to best practices.
- Updated the README.md file to reflect the new changes and provide accurate documentation for the project.
