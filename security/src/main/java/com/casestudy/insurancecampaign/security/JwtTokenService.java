package com.casestudy.insurancecampaign.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for generating and validating JWT tokens.
 *
 * <p>
 * This service supports two signing strategies:
 * </p>
 *
 * <ul>
 *     <li>HMAC signing using a shared secret</li>
 *     <li>RSA signing using a private key and verification using a public key</li>
 * </ul>
 *
 * <p>
 * In the microservice architecture, {@code auth-service} should generate
 * tokens using the private key, while other services should only validate
 * tokens using the public key.
 * </p>
 */
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class JwtTokenService {

    private final JwtProperties properties;
    private final boolean rsaEnabled;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final SecretKey secretKey;

    /**
     * Creates a new JWT token service and initializes key material based
     * on {@link JwtProperties}.
     *
     * <p>
     * If RSA configuration is available, RSA keys are loaded from inline PEM
     * values or configured locations. Otherwise, HMAC mode is used with the
     * configured secret.
     * </p>
     *
     * @param properties JWT configuration properties
     * @throws IllegalStateException if key configuration is missing or invalid
     */
    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        this.rsaEnabled = properties.rsaEnabled();
        if (rsaEnabled) {
            this.publicKey = PemKeyUtils.readPublicKey(properties.publicKey(), properties.publicKeyLocation());
            this.privateKey = hasText(properties.privateKey()) || hasText(properties.privateKeyLocation())
                    ? PemKeyUtils.readPrivateKey(properties.privateKey(), properties.privateKeyLocation())
                    : null;
            this.secretKey = null;
        } else {
            this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
            this.publicKey = null;
            this.privateKey = null;
        }
    }

    /**
     * Generates a signed JWT access token for the given user.
     *
     * <p>
     * The generated token contains:
     * </p>
     *
     * <ul>
     *     <li>issuer</li>
     *     <li>subject as username</li>
     *     <li>roles claim</li>
     *     <li>issued date</li>
     *     <li>expiration date</li>
     * </ul>
     *
     * @param username username or subject value of the token
     * @param roles    granted roles to store in the token claims
     * @return signed JWT token
     * @throws IllegalStateException if RSA mode is enabled but private key is missing
     */
    public String generateToken(String username, List<String> roles) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.expirationMinutes() * 60);
        var builder = Jwts.builder()
                .issuer(properties.issuer())
                .subject(username)
                .claims(Map.of("roles", roles))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt));

        if (rsaEnabled) {
            if (privateKey == null) {
                throw new IllegalStateException("JWT private key is required for token generation. Configure app.jwt.private-key-location or app.jwt.private-key in auth-service.");
            }
            return builder.signWith(privateKey, Jwts.SIG.RS256).compact();
        }
        return builder.signWith(secretKey).compact();
    }

    /**
     * Parses and validates JWT claims.
     *
     * <p>
     * The method verifies the token signature and validates the expected issuer.
     * In RSA mode, the public key is used for verification. In HMAC mode, the
     * shared secret key is used.
     * </p>
     *
     * @param token JWT token to parse
     * @return parsed JWT claims
     * @throws io.jsonwebtoken.JwtException if the token is expired, malformed,
     *                                      unsupported, or has an invalid signature
     */
    public Claims parseClaims(String token) {
        var parser = rsaEnabled
                ? Jwts.parser().verifyWith(publicKey).requireIssuer(properties.issuer()).build()
                : Jwts.parser().verifyWith(secretKey).requireIssuer(properties.issuer()).build();
        return parser.parseSignedClaims(token).getPayload();
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
