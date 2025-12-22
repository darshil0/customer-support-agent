package com.example.support;

import java.util.regex.Pattern;

/** Utility class for input validation and sanitization. */
public class ValidationUtils {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  private static final Pattern CUSTOMER_ID_PATTERN = Pattern.compile("^CUST\\d{3,}$");

  /**
   * Validates a customer ID.
   *
   * @param customerId the customer ID to validate
   * @return true if valid, false otherwise
   */
  public static boolean isValidCustomerId(String customerId) {
    return customerId != null
        && !customerId.trim().isEmpty()
        && CUSTOMER_ID_PATTERN.matcher(customerId).matches();
  }

  /**
   * Validates an email address.
   *
   * @param email the email to validate
   * @return true if valid, false otherwise
   */
  public static boolean isValidEmail(String email) {
    return email != null && !email.trim().isEmpty() && EMAIL_PATTERN.matcher(email).matches();
  }

  /**
   * Validates an amount is positive and within reasonable limits.
   *
   * @param amount the amount to validate
   * @return true if valid, false otherwise
   */
  public static boolean isValidAmount(double amount) {
    return amount > 0 && amount <= 100000.00;
  }

  /**
   * Validates a tier value.
   *
   * @param tier the tier to validate
   * @return true if valid, false otherwise
   */
  public static boolean isValidTier(String tier) {
    if (tier == null || tier.trim().isEmpty()) {
      return false;
    }
    String normalizedTier = tier.trim().toLowerCase();
    return normalizedTier.equals("basic")
        || normalizedTier.equals("premium")
        || normalizedTier.equals("enterprise");
  }

  /**
   * Validates a priority value.
   *
   * @param priority the priority to validate
   * @return true if valid, false otherwise
   */
  public static boolean isValidPriority(String priority) {
    if (priority == null || priority.trim().isEmpty()) {
      return false;
    }
    String normalizedPriority = priority.trim().toLowerCase();
    return normalizedPriority.equals("low")
        || normalizedPriority.equals("medium")
        || normalizedPriority.equals("high")
        || normalizedPriority.equals("urgent");
  }

  /**
   * Validates a status value.
   *
   * @param status the status to validate
   * @return true if valid, false otherwise
   */
  public static boolean isValidStatus(String status) {
    if (status == null || status.trim().isEmpty()) {
      return false;
    }
    String normalizedStatus = status.trim().toLowerCase();
    return normalizedStatus.equals("open")
        || normalizedStatus.equals("closed")
        || normalizedStatus.equals("pending")
        || normalizedStatus.equals("all");
  }

  /**
   * Sanitizes a string by removing potentially harmful characters.
   *
   * @param input the string to sanitize
   * @return sanitized string
   */
  public static String sanitize(String input) {
    if (input == null) {
      return "";
    }
    return input.trim().replaceAll("[<>\"']", "").replaceAll("\\s+", " ");
  }

  /**
   * Rounds an amount to 2 decimal places.
   *
   * @param amount the amount to round
   * @return rounded amount
   */
  public static double roundAmount(double amount) {
    return Math.round(amount * 100.0) / 100.0;
  }
}
