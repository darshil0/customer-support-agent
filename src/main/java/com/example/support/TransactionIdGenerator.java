package com.example.support;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for generating unique, sequential IDs
 * for transactions and support tickets.
 * <p>
 * Format examples:
 * <ul>
 *   <li>{@code TXN-251212-000123}</li>
 *   <li>{@code REF-251212-000124}</li>
 *   <li>{@code TICKET-251212-000125}</li>
 * </ul>
 * </p>
 */
public final class TransactionIdGenerator {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
  private static final Map<String, AtomicLong> SEQUENCE_MAP = new ConcurrentHashMap<>();

  private TransactionIdGenerator() {
    // Utility class; prevent instantiation
  }

  /**
   * Generates a unique ID with a given prefix.
   * Thread-safe and sequential within each prefix type.
   *
   * @param prefix ID type prefix (e.g., "TXN", "REF", "TICKET")
   * @return Formatted ID like TXN-251212-000231
   */
  public static String generateTransactionId(String prefix) {
    String type = prefix.toUpperCase();
    AtomicLong sequence = SEQUENCE_MAP.computeIfAbsent(type, k -> new AtomicLong(1000));
    long seq = sequence.getAndIncrement();
    String datePart = LocalDate.now().format(DATE_FORMATTER);
    return String.format("%s-%s-%06d", type, datePart, seq);
  }

  /**
   * Generates a unique ticket ID.
   */
  public static String generateTicketId() {
    return generateTransactionId("TICKET");
  }

  /**
   * Resets all sequence counters. Useful for testing.
   */
  public static void resetSequences() {
    SEQUENCE_MAP.clear();
  }
}
