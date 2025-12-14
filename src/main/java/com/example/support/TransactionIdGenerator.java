package com.example.support;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates unique transaction IDs for financial operations.
 */
public class TransactionIdGenerator {
    
    private static final AtomicLong counter = new AtomicLong(1000);
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    /**
     * Generates a unique transaction ID.
     * Format: TXN-YYYYMMDDHHMMSS-COUNTER
     * 
     * @return unique transaction ID
     */
    public static String generate() {
        String timestamp = LocalDateTime.now().format(formatter);
        long count = counter.getAndIncrement();
        return String.format("TXN-%s-%04d", timestamp, count);
    }
    
    /**
     * Generates a unique ticket ID.
     * Format: TKT-YYYYMMDDHHMMSS-COUNTER
     * 
     * @return unique ticket ID
     */
    public static String generateTicketId() {
        String timestamp = LocalDateTime.now().format(formatter);
        long count = counter.getAndIncrement();
        return String.format("TKT-%s-%04d", timestamp, count);
    }
    
    /**
     * Generates a unique refund ID.
     * Format: REF-YYYYMMDDHHMMSS-COUNTER
     * 
     * @return unique refund ID
     */
    public static String generateRefundId() {
        String timestamp = LocalDateTime.now().format(formatter);
        long count = counter.getAndIncrement();
        return String.format("REF-%s-%04d", timestamp, count);
    }
    
    /**
     * Resets the counter (for testing purposes only).
     */
    public static void resetCounter() {
        counter.set(1000);
    }
}
