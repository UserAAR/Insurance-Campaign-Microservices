package com.casestudy.insurancecampaign.common.exception;

/**
 * Exception thrown when a requested resource cannot be found.
 *
 * <p>This exception is commonly used when an entity lookup by ID does not return
 * any result.</p>
 */
public class ResourceNotFoundException extends BusinessException {

    /**
     * Creates a resource not found exception.
     *
     * @param message human-readable error message
     */
    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
    
}
