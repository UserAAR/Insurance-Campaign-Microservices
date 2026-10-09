package com.casestudy.insurancecampaign.auth.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Utility class for generating secure random tokens and hashing token values.
 *
 * <p>
 * This class is mainly used for refresh tokens and password reset tokens.
 * Raw tokens are returned to the client once, while hashed token values are
 * stored in the database.
 * </p>
 */
public final class TokenHashUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Prevents instantiation of this utility class.
     */
    private TokenHashUtils() {
    }

    /**
     * Generates a cryptographically secure random token.
     *
     * <p>
     * The generated token is Base64 URL-safe encoded without padding, which makes
     * it suitable for usage in HTTP requests, JSON payloads, and URLs.
     * </p>
     *
     * @return generated random token
     */
    public static String randomToken() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Calculates the SHA-256 hash of the given token.
     *
     * <p>
     * This method is used to avoid storing raw refresh tokens or password reset
     * tokens in the database.
     * </p>
     *
     * @param token raw token value
     * @return SHA-256 hash value in hexadecimal format
     * @throws IllegalStateException if SHA-256 algorithm is not available
     */
    public static String sha256(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }

}
