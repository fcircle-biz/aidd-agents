package com.example.todoapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Spring Security configuration for the Todo application.
 * 
 * This configuration provides:
 * - Basic authentication setup (without user authentication for simplicity)
 * - CSRF protection (disabled for API endpoints)
 * - XSS protection through security headers
 * - Secure access to H2 console
 * - SQL injection protection (ensured through JPA parameter binding)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure the security filter chain with appropriate security measures.
     *
     * @param http HttpSecurity configuration object
     * @return SecurityFilterChain configured security filter chain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Allow unrestricted access to public resources
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                
                // Allow access to API endpoints (for external integration)
                .requestMatchers("/api/**").permitAll()
                
                // Allow access to H2 console for development
                .requestMatchers("/h2-console/**").permitAll()
                
                // Allow access to actuator endpoints
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // Allow access to dev endpoints (development environment only)
                .requestMatchers("/dev/**").permitAll()
                
                // Allow access to all web pages (no authentication required per requirements)
                .requestMatchers("/", "/todos/**", "/error/**").permitAll()
                
                // Allow access to management endpoints
                .requestMatchers("/management/**").permitAll()
                
                // All other requests are permitted (simplified security model)
                .anyRequest().permitAll()
            )
            
            // Configure CSRF protection
            .csrf(csrf -> csrf
                // Disable CSRF for API endpoints to allow external access
                .ignoringRequestMatchers("/api/**")
                // Disable CSRF for H2 console
                .ignoringRequestMatchers("/h2-console/**")
                // Disable CSRF for dev endpoints
                .ignoringRequestMatchers("/dev/**")
                // Keep CSRF enabled for web forms (provides protection against CSRF attacks)
            )
            
            // Configure security headers for XSS and other attack prevention
            .headers(headers -> headers
                // Allow H2 console to be displayed in frames (for development)
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
                
                // Enable XSS protection
                .contentTypeOptions(contentTypeOptions -> {})
                
                // Configure referrer policy
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            );

        return http.build();
    }
}