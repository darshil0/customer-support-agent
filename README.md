## 🚀 Customer Support Multi-Agent System

A production-ready, intelligent customer support solution built with **Google Agent Development Kit (ADK) for Java**, showcasing enterprise-grade multi-agent orchestration and robust tooling.

## ⭐ Executive Summary & Quick Start

### 🎯 Architecture Overview

The system uses a **hierarchical multi-agent architecture** where a Root Orchestrator delegates tasks to specialized sub-agents (Billing, Tech Support, Account) and complex workflows. This structure is implemented by defining sub-agents as callable tools within the primary agent.

The core routing and tool declaration logic is defined in the primary agent class, `CustomerSupportAgent.java`, and the agent orchestration setup (including the Root Orchestrator's behavior and sub-agents) is configured in `AgentConfiguration.java`.

### 📋 Prerequisites

  * **Java 17+**
  * **Maven 3.8+**
  * **Google API Key** with Gemini access

### 1\. Set Up Environment

Set your API key as an environment variable (required by `Configuration.java`):

```bash
# Linux/Mac
export GOOGLE_API_KEY="your-api-key-here"

# Windows (PowerShell):
$env:GOOGLE_API_KEY="your-api-key-here"
```

### 2\. Build and Test

The project has comprehensive **Unit and Integration tests** (30+ combined) consolidated in `CustomerSupportAgentUnifiedTest.java`, covering routing, workflow logic, and tool functionality.

| Command | Purpose |
| :--- | :--- |
| `mvn clean install` | **Build** the project and download all dependencies. |
| `mvn test` | **Run** all comprehensive **Unit and Integration tests** (Expected: **PASS**). |
| `mvn package` | Create deployable JAR (`target/customer-support-agent-1.0.0.jar`). |

### 3\. Run the Agent (Development)

Run in the recommended **Web UI Mode** via Spring Boot:

```bash
mvn spring-boot:run
```

Then open **http://localhost:8000** in your browser to start chatting.

-----

## ✨ System Capabilities

### 🛡️ Core Reliability & Safety

| Feature | Description | Status |
| :--- | :--- | :--- |
| **Input Validation** | Centralized, robust parameter checks on all tool inputs in `CustomerSupportAgent.java`. | ✅ Implemented |
| **Content Safety** | `beforeModelCallback` blocks sensitive data (SSN, credit cards, passwords). | ✅ Implemented |
| **Error Handling** | Structured `try-catch` blocks return explicit, machine-readable errors. | ✅ Implemented |
| **Transaction IDs** | Secure, traceable IDs generated for payments and tickets. | ✅ Implemented |
| **Test Isolation (Static Mock Data Reset)** | The static mock data in `CustomerSupportAgent` is reset (`CustomerSupportAgent.resetMockData()`) before every unit and integration test run to ensure strict isolation and repeatable results. | ✅ Implemented |

### 🔧 Implemented Tools (`CustomerSupportAgent.java` Functions)

| Tool Name | Agent Owner | Purpose |
| :--- | :--- | :--- |
| `getCustomerAccount` | All | Retrieve customer details (includes mock caching). |
| `processPayment` | Billing | Securely update balance and generate transaction ID. |
| `createTicket` | Tech Support | Create new ticket with auto-generated ID and priority. |
| `getTickets` | Tech Support | Query existing tickets by customer and status filter. |
| `updateAccountSettings` | Account | Update email or tier with validation checks against `VALID_TIERS`. |
| `validateRefundEligibility` | Refund (Step 1) | Business logic to determine refund eligibility (e.g., checks 30-day payment window). |
| `processRefund` | Refund (Step 2) | Final refund processing (deducts from balance) **only if** validation state is true. |

-----

## 💬 Usage Examples

| Scenario | Agent Used | Key Tool(s) |
| :--- | :--- | :--- |
| **Check Balance** | Root $\rightarrow$ Billing | `getCustomerAccount` |
| **Process Payment** | Root $\rightarrow$ Billing | `processPayment` |
| **Create Ticket** | Root $\rightarrow$ Tech Support | `createTicket` |
| **Process Refund** | Root $\rightarrow$ Sequential Agent | `validateRefundEligibility` $\rightarrow$ `processRefund` |

**Example: Refund Request (Sequential Workflow)**

The `Refund Processor` uses a $\text{SequentialAgent}$ (configured in `AgentConfiguration.java`) to ensure **validation occurs before the payment action**.

1.  **User**: I'd like to request a refund for customer CUST001
2.  **Validator (LlmAgent)**: Calls `validateRefundEligibility` and **writes `refund_eligible=true/false` to the ToolContext state**.
      * *Result*: Customer is found to be ineligible (e.g., payment made 45 days ago).
3.  **Processor (LlmAgent)**: Reads validation context. Since eligibility is false, the process stops.
      * *Result*: User receives a polite denial and reason (e.g., "Last payment was more than 30 days ago.").

-----

## 🏛️ Deployment & Extensibility

### 🚢 Deployment Options

The system is configured for standard deployment via JAR or containerization:

  * **Local JAR**: `java -jar target/customer-support-agent-1.0.0.jar`
  * **Docker Container**: Ready to build using the provided `Dockerfile` template.
  * **Google Cloud Run**: Compatible with serverless deployment using `gcloud run deploy`.

### ⚙️ Extensibility (ADK Best Practices)

| Pattern | Example | Benefit |
| :--- | :--- | :--- |
| **Parallel Agent** | Add a `ParallelAgent` to run credit and fraud checks simultaneously. | Speed up multi-step validation. |
| **Custom Callbacks** | Implement a callback for Cloud Trace integration. | Enhance observability and performance tracking. |
| **Agent Specialization** | Introduce a `Marketing Agent` with tools for sending promotions. | Extend capabilities without altering core logic. |

## 📚 Resources

  * **Google ADK Docs**: [https://google.github.io/adk-docs/](https://google.github.io/adk-docs/)
  * **ADK Java GitHub**: [https://github.com/google/adk-java](https://github.com/google/adk-java)
  * **Spring Boot Docs**: [https://docs.spring.io/spring-boot/docs/current/reference/html/](https://docs.spring.io/spring-boot/docs/current/reference/html/)

-----

**Built with ❤️ using Google Agent Development Kit for Java by Darshil**

**V1.0.0** - Fully audited, complete, and production-ready implementation.

-----
