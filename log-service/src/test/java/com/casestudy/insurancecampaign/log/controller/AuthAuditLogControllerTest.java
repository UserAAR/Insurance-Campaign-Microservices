package com.casestudy.insurancecampaign.log.controller;

import com.casestudy.insurancecampaign.log.base.AbstractRestControllerTest;
import com.casestudy.insurancecampaign.log.model.dto.request.AuthAuditLogRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthAuditLogControllerTest extends AbstractRestControllerTest {

    @Test
    void shouldWriteAuthAuditWhenCallerIsInternalService() throws Exception {
        AuthAuditLogRequest request = request("LOGIN_SUCCESS", "SUCCESS");

        mockMvc.perform(post("/api/v1/logs/auth-audit")
                        .header(HttpHeaders.AUTHORIZATION, internalServiceBearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Auth audit saved successfully"));
    }

    @Test
    void shouldRejectWriteAuthAuditWhenCallerIsAdminUser() throws Exception {
        AuthAuditLogRequest request = request("LOGIN_SUCCESS", "SUCCESS");

        mockMvc.perform(post("/api/v1/logs/auth-audit")
                        .header(HttpHeaders.AUTHORIZATION, adminBearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void shouldRejectWriteAuthAuditWhenCallerIsRegularUser() throws Exception {
        AuthAuditLogRequest request = request("LOGIN_FAILURE", "FAILED");

        mockMvc.perform(post("/api/v1/logs/auth-audit")
                        .header(HttpHeaders.AUTHORIZATION, userBearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void shouldRejectWriteAuthAuditWhenUnauthenticated() throws Exception {
        AuthAuditLogRequest request = request("LOGIN_SUCCESS", "SUCCESS");

        mockMvc.perform(post("/api/v1/logs/auth-audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private AuthAuditLogRequest request(String eventType, String status) {
        return new AuthAuditLogRequest(
                "auth-service",
                eventType,
                "demo",
                "Authentication audit event",
                status,
                Instant.parse("2026-07-26T12:00:00Z")
        );
    }
}
