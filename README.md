# 🚀 Customer Support Multi-Agent System

An intelligent customer support solution built with the **Google Agent Development Kit (ADK) for Java**, showcasing enterprise-grade multi-agent orchestration and robust tooling.

## ✨ Features

- **Hierarchical Multi-Agent Architecture**: A `Root Orchestrator` delegates tasks to specialized sub-agents for `Billing`, `Tech Support`, and `Account` management.
- **Sequential Workflows**: Complex, multi-step processes like refunds are handled gracefully using sequential agents to ensure proper validation.
- **Comprehensive Tooling**: A rich set of tools for managing customer accounts, processing payments, handling support tickets, and more.
- **Robust Error Handling**: Centralized validation and structured error handling for reliable operation.
- **Test-Driven**: Includes 39 unit tests for complete coverage of all agent functionalities.

## 📋 Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **Google API Key** with Gemini access

## 🚀 Quick Start

1.  **Set Up Environment**:
    Set your API key as an environment variable (required by `Configuration.java`):
    ```bash
    # Linux/Mac
    export GOOGLE_API_KEY="your-api-key-here"

    # Windows (PowerShell)
    $env:GOOGLE_API_KEY="your-api-key-here"
    ```

2.  **Build and Test**:
    The project is pre-configured with comprehensive unit tests. Run the following commands to build the project and verify its integrity:
    ```bash
    # Build the project and download all dependencies
    mvn clean install

    # Run all 39 unit tests (Expected: PASS)
    mvn test
    ```

3.  **Run the Agent**:
    Run the agent in the recommended **Web UI Mode** via Spring Boot:
    ```bash
    mvn spring-boot:run
    ```
    Then, open **http://localhost:8000** in your browser to start chatting.

## 🛠️ Implemented Tools

| Tool Name                 | Agent Owner   | Purpose                                                  |
| :------------------------ | :------------ | :------------------------------------------------------- |
| `getCustomerAccount`      | All           | Retrieve customer details (includes caching).            |
| `processPayment`          | Billing       | Securely update balance and generate a transaction ID.   |
| `createTicket`            | Tech Support  | Create a new ticket with an auto-generated ID and priority. |
| `getTickets`              | Tech Support  | Query existing tickets by customer and status filter.    |
| `updateAccountSettings`   | Account       | Update email or tier with validation.                    |
| `validateRefundEligibility` | Refund (Step 1) | Validate refund eligibility (checks 30-day window).      |
| `processRefund`           | Refund (Step 2) | Process a refund only if validation has passed.          |

---

## 🏛️ Deployment & Extensibility

### 🚢 Deployment Options

The system is configured for standard deployment via JAR or containerization:

- **Local JAR**: `java -jar target/customer-support-agent-1.0.2.jar`
- **Docker Container**: Use the provided `Dockerfile` template.
- **Google Cloud Run**: Compatible with serverless deployment using `gcloud run deploy`.

### ⚙️ Extensibility (ADK Best Practices)

| Pattern              | Example                                                      | Benefit                                          |
| :------------------- | :----------------------------------------------------------- | :----------------------------------------------- |
| **Parallel Agent**   | Add a `ParallelAgent` to run credit and fraud checks simultaneously. | Speed up multi-step validation.                  |
| **Custom Callbacks** | Implement a callback for Cloud Trace integration.            | Enhance observability.                           |
| **Agent Specialization** | Introduce a `Marketing Agent` with promotional tools.        | Extend capabilities without altering core logic. |

---

## 📁 Project Structure

The core logic is defined in `CustomerSupportAgent.java` (tool implementations) and the agent orchestration is configured in `AgentConfiguration.java` (agent hierarchy).

```
customer-support-agent/
├── src/
│   ├── main/
│   │   ├── java/com/example/support/
│   │   │   ├── App.java                      # Spring Boot entry point
│   │   │   ├── Configuration.java            # API key management
│   │   │   ├── CustomerSupportAgent.java     # Tool implementations
│   │   │   ├── AgentConfiguration.java       # Agent hierarchy
│   │   │   ├── TransactionIdGenerator.java   # ID generation
│   │   │   └── ValidationUtils.java          # Input validation
│   │   └── resources/
│   │       ├── application.properties        # Server config
│   │       └── logback.xml                   # Logging config
│   └── test/
│       └── java/com/example/support/
│           └── CustomerSupportAgentTest.java # Comprehensive tests
├── pom.xml                                   # Maven dependencies
├── README.md                                 # This file
├── CHANGELOG.md                              # Changelog
└── LICENSE                                   # Apache 2.0 License
```

---

## 📚 Resources

- **Google ADK Docs**: https://google.github.io/adk-docs/
- **ADK Java GitHub**: https://github.com/google/adk-java
- **Spring Boot Docs**: https://docs.spring.io/spring-boot/docs/current/reference/html/

---

**Built with ❤️ using Google Agent Development Kit for Java by Darshil**

**Version 1.0.2 - Under Development**
