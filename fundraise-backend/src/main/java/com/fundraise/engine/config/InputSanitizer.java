package com.fundraise.engine.config;

/**
 * Input sanitizer for XSS prevention.
 * Escapes HTML entities and removes dangerous characters from user input.
 */
public class InputSanitizer {

    /**
     * Sanitize a string input by escaping HTML entities.
     */
    public static String sanitize(String input) {
        if (input == null) return null;

        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");
    }

    /**
     * Sanitize input and strip leading/trailing whitespace.
     */
    public static String sanitizeAndTrim(String input) {
        if (input == null) return null;
        return sanitize(input).trim();
    }

    /**
     * Validate email format (basic check).
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    /**
     * Check if input contains potential SQL injection patterns.
     */
    public static boolean hasSqlInjectionPatterns(String input) {
        if (input == null) return false;
        String lower = input.toLowerCase();
        return lower.contains("union select") ||
               lower.contains("drop table") ||
               lower.contains("insert into") ||
               lower.contains("delete from") ||
               lower.contains("--") ||
               lower.contains(";");
    }

    /**
     * Validate that a UUID string is properly formatted.
     */
    public static boolean isValidUuid(String uuid) {
        if (uuid == null || uuid.isEmpty()) return false;
        return uuid.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }
}
