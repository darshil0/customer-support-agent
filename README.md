# 🚀 Customer Support Multi-Agent System

An intelligent customer support solution built with **Google Agent Development Kit (ADK) for Java**, showcasing enterprise-grade multi-agent orchestration and robust tooling.

## ⭐ Executive Summary & Quick Start

### 🎯 Architecture Overview

The system uses a **hierarchical multi-agent architecture** where a Root Orchestrator delegates tasks to specialized sub-agents (Billing, Tech Support, Account) and complex workflows. This structure is implemented by defining sub-agents as callable tools within the primary agent.

The core routing and tool declaration logic is defined in `CustomerSupportAgent.java` (tool implementations) and the agent orchestration setup is configured in `AgentConfiguration.java` (agent hierarchy).

### 📋 Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **Google API Key** with Gemini access

### 1. Set Up Environment

Set your API key as an environment variable (required by `Configuration.java`):

```bash
# Linux/Mac
export GOOGLE_API_KEY="your-api-key-here"

# Windows (PowerShell)
$env:GOOGLE_API_KEY="your-api-key-here"
```

### 2. Build and Test

The project has comprehensive **Unit Tests** (39 tests) in `CustomerSupportAgentTest.java`, covering routing, workflow logic, and tool functionality.

| Command | Purpose |
|:---|:---|
| `mvn clean install` | **Build** the project and download all dependencies |
| `mvn test` | **Run** all comprehensive unit tests (Expected: **PASS**) |
| `mvn package` | Create deployable JAR (`target/customer-support-agent-1.0.2.jar`) |

### 3. Run the Agent (Development)

Run in the recommended **Web UI Mode** via Spring Boot:

```bash
mvn spring-boot:run
```

Then open **http://localhost:8000** in your browser to start chatting.

---

## ✨ System Capabilities

### 🛡️ Core Reliability & Safety

| Feature | Description | Status |
|:---|:---|:---|
| **Input Validation** | Centralized, robust parameter checks via `ValidationUtils.java` | ✅ Implemented |
| **Error Handling** | Structured `try-catch` blocks return explicit, machine-readable errors | ✅ Implemented |
| **Transaction IDs** | Secure, traceable IDs generated via `TransactionIdGenerator.java` | ✅ Implemented |
| **Test Isolation** | Static mock data reset before every test run for strict isolation | ✅ Implemented |
| **Spring Integration** | Proper `@Component` annotation for dependency injection | ✅ Fixed |

### 🔧 Implemented Tools (`CustomerSupportAgent.java` Methods)

| Tool Name | Agent Owner | Purpose |
|:---|:---|:---|
| `getCustomerAccount` | All | Retrieve customer details (includes caching) |
| `processPayment` | Billing | Securely update balance and generate transaction ID |
| `createTicket` | Tech Support | Create new ticket with auto-generated ID and priority |
| `getTickets` | Tech Support | Query existing tickets by customer and status filter |
| `updateAccountSettings` | Account | Update email or tier with validation |
| `validateRefundEligibility` | Refund (Step 1) | Validate refund eligibility (checks 30-day window) |
| `processRefund` | Refund (Step 2) | Process refund only if validation passed |

---

## 💬 Usage Examples

| Scenario | Agent Used | Key Tool(s) |
|:---|:---|:---|
| **Check Balance** | Root → Billing | `getCustomerAccount` |
| **Process Payment** | Root → Billing | `processPayment` |
| **Create Ticket** | Root → Tech Support | `createTicket` |
| **Process Refund** | Root → Sequential Agent | `validateRefundEligibility` → `processRefund` |

### Example: Refund Request (Sequential Workflow)

The `Refund Processor` uses a `SequentialAgent` (configured in `AgentConfiguration.java`) to ensure **validation occurs before the payment action**.

1. **User**: "I'd like to request a refund for customer CUST002"
2. **Validator (LlmAgent)**: Calls `validateRefundEligibility` and writes `refund_eligible=true/false` to ToolContext
   - *Result*: Customer is eligible (payment made 5 days ago)
