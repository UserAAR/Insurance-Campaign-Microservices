package com.casestudy.insurancecampaign.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;

/**
 * Service responsible for validating JWT tokens in API Gateway.
 *
 * <p>
 * This service parses signed JWT access tokens and verifies their signature.
 * It supports HMAC validation with a shared secret and RSA validation with a public key.
 * </p>
 *
 * <p>
 * In the recommended microservice setup, Auth Service signs tokens with a private key,
 * while API Gateway validates them with the public key.
 * </p>
 */
@Service
@EnableConfigurationProperties(GatewayJwtProperties.class)
public class GatewayJwtTokenService {
    private final GatewayJwtProperties properties;
    private final boolean rsaEnabled;
    private final PublicKey publicKey;
    private final SecretKey secretKey;

    /**
     * Creates a new gateway JWT token service.
     *
     * <p>
     * If RSA configuration exists, the service loads the configured public key.
     * Otherwise, it uses the configured HMAC secret.
     * </p>
     *
     * @param properties JWT configuration properties for API Gateway
     */
    public GatewayJwtTokenService(GatewayJwtProperties properties) {
        this.properties = properties;
        this.rsaEnabled = properties.rsaEnabled();
        if (rsaEnabled) {
            this.publicKey = GatewayPemKeyUtils.readPublicKey(properties.publicKey(), properties.publicKeyLocation());
            this.secretKey = null;
        } else {
            this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
            this.publicKey = null;
        }
    }

    /**
     * Parses and validates JWT claims.
     *
     * <p>
     * The token signature and issuer are validated. In RSA mode, the public key is used.
     * In HMAC mode, the shared secret key is used.
     * </p>
     *
     * @param token JWT access token
     * @return parsed JWT claims
     * @throws io.jsonwebtoken.JwtException if the token is invalid, expired, malformed,
     *                                      unsupported, or has an invalid signature
     */
    public Claims parseClaims(String token) {
        var parser = rsaEnabled
                ? Jwts.parser().verifyWith(publicKey).requireIssuer(properties.issuer()).build()
                : Jwts.parser().verifyWith(secretKey).requireIssuer(properties.issuer()).build();
        return parser.parseSignedClaims(token).getPayload();
    }

}
