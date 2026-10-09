package com.casestudy.insurancecampaign.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for JWT generation and validation.
 *
 * <p>
 * Properties are loaded with the prefix {@code app.jwt}.
 * </p>
 *
 * <p>
 * The project supports two JWT signing modes:
 * </p>
 *
 * <ul>
 *     <li>HMAC mode using {@code secret}</li>
 *     <li>RSA mode using public/private keys or key locations</li>
 * </ul>
 *
 * <p>
 * RSA mode is enabled when at least one RSA-related property is configured.
 * In that mode, the public key is used for token validation and the private
 * key is used only for token generation.
 * </p>
 *
 * @param secret             shared secret used for HMAC JWT signing
 * @param expirationMinutes  token expiration time in minutes
 * @param issuer             expected JWT issuer
 * @param publicKey          inline RSA public key in PEM format
 * @param privateKey         inline RSA private key in PEM format
 * @param publicKeyLocation  location of RSA public key file
 * @param privateKeyLocation location of RSA private key file
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long expirationMinutes,
        String issuer,
        String publicKey,
        String privateKey,
        String publicKeyLocation,
        String privateKeyLocation
) {

    /**
     * Checks whether RSA-based JWT signing/verification is enabled.
     *
     * <p>
     * RSA is considered enabled when at least one of the RSA key fields
     * is configured.
     * </p>
     *
     * @return {@code true} if RSA configuration exists, otherwise {@code false}
     */
    public boolean rsaEnabled() {
        return hasText(publicKey) || hasText(privateKey) || hasText(publicKeyLocation) || hasText(privateKeyLocation);
    }

    /**
     * Checks whether the given value contains non-blank text.
     *
     * @param value value to check
     * @return {@code true} if value is not {@code null} and not blank
     */
    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

}
