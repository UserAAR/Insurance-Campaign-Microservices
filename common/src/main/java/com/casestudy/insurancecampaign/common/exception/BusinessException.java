package com.casestudy.insurancecampaign.common.exception;

/**
 * Base exception type for business rule violations.
 *
 * <p>This exception carries an {@link ErrorCode}, allowing the global exception
 * handler to map business errors to consistent API error responses.</p>
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * Creates a new business exception.
     *
     * @param errorCode application-specific error code
     * @param message   human-readable error message
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Returns the application-specific error code.
     *
     * @return error code associated with this exception
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
