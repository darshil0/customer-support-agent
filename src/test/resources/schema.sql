CREATE TABLE customers (
    customer_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    tier VARCHAR(50) NOT NULL,
    balance DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    account_created TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE tickets (
    ticket_id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL REFERENCES customers(customer_id),
    subject VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created TIMESTAMP NOT NULL
);

-- Seed mock data
INSERT INTO customers (customer_id, name, email, tier, balance, account_created, status)
VALUES ('CUST001', 'John Doe', 'john.doe@example.com', 'Premium', 1250.00, DATEADD('DAY', -45, CURRENT_TIMESTAMP), 'active'),
       ('CUST002', 'Jane Smith', 'jane.smith@example.com', 'Basic', 0.00, DATEADD('DAY', -5, CURRENT_TIMESTAMP), 'active'),
       ('CUST003', 'Bob Johnson', 'bob.johnson@example.com', 'Enterprise', 5000.00, DATEADD('DAY', -10, CURRENT_TIMESTAMP), 'active');
