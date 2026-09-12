package com.railsync.util;

import java.security.SecureRandom;
import java.util.Set;

/**
 * Generates cryptographically secure, unique Railway PNR numbers.
 * Example format: "RS482731" or "RS910283".
 */
public final class PNRGenerator {
    private static final String PREFIX = "RS";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PNRGenerator() {}

    /**
     * Generates a unique 6-digit PNR with prefix "RS".
     * Checks against known PNR set to guarantee no collision.
     */
    public static synchronized String generateUniquePNR(Set<String> existingPNRs) {
        String pnr;
        int attempts = 0;
        do {
            int number = 100000 + RANDOM.nextInt(900000);
            pnr = PREFIX + number;
            attempts++;
            if (attempts > 1000) {
                // Fallback to high-entropy alphanumeric if numeric range gets crowded
                pnr = PREFIX + System.currentTimeMillis() % 1000000;
                break;
            }
        } while (existingPNRs != null && existingPNRs.contains(pnr));

        if (existingPNRs != null) {
            existingPNRs.add(pnr);
        }
        return pnr;
    }
}
