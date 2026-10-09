package com.casestudy.insurancecampaign.log.controller;

import com.casestudy.insurancecampaign.common.api.ApiResponse;
import com.casestudy.insurancecampaign.log.model.document.SlowRequestDocument;
import com.casestudy.insurancecampaign.log.repository.SlowRequestLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for reading slow request logs.
 */
@RestController
@RequestMapping("/api/v1/logs")
public class SlowRequestLogController {

    private final SlowRequestLogRepository repository;

    public SlowRequestLogController(SlowRequestLogRepository repository) {
        this.repository = repository;
    }

    /**
     * Lists slow request logs with pagination. ROLE_ADMIN only.
     *
     * @param page page number
     * @param size page size
     * @return paginated slow request logs
     */
    @Operation(summary = "List slow requests logged because they exceeded 5 ms")
    @GetMapping("/slow-requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<SlowRequestDocument>> getSlowRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(repository.findAll(PageRequest.of(page, size)));
    }

}
