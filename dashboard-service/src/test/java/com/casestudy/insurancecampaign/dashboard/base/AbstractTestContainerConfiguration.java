package com.casestudy.insurancecampaign.dashboard.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractTestContainerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AbstractTestContainerConfiguration.class);

    @Container
    private static final PostgreSQLContainer<?> AUTH_POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", AUTH_POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", AUTH_POSTGRES::getUsername);
        registry.add("spring.datasource.password", AUTH_POSTGRES::getPassword);

        registry.add("eureka.client.enabled", () -> false);
        registry.add("spring.cloud.config.enabled", () -> false);

        registry.add("app.jwt.issuer", () -> "allianz-campaign-api");
        registry.add("app.jwt.expiration-minutes", () -> 120);
        registry.add("app.jwt.public-key-location", () -> "classpath:keys/public.pem");
        registry.add("app.jwt.private-key-location", () -> "classpath:keys/private.pem");

        registry.add("app.auth.refresh-token-expiration-days", () -> 7);
        registry.add("app.auth.password-reset-token-expiration-minutes", () -> 15);
    }

}
