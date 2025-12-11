package com.example.support;

import java.util.concurrent.atomic.AtomicLong;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

/**
 * Utility class for generating unique, sequential IDs for transactions and tickets.
 */
public class TransactionIdGenerator {

    private static final AtomicLong SEQUENCE = new AtomicLong(1000);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    public static String generateTransactionId(String prefix) {
        String datePart = LocalDateTime.now().format(DATE_FORMATTER);
        long sequencePart = SEQUENCE.getAndIncrement();
        // Format: PREFIX-YYMMDD-SEQUENCE
        return String.format("%s-%s-%06d", prefix.toUpperCase(), datePart, sequencePart);
    }

    public static String generateTicketId() {
        // Format: TICKET-YYMMDD-SEQUENCE
        return generateTransactionId("TICKET");
    }
}
