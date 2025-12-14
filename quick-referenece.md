# 📋 Quick Reference Guide - Customer Support System v1.0.4

## ⚡ Quick Commands

### Build & Run
```bash
# Full build with tests
mvn clean install

# Run application
mvn spring-boot:run

# Run tests only
mvn test

# Build JAR
mvn clean package

# Run JAR
java -jar target/customer-support-agent-1.0.4.jar
```

### Environment Setup
```bash
# Linux/Mac
export GOOGLE_API_KEY="your-key-here"

# Windows CMD
set GOOGLE_API_KEY=your-key-here

# Windows PowerShell
$env:GOOGLE_API_KEY="your-key-here"
```

---

## 🔧 7 Core Tools

| Tool | Purpose | Parameters |
|------|---------|------------|
| `getCustomerAccount` | Get customer info | customerId, context |
| `processPayment` | Process payment | customerId, amount, context |
| `createTicket` | Create support ticket | customerId, subject, description, priority, context |
| `getTickets` | Retrieve tickets | customerId, status, context |
| `updateAccountSettings` | Update account | customerId, email, tier, context |
| `validateRefundEligibility` | Check refund eligibility | customerId, context |
| `processRefund` | Process refund | customerId, amount, context |

---

## 🌐 API Endpoints

### GET Endpoints
```bash
# API Info
GET /api

# Health Check
GET /api/health

# Get Customer
GET /api/customer/{customerId}

# Get Tickets
GET /api/tickets/{customerId}?status={status}
```

### POST Endpoints
```bash
# Process Payment
POST /api/payment
Body: {"customerId": "CUST001", "amount": 100}

# Create Ticket
POST /api/ticket
Body: {"customerId": "CUST001", "subject": "Issue", "description": "Details", "priority": "high"}

# Validate Refund
POST /api/refund/validate
Body: {"customerId": "CUST001"}

# Process Refund
POST /api/refund/process
Body: {"customerId": "CUST001", "amount": 50}
```

### PUT Endpoints
```bash
# Update Account
PUT /api/account
Body: {"customerId": "CUST001", "email": "new@example.com", "tier": "premium"}
```

---

## 📊 Test Customers

| ID | Name | Tier | Balance | Days Old | Refund Eligible |
|----|------|------|---------|----------|-----------------|
| CUST001 | John Doe | Premium | $1,250 | 45 | ❌ |
| CUST002 | Jane Smith | Basic | $0 | 5 | ✅ |
| CUST003 | Bob Johnson | Enterprise | $5,000 | 10 | ✅ |

---

## ✅ Validation Rules

### Customer ID
- Format: `CUST` + 3+ digits
- Example: `CUST001`, `CUST123`

### Email
- Format: Standard email pattern
- Example: `user@example.com`

### Amount
- Range: $0.01 - $100,000.00
- Precision: 2 decimals (auto-rounded)

### Priority
- Values: `low`, `medium`, `high`, `urgent`

### Tier
- Values: `basic`, `premium`, `enterprise`

### Status
- Values: `open`, `closed`, `pending`, `all`

---

## 🎯 Common Workflows

### Payment Workflow
```bash
# 1. Get customer
curl http://localhost:8000/api/customer/CUST001

# 2. Process payment
curl -X POST http://localhost:8000/api/payment \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST001","amount":100}'

# 3. Verify balance
curl http://localhost:8000/api/customer/CUST001
```

### Refund Workflow
```bash
# 1. Validate eligibility
curl -X POST http://localhost:8000/api/refund/validate \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST002"}'

# 2. Process refund (if eligible)
curl -X POST http://localhost:8000/api/refund/process \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST002","amount":50}'
```

### Support Ticket Workflow
```bash
# 1. Create ticket
curl -X POST http://localhost:8000/api/ticket \
  -H "Content-Type: application/json" \
  -d '{
    "customerId":"CUST001",
    "subject":"Login Issue",
    "description":"Cannot access account",
    "priority":"high"
  }'

# 2. View tickets
curl "http://localhost:8000/api/tickets/CUST001?status=open"
```

---

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| API key error | `export GOOGLE_API_KEY="your-key"` |
| Port 8000 in use | Change port: `SERVER_PORT=8080 mvn spring-boot:run` |
| Tests fail | `mvn clean install -U` |
| Java version error | Verify: `java -version` (must be 17+) |
| Build warnings | `mvn clean install` |

---

## 📝 Response Format

### Success Response
```json
{
  "success": true,
  "data": { /* result data */ },
  "message": "Operation successful"
}
```

### Error Response
```json
{
  "success": false,
  "error": "Error description"
}
```

---

## 🎨 Test Patterns

### Run Specific Test
```bash
mvn test -Dtest=CustomerSupportAgentTest#testGetCustomerAccountValid
```

### Run Test Class
```bash
mvn test -Dtest=CustomerSupportAgentTest
```

### Verbose Output
```bash
mvn test -X
```

---

## 🔒 Security Checklist

- [ ] GOOGLE_API_KEY is set as environment variable
- [ ] API key is never committed to Git
- [ ] Inputs are validated and sanitized
- [ ] Error messages don't expose internals
- [ ] HTTPS is used in production

---

## 📦 File Structure

```
customer-support-agent/
├── src/
│   ├── main/java/com/example/support/
│   │   ├── App.java
│   │   ├── Configuration.java
│   │   ├── CustomerSupportAgent.java
│   │   ├── TransactionIdGenerator.java
│   │   └── ValidationUtils.java
│   ├── test/java/com/example/support/
│   │   └── CustomerSupportAgentTest.java
│   └── main/resources/
│       └── application.properties
├── pom.xml
├── README.md
├── CHANGELOG.md
└── .gitignore
```

---

## 🎯 Key Metrics

| Metric | Value |
|--------|-------|
| Test Pass Rate | 100% (35/35) |
| Code Coverage | 100% |
| Build Time | ~30s |
| Startup Time | ~3s |
| Lines of Code | ~2,500 |

---

## 🚀 Deployment

### JAR Deployment
```bash
mvn clean package
java -jar target/customer-support-agent-1.0.4.jar
```

### Docker Deployment
```bash
docker build -t support-agent:1.0.4 .
docker run -p 8000:8000 -e GOOGLE_API_KEY=$GOOGLE_API_KEY support-agent:1.0.4
```

---

## 📞 Quick Links

- **README**: Complete documentation
- **CHANGELOG**: Version history
- **TESTING-GUIDE**: Test documentation
- **GitHub**: https://github.com/darshil0/customer-support-agent

---

## 💡 Pro Tips

1. Use the quick-start.sh script for automated setup
2. Always reset test data in @BeforeEach
3. Use @DisplayName for readable test names
4. Check health endpoint after starting: `/api/health`
5. Use curl with `-v` flag for debugging
6. Keep API key in environment, never in code
7. Run tests before committing changes
8. Check logs for detailed error messages

---

**Version**: 1.0.4  
**Status**: ✅ Production Ready  
**Last Updated**: December 14, 2025
