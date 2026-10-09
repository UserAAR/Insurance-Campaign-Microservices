package com.casestudy.insurancecampaign.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class for authentication-related beans.
 *
 * <p>
 * This configuration provides the {@link PasswordEncoder} bean used by the
 * auth-service to hash user passwords and validate login credentials.
 * </p>
 */
@Configuration
public class AuthConfiguration {

    /**
     * Creates a delegating password encoder.
     *
     * <p>
     * {@link PasswordEncoderFactories#createDelegatingPasswordEncoder()} supports
     * multiple password encoding algorithms and stores the encoder id as a prefix
     * in the encoded password value.
     * </p>
     *
     * @return password encoder used for password hashing and verification
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
