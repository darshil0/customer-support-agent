package com.example.support;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utility class for common data validation routines. Provides strict type and format validation for
 * customer IDs, emails, transaction amounts, tiers, and ticket priorities.
 *
 * @author
 * @version 1.0.3
 */
public final class ValidationUtils {

  private static final Pattern CUSTOMER_ID_PATTERN = Pattern.compile("CUST\\d{3}");
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  private static final Set<String> VALID_TIERS = Set.of("BASIC", "PREMIUM", "ENTERPRISE");
  private static final Set<String> VALID_PRIORITIES = Set.of("LOW", "MEDIUM", "HIGH");

  private static final double MAX_TRANSACTION_AMOUNT = 100_000.00;

  private ValidationUtils() {
    throw new UnsupportedOperationException("Utility class - cannot instantiate");
  }

  public static String validateCustomerId(String customerId) {
    if (customerId == null || customerId.trim().isEmpty()) {
      throw new IllegalArgumentException("Customer ID is required");
    }
    String id = customerId.trim().toUpperCase();
    if (!CUSTOMER_ID_PATTERN.matcher(id).matches()) {
      throw new IllegalArgumentException(
          "Invalid customer ID format. Must be CUST followed by three digits (e.g., CUST001)");
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
      throw new IllegalArgumentException("Invalid amount format: " + amountObj);
    }

    if (amount <= 0) {
      throw new IllegalArgumentException("Amount must be greater than zero");
    }
    if (amount > MAX_TRANSACTION_AMOUNT) {
      throw new IllegalArgumentException(
          "Amount exceeds maximum transaction limit of $" + MAX_TRANSACTION_AMOUNT);
    }

    return Math.round(amount * 100.0) / 100.0;
  }

  public static String validateEmail(String email) {
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be empty");
    }

    String validatedEmail = email.trim();
    if (!EMAIL_PATTERN.matcher(validatedEmail).matches()) {
      throw new IllegalArgumentException("Invalid email format");
    }
    return validatedEmail;
  }

  public static String validateTier(String tier) {
    if (tier == null || tier.trim().isEmpty()) {
      throw new IllegalArgumentException("Account tier cannot be empty");
    }

    String normalizedTier = tier.trim().toUpperCase();
    if (!VALID_TIERS.contains(normalizedTier)) {
      throw new IllegalArgumentException(
          "Invalid tier. Must be one of: Basic, Premium, Enterprise");
    }

    return capitalize(normalizedTier);
  }

  public static String validatePriority(String priority) {
    if (priority == null || priority.trim().isEmpty()) {
      return "Medium";
    }

    String normalized = priority.trim().toUpperCase();
    return VALID_PRIORITIES.contains(normalized) ? capitalize(normalized) : "Medium";
  }

  private static String capitalize(String str) {
    return str.charAt(0) + str.substring(1).toLowerCase();
  }
}
