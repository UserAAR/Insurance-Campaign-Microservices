package com.casestudy.insurancecampaign.common.exception;

/**
 * Exception thrown when a campaign status transition is not allowed.
 *
 * <p>For example, a campaign that is already deactivated may not be allowed to
 * become active again depending on the business rules.</p>
 */
public class InvalidStatusTransitionException extends BusinessException {

    /**
     * Creates an invalid status transition exception.
     *
     * @param message human-readable error message
     */
    public InvalidStatusTransitionException(String message) {
        super(ErrorCode.INVALID_STATUS_TRANSITION, message);
    }

}
