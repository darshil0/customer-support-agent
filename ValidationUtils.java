package com.example.support;

/**
 * Utility class for common data validation routines.
 */
public class ValidationUtils {

    private static final String CUSTOMER_ID_REGEX = "CUST\\d{3}";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String[] VALID_TIERS = {"BASIC", "PREMIUM", "ENTERPRISE"};
    private static final String[] VALID_PRIORITIES = {"LOW", "MEDIUM", "HIGH"};
    
    public static String validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        String id = customerId.trim().toUpperCase();
        if (!id.matches(CUSTOMER_ID_REGEX)) {
            throw new IllegalArgumentException("Invalid customer ID format. Must be CUST followed by three digits (e.g., CUST001)");
        }
        return id;
    }

    public static double validateAmount(Object amountObj) {
        if (amountObj == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        double amount;
        try {
            if (amountObj instanceof String) {
                amount = Double.parseDouble((String) amountObj);
            } else if (amountObj instanceof Number) {
                amount = ((Number) amountObj).doubleValue();
            } else {
                throw new IllegalArgumentException("Amount must be a number");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format");
        }
        
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be a positive value");
        }
        // Round to two decimal places for basic financial integrity
        return Math.round(amount * 100.0) / 100.0;
    }

    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        String validatedEmail = email.trim();
        if (!validatedEmail.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        return validatedEmail;
    }

    public static String validateTier(String tier) {
        if (tier == null || tier.trim().isEmpty()) {
            throw new IllegalArgumentException("Account tier cannot be empty");
        }
        String normalizedTier = tier.trim().toUpperCase();
        for (String validTier : VALID_TIERS) {
            if (validTier.equals(normalizedTier)) {
                // Return capitalized version for display (e.g., Basic, Premium)
                return normalizedTier.charAt(0) + normalizedTier.substring(1).toLowerCase();
            }
        }
        throw new IllegalArgumentException("Invalid tier: Must be one of Basic, Premium, or Enterprise");
    }

    public static String validatePriority(String priority) {
        String defaultPriority = "MEDIUM";
        if (priority == null || priority.trim().isEmpty()) {
            return defaultPriority;
        }
        String normalizedPriority = priority.trim().toUpperCase();
        for (String validPriority : VALID_PRIORITIES) {
            if (validPriority.equals(normalizedPriority)) {
                return normalizedPriority;
            }
        }
        // Use default priority if the provided one is invalid
        return defaultPriority; 
    }
}
