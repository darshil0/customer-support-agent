package com.example.support;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Utility class for centralizing and standardizing all tool input validation logic.
 */
public class ValidationUtils {

    /**
     * Validates and normalizes the customer ID.
     */
    public static String validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be empty");
        }

        String normalized = customerId.trim().toUpperCase();

        // Expect format like CUST001, CUST1000, etc.
        if (!normalized.matches("CUST\\d{3,}")) {
            throw new IllegalArgumentException(
                "Invalid customer ID format. Expected format: CUST### (e.g., CUST001)"
            );
        }

        // Note: Actual existence check must happen in the tool where mockDatabase is accessible.
        
        return normalized;
    }

    /**
     * Validates and converts the amount to a double, with sanity checks.
     */
    public static double validateAmount(Object amountObj) {
        double amount;

        if (amountObj instanceof Number) {
            amount = ((Number) amountObj).doubleValue();
        } else if (amountObj instanceof String) {
            try {
                amount = Double.parseDouble((String) amountObj);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    "Invalid amount format: " + amountObj + ". Must be a number."
                );
            }
        } else {
            throw new IllegalArgumentException(
                "Amount must be a number or numeric string"
            );
        }

        if (amount <= 0) {
            throw new IllegalArgumentException(
                "Amount must be greater than zero"
            );
        }

        if (amount > 100000) {
            throw new IllegalArgumentException(
                "Amount exceeds maximum limit of $100,000"
            );
        }

        // Round to 2 decimals for financial precision
        return Math.round(amount * 100.0) / 100.0;
    }
    
    /**
     * Validates and normalizes a ticket priority.
     */
    public static String validatePriority(String priority) {
        String[] validPriorities = {"LOW", "MEDIUM", "HIGH"};
        String normalized = Optional.ofNullable(priority)
            .orElse("MEDIUM") // Default
            .trim()
            .toUpperCase();
        
        for (String valid : validPriorities) {
            if (valid.equals(normalized)) {
                return normalized;
            }
        }
        
        return "MEDIUM"; // Default to MEDIUM if provided priority is invalid
    }
}
