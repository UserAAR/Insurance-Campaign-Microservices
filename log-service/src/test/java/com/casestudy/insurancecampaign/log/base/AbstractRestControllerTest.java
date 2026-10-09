package com.casestudy.insurancecampaign.log.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Base class for MockMvc-based log-service controller tests.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.consumer.group-id=log-service-test",
        "app.kafka.topics.log-events=log-events-test",
        "app.jwt.issuer=allianz-campaign-api",
        "app.jwt.expiration-minutes=120",
        "app.jwt.public-key-location=classpath:keys/public.pem",
        "app.jwt.private-key-location=classpath:keys/private.pem"
})
public abstract class AbstractRestControllerTest extends AbstractTestContainerConfiguration {

    private static final String TOKEN_TYPE = "Bearer";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private ResourceLoader resourceLoader;

    protected String adminBearerToken() {
        return bearerToken(generateAccessToken("admin", List.of("ROLE_ADMIN")));
    }

    protected String userBearerToken() {
        return bearerToken(generateAccessToken("user", List.of("ROLE_USER")));
    }

    protected String internalServiceBearerToken() {
        return bearerToken(generateAccessToken("auth-service", List.of("ROLE_INTERNAL_SERVICE")));
    }

    private String bearerToken(String accessToken) {
        return TOKEN_TYPE + " " + accessToken;
    }

    private String generateAccessToken(String username, List<String> roles) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(120 * 60);

        return Jwts.builder()
                .header()
                .add("typ", TOKEN_TYPE)
                .and()
                .issuer("allianz-campaign-api")
                .subject(username)
                .claims(Map.of("roles", roles))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(readPrivateKey("classpath:keys/private.pem"), Jwts.SIG.RS256)
                .compact();
    }

    private PrivateKey readPrivateKey(String location) {
        try {
            Resource resource = resourceLoader.getResource(location);

            String pem = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(pem);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);

            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception exception) {
            throw new IllegalStateException("Test private key could not be loaded from " + location, exception);
        }
    }
}
