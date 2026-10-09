package com.casestudy.insurancecampaign.auth.util;

import lombok.experimental.UtilityClass;

import java.util.Locale;

/**
 * Utility class for common String operations used across the authentication module.
 *
 * <p>
 * This class provides helper methods for preparing text values before comparison,
 * lookup, or persistence operations. For example, usernames and email addresses
 * can be normalized to keep authentication and search operations consistent.
 * </p>
 *
 * <p>
 * Since this class is annotated with {@link UtilityClass}, it cannot be instantiated
 * and all members are treated as static utility members by Lombok.
 * </p>
 */
@UtilityClass
public final class StringUtils {

    /**
     * Normalizes the given text value by trimming leading/trailing whitespace
     * and converting it to lowercase using {@link Locale#ROOT}.
     *
     * <p>
     * This method is commonly used before username or email comparison/search
     * operations to keep lookups case-insensitive and consistent.
     * </p>
     *
     * @param value text value to normalize
     * @return normalized value
     */
    public String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

}
