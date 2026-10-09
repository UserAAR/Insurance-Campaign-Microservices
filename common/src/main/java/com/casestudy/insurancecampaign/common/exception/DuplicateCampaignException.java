package com.casestudy.insurancecampaign.common.exception;

/**
 * Exception thrown when a duplicate campaign is detected.
 *
 * <p>A campaign is considered duplicate when another campaign already exists
 * with the same category, title, and description.</p>
 */
public class DuplicateCampaignException extends BusinessException {

    /**
     * Creates a duplicate campaign exception.
     *
     * @param message human-readable error message
     */
    public DuplicateCampaignException(String message) {
        super(ErrorCode.DUPLICATE_CAMPAIGN, message);
    }

}
