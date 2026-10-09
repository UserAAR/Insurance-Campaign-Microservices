package com.casestudy.insurancecampaign.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main entry point for the API Gateway application.
 *
 * <p>
 * API Gateway acts as the single entry point for client requests in the
 * Insurance Campaign Microservices project.
 * </p>
 *
 * <p>
 * Main responsibilities:
 * </p>
 *
 * <ul>
 *     <li>Route requests to downstream services such as Auth Service, Campaign Service,
 *     Dashboard Service, and Log Service.</li>
 *     <li>Validate JWT tokens before forwarding protected requests.</li>
 *     <li>Expose aggregated Swagger/OpenAPI documentation.</li>
 *     <li>Register with service discovery through Eureka.</li>
 *     <li>Participate in metrics, logs, and distributed tracing.</li>
 * </ul>
 */
@SpringBootApplication(scanBasePackages = "com.casestudy.insurancecampaign")
@EnableDiscoveryClient
public class ApiGatewayApplication {

    /**
     * Starts the API Gateway application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
