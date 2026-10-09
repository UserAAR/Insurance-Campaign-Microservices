package com.casestudy.insurancecampaign.common.exception;

import com.casestudy.insurancecampaign.common.api.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles exceptions globally and converts them into standardized API error responses.
 *
 * <p>This handler ensures that all services return a consistent error structure
 * for business exceptions, validation errors, authentication errors,
 * authorization errors, and unexpected system errors.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles application-specific business exceptions.
     *
     * <p>The HTTP status is selected based on the {@link ErrorCode} carried by
     * the exception.</p>
     *
     * @param exception business exception thrown by the application
     * @param request   current HTTP request
     * @return standardized API error response
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException exception, HttpServletRequest request) {
        HttpStatus status = switch (exception.getErrorCode()) {
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case DUPLICATE_CAMPAIGN, INVALID_STATUS_TRANSITION, BUSINESS_RULE_VIOLATION -> HttpStatus.CONFLICT;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.BAD_REQUEST;
        };

        return ResponseEntity.status(status)
                .body(ApiError.of(
                        exception.getErrorCode().name(),
                        exception.getMessage(),
                        status.value(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles authorization failures.
     *
     * <p>This method is triggered when the authenticated user does not have
     * permission to access the requested resource.</p>
     *
     * @param exception access denied exception
     * @param request   current HTTP request
     * @return standardized forbidden error response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(
                        ErrorCode.FORBIDDEN.name(),
                        "Access denied",
                        HttpStatus.FORBIDDEN.value(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles authentication failures.
     *
     * <p>This method is triggered when authentication is missing, invalid, or expired.</p>
     *
     * @param exception authentication exception
     * @param request   current HTTP request
     * @return standardized unauthorized error response
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.of(
                        ErrorCode.UNAUTHORIZED.name(),
                        "Authentication is required",
                        HttpStatus.UNAUTHORIZED.value(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles request validation errors.
     *
     * <p>Validation errors are converted into field-level error messages so that
     * API clients can clearly understand which fields are invalid.</p>
     *
     * @param exception validation exception thrown by Spring
     * @param request   current HTTP request
     * @return standardized validation error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest()
                .body(ApiError.validation(
                        "Request validation failed",
                        HttpStatus.BAD_REQUEST.value(),
                        request.getRequestURI(),
                        errors
                ));
    }

    /**
     * Handles unexpected exceptions.
     *
     * <p>This method acts as a fallback handler for all unhandled exceptions.</p>
     *
     * @param exception unexpected exception
     * @param request   current HTTP request
     * @return standardized internal server error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        return ResponseEntity.internalServerError()
                .body(ApiError.of(
                        ErrorCode.INTERNAL_SERVER_ERROR.name(),
                        exception.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        request.getRequestURI()
                ));
    }

}