package com.casestudy.insurancecampaign.gateway.security;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Utility class for reading and parsing RSA public keys for API Gateway JWT validation.
 *
 * <p>
 * This utility supports public keys in PEM format:
 * </p>
 *
 * <pre>
 * -----BEGIN PUBLIC KEY-----
 * ...
 * -----END PUBLIC KEY-----
 * </pre>
 *
 * <p>
 * Only public key parsing is supported here because API Gateway should validate tokens,
 * not generate them.
 * </p>
 */
final class GatewayPemKeyUtils {

    /**
     * Utility class constructor.
     *
     * <p>
     * This class should not be instantiated.
     * </p>
     */
    private GatewayPemKeyUtils() {
    }

    /**
     * Reads and parses an RSA public key.
     *
     * <p>
     * The public key can be provided either inline or by location. Inline PEM content
     * has priority over location-based loading.
     * </p>
     *
     * @param inlinePem inline public key in PEM format
     * @param location  public key location, such as {@code classpath:keys/public-key.pem}
     *                  or a filesystem path
     * @return parsed RSA public key
     * @throws IllegalStateException if the key is missing, unreadable, or invalid
     */
    static PublicKey readPublicKey(String inlinePem, String location) {
        String pem = readPem(inlinePem, location);
        byte[] der = decodePem(pem);
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot parse RSA public key", exception);
        }
    }

    /**
     * Reads PEM content from inline text or configured location.
     *
     * @param inlinePem inline PEM content
     * @param location  Spring resource location or filesystem path
     * @return PEM content
     * @throws IllegalStateException if no key source is configured or reading fails
     */
    private static String readPem(String inlinePem, String location) {
        if (hasText(inlinePem)) {
            return inlinePem;
        }
        if (!hasText(location)) {
            throw new IllegalStateException("Missing JWT public key. Configure app.jwt.public-key or app.jwt.public-key-location");
        }
        try {
            Resource resource = new DefaultResourceLoader().getResource(location);
            if (resource.exists()) {
                return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            return Files.readString(Path.of(location), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read JWT public key from location: " + location, exception);
        }
    }

    /**
     * Decodes a PEM public key into DER bytes.
     *
     * @param pem PEM-formatted public key
     * @return decoded DER bytes
     * @throws IllegalArgumentException if the PEM content is not valid Base64
     */
    private static byte[] decodePem(String pem) {
        String normalized = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(normalized);
    }

    /**
     * Checks whether the given value contains non-blank text.
     *
     * @param value value to check
     * @return {@code true} if the value is not {@code null} and not blank
     */
    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

}
