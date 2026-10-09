package com.casestudy.insurancecampaign.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Main entry point for the Config Server application.
 *
 * <p>
 * This service provides centralized configuration for the Insurance Campaign
 * Microservices project. Other services can retrieve their configuration from
 * this Config Server instead of keeping all configuration locally.
 * </p>
 *
 * <p>
 * The {@link EnableConfigServer} annotation enables Spring Cloud Config Server
 * functionality.
 * </p>
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    /**
     * Starts the Config Server application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }

}
