package com.example.support;

/**
 * Utility class for common data validation routines
 *
 * @author Darshil
 * @version 1.0.2 (Fixed)
 */
public class ValidationUtils {

  private static final String CUSTOMER_ID_REGEX = "CUST\\d{3}";
  private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
  private static final String[] VALID_TIERS = {"BASIC", "PREMIUM", "ENTERPRISE"};
  private static final String[] VALID_PRIORITIES = {"LOW", "MEDIUM", "HIGH"};
  private static final double MAX_TRANSACTION_AMOUNT = 100000.00; // $100,000 limit

  /**
   * Validates and normalizes customer ID format
   *
   * @param customerId The customer ID to validate
   * @return Normalized customer ID (uppercase)
   * @throws IllegalArgumentException if invalid
   */
  public static String validateCustomerId(String customerId) {
    if (customerId == null || customerId.trim().isEmpty()) {
      throw new IllegalArgumentException("Customer ID is required");
    }
    String id = customerId.trim().toUpperCase();
    if (!id.matches(CUSTOMER_ID_REGEX)) {
      throw new IllegalArgumentException(
          "Invalid customer ID format. Must be CUST followed by three digits (e.g., CUST001)");
    }
    return id;
  }

  /**
   * Validates amount for financial transactions
   *
   * @param amountObj The amount object (can be String or Number)
   * @return Validated amount rounded to 2 decimal places
   * @throws IllegalArgumentException if invalid
   */
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
      throw new IllegalArgumentException("Invalid amount format: " + amountObj);
    }

    if (amount <= 0) {
      throw new IllegalArgumentException("Amount must be greater than zero");
    }

    if (amount > MAX_TRANSACTION_AMOUNT) {
      throw new IllegalArgumentException(
          "Amount exceeds maximum transaction limit of $" + MAX_TRANSACTION_AMOUNT);
    }

    // Round to two decimal places
    return Math.round(amount * 100.0) / 100.0;
  }

  /**
   * Validates email address format
   *
   * @param email The email to validate
   * @return Trimmed and validated email
   * @throws IllegalArgumentException if invalid
   */
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

  /**
   * Validates account tier
   *
   * @param tier The tier to validate
   * @return Properly capitalized tier (e.g., "Premium")
   * @throws IllegalArgumentException if invalid
   */
  public static String validateTier(String tier) {
    if (tier == null || tier.trim().isEmpty()) {
      throw new IllegalArgumentException("Account tier cannot be empty");
    }

    String normalizedTier = tier.trim().toUpperCase();
    for (String validTier : VALID_TIERS) {
      if (validTier.equals(normalizedTier)) {
        // Return capitalized version (e.g., "Premium")
        return normalizedTier.charAt(0) + normalizedTier.substring(1).toLowerCase();
      }
    }

    throw new IllegalArgumentException(
        "Invalid tier. Must be one of: Basic, Premium, or Enterprise");
  }

  /**
   * Validates ticket priority, with default fallback
   *
   * @param priority The priority to validate (can be null)
   * @return Validated priority or "MEDIUM" as default
   */
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

    // Return default if invalid (graceful handling)
    return defaultPriority;
  }
}
