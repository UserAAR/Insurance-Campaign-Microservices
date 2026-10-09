package com.casestudy.insurancecampaign.auth;

import com.casestudy.insurancecampaign.auth.client.LogServiceHttpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.service.registry.ImportHttpServices;

/**
 * Main entry point for the Auth Service application.
 *
 * <p>
 * This service is responsible for authentication operations such as user
 * registration, login, refresh-token handling, password reset, and logout.
 * </p>
 */
@SpringBootApplication(scanBasePackages = "com.casestudy.insurancecampaign")
@EnableDiscoveryClient
@ImportHttpServices(group = "log-service", types = LogServiceHttpClient.class)
public class AuthServiceApplication {

    /**
     * Starts the Auth Service Spring Boot application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

}
