package com.casestudy.insurancecampaign.campaign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.casestudy.insurancecampaign")
@EnableDiscoveryClient
@EnableCaching
public class CampaignServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampaignServiceApplication.class, args);
    }
}
