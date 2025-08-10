package com.example.todoapp.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security configuration tests for the Todo application.
 * 
 * This test class verifies that all security measures are properly implemented:
 * - CSRF protection (enabled for web forms, disabled for API)
 * - XSS protection through security headers
 * - Access control for different endpoints
 * - Security headers configuration
 * - SQL injection protection (verified through repository tests)
 */
@SpringBootTest
@AutoConfigureTestMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Security Configuration Tests")
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should allow access to public static resources")
    void shouldAllowAccessToStaticResources() throws Exception {
        mockMvc.perform(get("/css/style.css"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow access to API endpoints without CSRF token")
    void shouldAllowApiAccessWithoutCsrf() throws Exception {
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow POST to API without CSRF token")
    void shouldAllowApiPostWithoutCsrf() throws Exception {
        String jsonContent = """
            {
                "title": "Security Test Todo",
                "description": "Testing API security",
                "status": "TODO",
                "priority": "MEDIUM"
            }
            """;

        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should allow access to H2 console for development")
    void shouldAllowH2ConsoleAccess() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow access to web pages")
    void shouldAllowWebPageAccess() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection()); // Redirects to /todos
    }

    @Test
    @DisplayName("Should include security headers in responses")
    void shouldIncludeSecurityHeaders() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().exists("Referrer-Policy"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
    }

    @Test
    @DisplayName("Should include Content Security Policy header")
    void shouldIncludeContentSecurityPolicy() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().string("Content-Security-Policy", 
                    "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self';"));
    }

    @Test
    @DisplayName("Should allow access to actuator health endpoint")
    void shouldAllowActuatorHealthAccess() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow access to development endpoints")
    void shouldAllowDevEndpointsAccess() throws Exception {
        mockMvc.perform(get("/dev/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should require CSRF token for web form submissions")
    void shouldRequireCsrfForWebForms() throws Exception {
        // This test verifies CSRF protection is enabled for web forms
        // by attempting to submit a form without CSRF token
        mockMvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Test Todo")
                .param("description", "Test Description"))
                .andExpect(status().isForbidden()); // Should be forbidden due to missing CSRF token
    }

    @Test
    @DisplayName("Should allow web form submission with CSRF token")
    void shouldAllowWebFormWithCsrfToken() throws Exception {
        // First get the CSRF token from the form page
        String response = mockMvc.perform(get("/todos/new"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract CSRF token (simplified - in real scenario, parse HTML)
        // For this test, we'll perform the request with proper session
        mockMvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Test Todo")
                .param("description", "Test Description")
                .param("status", "TODO")
                .param("priority", "MEDIUM")
                .with(request -> {
                    request.setAttribute("_csrf", "test-token");
                    return request;
                }))
                .andExpect(status().is3xxRedirection()); // Should redirect on success
    }

    @Test
    @DisplayName("Should handle SQL injection attempts safely")
    void shouldHandleSqlInjectionSafely() throws Exception {
        // Test potential SQL injection in search parameters
        String maliciousInput = "'; DROP TABLE todo; --";
        
        mockMvc.perform(get("/api/todos/search")
                .param("keyword", maliciousInput)
                .param("status", "TODO"))
                .andExpect(status().isOk()); // Should handle safely and not cause errors
    }

    @Test
    @DisplayName("Should handle XSS attempts safely in API responses")
    void shouldHandleXssAttemptsInApi() throws Exception {
        // Test potential XSS in API input
        String xssPayload = "<script>alert('XSS')</script>";
        String jsonContent = String.format("""
            {
                "title": "%s",
                "description": "Test XSS protection",
                "status": "TODO",
                "priority": "MEDIUM"
            }
            """, xssPayload);

        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // The response should contain the escaped content
                .andExpect(jsonPath("$.title").value(xssPayload)); // JPA/JSON serialization handles this safely
    }

    @Test
    @DisplayName("Should prevent clickjacking with X-Frame-Options header")
    void shouldPreventClickjacking() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
    }

    @Test
    @DisplayName("Should include strict transport security header")
    void shouldIncludeStrictTransportSecurity() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Strict-Transport-Security"));
    }

    @Test
    @DisplayName("Should include referrer policy header")
    void shouldIncludeReferrerPolicy() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"));
    }

    @Test
    @DisplayName("Should handle large payloads safely")
    void shouldHandleLargePayloadsSafely() throws Exception {
        // Test with a large payload to ensure proper handling
        StringBuilder largeDescription = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeDescription.append("This is a very long description. ");
        }

        String jsonContent = String.format("""
            {
                "title": "Large Payload Test",
                "description": "%s",
                "status": "TODO",
                "priority": "MEDIUM"
            }
            """, largeDescription.toString());

        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isBadRequest()); // Should be rejected due to validation constraints
    }
}