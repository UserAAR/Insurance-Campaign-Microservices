package com.casestudy.insurancecampaign.gateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for load-balanced clients used by the API Gateway.
 *
 * <p>
 * The {@link LoadBalanced} {@link WebClient.Builder} allows gateway-owned
 * clients to call downstream services through service discovery by using
 * {@code lb://service-name} URLs. This keeps internal gateway calls consistent
 * with Spring Cloud Gateway routes that also use {@code lb://} URIs.
 * </p>
 */
@Configuration
public class GatewayLoadBalancerConfiguration {

    /**
     * Creates a load-balanced WebClient builder.
     *
     * @return WebClient builder resolved through Spring Cloud LoadBalancer
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

}
