package com.casestudy.insurancecampaign.gateway.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for JWT validation in API Gateway.
 *
 * <p>
 * Properties are loaded using the {@code app.jwt} prefix.
 * </p>
 *
 * <p>
 * The gateway supports two validation modes:
 * </p>
 *
 * <ul>
 *     <li>HMAC validation using {@code secret}</li>
 *     <li>RSA validation using a public key or public key location</li>
 * </ul>
 *
 * <p>
 * The API Gateway should only validate JWT tokens. It should not hold the private key
 * or generate access tokens.
 * </p>
 *
 * @param secret            shared secret used for HMAC token validation
 * @param expirationMinutes token expiration duration in minutes
 * @param issuer            expected JWT issuer
 * @param publicKey         inline RSA public key in PEM format
 * @param publicKeyLocation location of RSA public key file
 */
@ConfigurationProperties(prefix = "app.jwt")
public record GatewayJwtProperties(
        String secret,
        long expirationMinutes,
        String issuer,
        String publicKey,
        String publicKeyLocation
) {

    /**
     * Checks whether RSA-based JWT validation is enabled.
     *
     * @return {@code true} if public key configuration exists, otherwise {@code false}
     */
    public boolean rsaEnabled() {
        return hasText(publicKey) || hasText(publicKeyLocation);
    }

    /**
     * Checks whether a string contains non-blank text.
     *
     * @param value value to check
     * @return {@code true} if the value is not {@code null} and not blank
     */
    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

}
