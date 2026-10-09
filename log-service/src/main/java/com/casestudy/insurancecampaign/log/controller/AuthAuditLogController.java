package com.casestudy.insurancecampaign.log.controller;
import com.casestudy.insurancecampaign.common.api.ApiResponse;
import com.casestudy.insurancecampaign.log.model.dto.request.AuthAuditLogRequest;
import com.casestudy.insurancecampaign.log.service.AuthAuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller responsible for receiving authentication audit events.
 */
@RestController
@RequestMapping("/api/v1/logs")
public class AuthAuditLogController {

    private final AuthAuditLogService authAuditLogService;

    public AuthAuditLogController(AuthAuditLogService authAuditLogService) {
        this.authAuditLogService = authAuditLogService;
    }

    /**
     * Receives an authentication audit event, saves it into Elasticsearch,
     * and writes it to the log-service info log.
     *
     * @param request authentication audit log request
     * @return confirmation message wrapped with {@link ApiResponse}
     */
    @Operation(summary = "Save authentication audit event to Elasticsearch")
    @PostMapping("/auth-audit")
    @PreAuthorize("hasRole('INTERNAL_SERVICE')")
    public ApiResponse<String> writeAuthAudit(@RequestBody AuthAuditLogRequest request) {
        authAuditLogService.save(request);
        return ApiResponse.success("Auth audit saved successfully");
    }
}