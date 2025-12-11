package com.example.support;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

/**
 * Utility class for generating secure, traceable transaction and ticket IDs.
 */
public class TransactionIdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a traceable transaction ID. Format: TXN-YYYYMMDD-XXXXXX
     * (where XXXXXX is a secure random suffix).
     */
    public static String generateTransactionId(String prefix) {
        String date = LocalDate.now().format(
            DateTimeFormatter.BASIC_ISO_DATE
        );

        // Generate 3 random bytes and encode them for a secure, short random suffix
        byte[] bytes = new byte[3];
        RANDOM.nextBytes(bytes);
        String random = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes);

        return prefix + "-" + date + "-" + random.toUpperCase();
    }

    /**
     * Generates a unique, short ticket ID.
     */
    public static String generateTicketId() {
        return "TKT-" + UUID.randomUUID()
            .toString()
            .substring(0, 8)
            .toUpperCase();
    }
}
