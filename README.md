# Customer Support Multi-Agent System

**Enterprise-grade Java 17 / Spring Boot 3 solution** for intelligent customer support routing, powered by Google's Generative AI Development Kit (ADK) and Gemini models. Includes PostgreSQL persistence, GraphQL API, real-time WebSockets, production safety guardrails, and React frontend dashboard.

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/darshil0/customer-support-agent)
[![Test Coverage](https://img.shields.io/badge/coverage-100%25-brightgreen)](https://github.com/darshil0/customer-support-agent)
[![Version](https://img.shields.io/badge/version-1.2.1-blue)](https://github.com/darshil0/customer-support-agent)
[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/license-Apache%202.0-green)](LICENSE)

---

## Overview

This system implements a hierarchical multi-agent architecture built with Google ADK for Java:

- **Customer Support Orchestrator** — Root agent routing inquiries to specialized sub-agents.
- **Billing Agent** — Payment processing, balance lookup, and invoice support.
- **Technical Support Agent** — Technical troubleshooting and SLA-driven ticket creation.
- **Account Agent** — Profile modifications and tier management.
- **Refund Processor Workflow** — Sequential two-step workflow (Validate → Process) with compliance controls.

**All 50 Java backend test methods and 15 React frontend test methods pass cleanly**, ensuring enterprise readiness.

---

## Architecture & Agent System Safety

### Hierarchical Agent Structure
```
rootCustomerSupportAgent (customer-support-orchestrator)
├── billing-agent
├── technical-support-agent
├── account-agent
└── refund-processor-workflow (SequentialAgent)
    ├── refund-validator
    └── refund-processor
```

### Safety & Guardrail Features
1. **Untrusted Inputs**: All customer messages, tool parameters, and context values are treated as untrusted data.
2. **Anti-Prompt Injection**: Prompts sanitize input HTML/script tags and instruct models to reject administrative impersonation or prompt override attempts.
3. **No Hallucinated Data**: Agents operate only on facts returned by tools; balance/ticket details are never invented.
4. **Tenant Boundary Protection**: Operations validate customer ID scope to prevent cross-customer data access.
5. **Irreversible Action Confirmation**: Side-effect operations (e.g., payments and refunds) enforce validation and confirmation.

---

## Quick Start Guide

### Prerequisites
- **Java 17+** (`java -version`)
- **Maven 3.8+** (`mvn -version`)
- **Node.js 18+ & npm** (`npm -version`)
- **Google API Key** (Gemini API access)

### 1. Configuration
Copy `.env.example` to configure environment variables:

```bash
cp .env.example .env
export GOOGLE_API_KEY="your-gemini-api-key"
```

### 2. Build & Test
```bash
# Build & run backend test suite (50 tests)
mvn clean test

# Install & run frontend test suite (15 tests)
npm install --legacy-peer-deps
npm test -- --watch=false
```

### 3. Run Application
```bash
# Start backend API (port 8000)
mvn spring-boot:run

# In another terminal: Start frontend dev server (port 3000)
npm run dev
```

---

## Core Tools & Endpoints

### Core Backend Tools (7 Total)
1. **`getCustomerAccount`** — Retrieve and cache customer profile details.
2. **`processPayment`** — Process payments with round/limit validations.
3. **`createTicket`** — Create support ticket with priority validation.
4. **`getTickets`** — Retrieve tickets filtered by status.
5. **`updateAccountSettings`** — Update email and tier status.
6. **`validateRefundEligibility`** — Verify 30-day window and account status.
7. **`processRefund`** — Execute refunds after validation check.

### REST Endpoints
- `GET /api/health` — Service health check.
- `GET /api/customer/{customerId}` — Fetch account details.
- `PUT /api/account` — Update profile/tier.
- `POST /api/payment` — Process payment.
- `POST /api/ticket` — Create support ticket.
- `GET /api/tickets/{customerId}` — List customer tickets.
- `POST /api/refund/validate` — Validate refund eligibility.
- `POST /api/refund/process` — Execute refund.

---

## Docker Support

### Build & Run Docker Container
```bash
# Build multi-stage image
docker build -t customer-support-agent:1.2.1 .

# Run container
docker run -p 8000:8000 -e GOOGLE_API_KEY="your-key" customer-support-agent:1.2.1
```

---

## License

Apache License 2.0 — See [LICENSE](LICENSE) for details.
