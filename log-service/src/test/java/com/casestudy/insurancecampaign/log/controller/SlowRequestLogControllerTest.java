package com.casestudy.insurancecampaign.log.controller;

import com.casestudy.insurancecampaign.log.base.AbstractRestControllerTest;
import com.casestudy.insurancecampaign.log.model.document.SlowRequestDocument;
import com.casestudy.insurancecampaign.log.repository.SlowRequestLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SlowRequestLogControllerTest extends AbstractRestControllerTest {

    @MockitoBean
    private SlowRequestLogRepository slowRequestLogRepository;

    @Test
    void shouldReturnSlowRequestsWithPaginationWhenRoleIsAdmin() throws Exception {
        // Given
        SlowRequestDocument document = new SlowRequestDocument(
                "campaign-service",
                "GET",
                "/api/v1/campaigns",
                200,
                150L,
                "trace-123",
                Map.of("X-Correlation-Id", "correlation-123"),
                Instant.parse("2026-07-26T12:15:00Z")
        );

        PageRequest pageRequest = PageRequest.of(0, 20);

        when(slowRequestLogRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(document), pageRequest, 1));

        // When & Then
        mockMvc.perform(get("/api/v1/logs/slow-requests")
                        .header(HttpHeaders.AUTHORIZATION, adminBearerToken())
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].serviceName").value("campaign-service"))
                .andExpect(jsonPath("$.data.content[0].method").value("GET"))
                .andExpect(jsonPath("$.data.content[0].path").value("/api/v1/campaigns"))
                .andExpect(jsonPath("$.data.content[0].status").value(200))
                .andExpect(jsonPath("$.data.content[0].durationMs").value(150))
                .andExpect(jsonPath("$.data.content[0].traceId").value("trace-123"))
                .andExpect(jsonPath("$.data.content[0].headers['X-Correlation-Id']").value("correlation-123"));

        // Verify
        verify(slowRequestLogRepository).findAll(ArgumentMatchers.<Pageable>argThat(pageable ->
                pageable.getPageNumber() == 0
                        && pageable.getPageSize() == 20
        ));
    }

    @Test
    void shouldUseDefaultPaginationWhenRequestParamsAreMissingAndRoleIsAdmin() throws Exception {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 20);

        when(slowRequestLogRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageRequest, 0));

        // When & Then
        mockMvc.perform(get("/api/v1/logs/slow-requests")
                        .header(HttpHeaders.AUTHORIZATION, adminBearerToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        // Verify
        verify(slowRequestLogRepository).findAll(ArgumentMatchers.<Pageable>argThat(pageable ->
                pageable.getPageNumber() == 0
                        && pageable.getPageSize() == 20
        ));
    }

    @Test
    void shouldRejectSlowRequestsWhenRoleIsUser() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/logs/slow-requests")
                        .header(HttpHeaders.AUTHORIZATION, userBearerToken()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        // Verify
        verifyNoInteractions(slowRequestLogRepository);
    }

    @Test
    void shouldRejectSlowRequestsWhenUnauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/logs/slow-requests"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        // Verify
        verifyNoInteractions(slowRequestLogRepository);
    }
}
