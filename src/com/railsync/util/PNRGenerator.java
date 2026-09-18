package com.railsync.util;

import java.security.SecureRandom;
import java.util.Set;

/**
 * Generates unique PNR numbers for bookings.
 * Example format: "RS482731".
 */
public final class PNRGenerator {
    private static final String PREFIX = "RS";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PNRGenerator() {}

    /**
     * Generates a unique 6-digit PNR with prefix "RS".
     * Checks against existing PNRs to avoid duplicates.
     */
    public static synchronized String generateUniquePNR(Set<String> existingPNRs) {
        String pnr;
        int attempts = 0;
        do {
            int number = 100000 + RANDOM.nextInt(900000);
            pnr = PREFIX + number;
            attempts++;
            if (attempts > 1000) {
                // Fallback using timestamp if needed
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
