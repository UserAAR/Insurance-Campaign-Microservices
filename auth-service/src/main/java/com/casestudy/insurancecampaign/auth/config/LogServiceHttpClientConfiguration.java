package com.casestudy.insurancecampaign.auth.config;

import com.casestudy.insurancecampaign.security.JwtTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;

import java.util.List;

/**
 * Configures the {@code log-service} HTTP Service group used by
 * {@code LogServiceHttpClient}.
 *
 * <p>A fresh internal JWT is attached to every outgoing request. The client
 * itself remains an {@code @HttpExchange} interface imported through
 * {@code @ImportHttpServices}.</p>
 */
@Configuration(proxyBeanMethods = false)
public class LogServiceHttpClientConfiguration {

    private static final String LOG_SERVICE_GROUP = "log-service";
    private static final String SERVICE_SUBJECT = "auth-service";
    private static final String INTERNAL_SERVICE_ROLE = "ROLE_INTERNAL_SERVICE";

    /**
     * Adds the internal service bearer token to requests created for the
     * {@code log-service} HTTP Service group.
     *
     * @param jwtTokenService shared JWT service using Auth Service's private key
     * @return group-specific RestClient customization
     */
    @Bean
    RestClientHttpServiceGroupConfigurer logServiceHttpServiceGroupConfigurer(
            JwtTokenService jwtTokenService
    ) {
        return groups -> groups.forEachClient((group, clientBuilder) -> {
            if (!LOG_SERVICE_GROUP.equals(group.name())) {
                return;
            }

            clientBuilder.defaultRequest(request -> request.headers(headers ->
                    headers.setBearerAuth(jwtTokenService.generateToken(
                            SERVICE_SUBJECT,
                            List.of(INTERNAL_SERVICE_ROLE)
                    ))
            ));
        });
    }
}
