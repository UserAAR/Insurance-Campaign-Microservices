package com.casestudy.insurancecampaign.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Main entry point for the Discovery Server application.
 *
 * <p>
 * This service enables Eureka Server and provides service discovery for the
 * Insurance Campaign Microservices project.
 * </p>
 *
 * <p>
 * Microservices such as API Gateway, Config Server, Auth Service, Campaign Service,
 * Dashboard Service, and Log Service can register themselves with this Discovery Server.
 * Other services can then discover them by service name instead of hardcoded host/port values.
 * </p>
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {

    /**
     * Starts the Discovery Server application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }

}