3. **Processor (LlmAgent)**: Reads validation context. Since eligibility is true, calls `processRefund`
   - *Result*: User receives confirmation with refund ID and 5-7 business day timeline

---

## 🏛️ Deployment & Extensibility

### 🚢 Deployment Options

The system is configured for standard deployment via JAR or containerization:

- **Local JAR**: `java -jar target/customer-support-agent-1.0.2.jar`
- **Docker Container**: Use provided `Dockerfile` template
- **Google Cloud Run**: Compatible with serverless deployment using `gcloud run deploy`

### ⚙️ Extensibility (ADK Best Practices)

| Pattern | Example | Benefit |
|:---|:---|:---|
| **Parallel Agent** | Add a `ParallelAgent` to run credit and fraud checks simultaneously | Speed up multi-step validation |
| **Custom Callbacks** | Implement a callback for Cloud Trace integration | Enhance observability |
| **Agent Specialization** | Introduce a `Marketing Agent` with promotional tools | Extend capabilities without altering core logic |

---

## 🔧 Version 1.0.2 - Under Development

### State Management and ADK API Fixes ✅

1. **State Management Restored**: Re-enabled `ToolContext` functionality that was previously commented out, restoring critical state management for multi-step agent workflows.
2. **ADK API Versioning**: Corrected the usage of the ADK `ToolContext` API to match the project's dependency version (0.3.0), resolving compilation errors.
3. **Test Suite Fixes**: Repaired and re-enabled the full test suite to properly mock the `ToolContext` and validate the agent's functionality.
4. **Documentation Cleanup**: Removed inaccurate "production-ready" claims and updated documentation to reflect the project's true state.

### Test Coverage

- **39 Unit Tests** covering all tool methods
- **100% Tool Coverage** for validation, payments, tickets, refunds
- **Test Isolation** with proper mock data reset between tests
- **Edge Case Testing** for null values, invalid formats, boundary conditions

### Build Verification

```bash
# Verify everything works
mvn clean install

# Expected output:
# [INFO] BUILD SUCCESS
# [INFO] Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
```

---

## 📁 Project Structure

```
customer-support-agent/
├── src/
│   ├── main/
│   │   ├── java/com/example/support/
│   │   │   ├── App.java                      # Spring Boot entry point
│   │   │   ├── Configuration.java             # API key management
│   │   │   ├── CustomerSupportAgent.java      # Tool implementations
│   │   │   ├── AgentConfiguration.java        # Agent hierarchy
│   │   │   ├── TransactionIdGenerator.java    # ID generation
│   │   │   └── ValidationUtils.java           # Input validation
│   │   └── resources/
│   │       ├── application.properties         # Server config
│   │       └── logback.xml                    # Logging config
│   └── test/
│       └── java/com/example/support/
│           └── CustomerSupportAgentTest.java  # Comprehensive tests
├── pom.xml                                    # Maven dependencies
├── README.md                                  # This file
├── CHANGELOG.md                               # Changelog
└── LICENSE                                    # Apache 2.0 License
```

---

## 📚 Resources

- **Google ADK Docs**: https://google.github.io/adk-docs/
- **ADK Java GitHub**: https://github.com/google/adk-java
- **Spring Boot Docs**: https://docs.spring.io/spring-boot/docs/current/reference/html/

---

## 🎓 Key Learnings & Best Practices

### What Was Fixed in v1.0.2

1. **Proper Dependency Injection**: Always use `@Component` for Spring-managed beans
2. **Test Separation**: Keep tests completely separate from implementation code
3. **State Management**: Static state requires explicit reset mechanisms for test isolation
4. **Complete APIs**: Ensure all referenced methods are implemented before compilation
5. **Consistent Formatting**: Maintain code style standards from the start

### Key Technical Features

- ✅ All tests passing (39 unit tests)
- ✅ Proper error handling in all tools
- ✅ Input validation for all parameters
- ✅ Thread-safe data structures (ConcurrentHashMap)
- ✅ Comprehensive logging
- ✅ Spring Boot integration working
- ✅ Foundational documentation in place

---

**Built with ❤️ using Google Agent Development Kit for Java by Darshil**

**Version 1.0.2 - Under Development**

---
