package com.casestudy.insurancecampaign.config.observability;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;
import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Locale;
import java.util.Set;

/**
 * Observation configuration for the Config Server HTTP server requests.
 *
 * <p>
 * This configuration customizes Micrometer observations for HTTP server spans.
 * It has two main responsibilities:
 * </p>
 *
 * <ul>
 *     <li>Ignore noisy Actuator HTTP observations such as {@code /actuator/health}
 *     and {@code /actuator/prometheus}.</li>
 *     <li>Improve HTTP span names by using a readable format such as
 *     {@code http get /config-server/default}.</li>
 * </ul>
 *
 * <p>
 * This helps Grafana Tempo display cleaner and more meaningful traces.
 * </p>
 */
@Configuration
public class ConfigServerHttpServerObservationConfiguration {

    private static final String HTTP_SERVER_REQUESTS = "http.server.requests";
    private static final Set<String> EMPTY_ROUTES = Set.of("UNKNOWN", "root", "/**", "none", "");

    /**
     * Creates an {@link ObservationPredicate} that ignores Actuator HTTP server observations.
     *
     * <p>
     * Only observations named {@code http.server.requests} are checked.
     * If the request path starts with {@code /actuator}, the observation is ignored.
     * All other observations are allowed.
     * </p>
     *
     * @return observation predicate used to filter HTTP observations
     */
    @Bean
    public ObservationPredicate ignoreActuatorHttpServerObservations() {
        return (name, context) -> {
            if (!HTTP_SERVER_REQUESTS.equals(name)) {
                return true;
            }
            String path = requestPath(context);
            return path == null || !path.startsWith("/actuator");
        };
    }

    /**
     * Creates an {@link ObservationFilter} that improves HTTP server span names.
     *
     * <p>
     * The filter reads the HTTP method and route from low-cardinality observation
     * key-values. If route information is not available, it falls back to the raw
     * request path.
     * </p>
     *
     * <p>
     * Example generated contextual name:
     * </p>
     *
     * <pre>
     * http get /config-server/default
     * </pre>
     *
     * @return observation filter used to customize HTTP span names
     */
    @Bean
    public ObservationFilter httpServerRouteSpanNameObservationFilter() {
        return context -> {
            if (!HTTP_SERVER_REQUESTS.equals(context.getName())) {
                return context;
            }

            String method = firstNonBlank(
                    lowCardinalityValue(context, "method"),
                    lowCardinalityValue(context, "http.request.method"),
                    "http"
            ).toLowerCase(Locale.ROOT);

            String route = firstNonBlank(
                    lowCardinalityValue(context, "uri"),
                    lowCardinalityValue(context, "http.route"),
                    requestPath(context)
            );

            if (route != null && !EMPTY_ROUTES.contains(route)) {
                context.setContextualName("http " + method + " " + route);
            }

            return context;
        };
    }

    /**
     * Finds a low-cardinality key-value from the given observation context.
     *
     * @param context observation context
     * @param key     key to search for
     * @return matching value, or {@code null} if the key does not exist or value is blank
     */
    private static String lowCardinalityValue(Observation.Context context, String key) {
        for (KeyValue keyValue : context.getLowCardinalityKeyValues()) {
            if (key.equals(keyValue.getKey())) {
                return blankToNull(keyValue.getValue());
            }
        }
        return null;
    }

    /**
     * Extracts the request path from the observation context carrier.
     *
     * <p>
     * Different servlet/reactive request implementations expose URI data through
     * different methods. For this reason, this method tries to read the path using
     * reflection from common no-argument methods such as:
     * </p>
     *
     * <ul>
     *     <li>{@code getRequestURI()}</li>
     *     <li>{@code getURI()}</li>
     * </ul>
     *
     * @param context observation context
     * @return request path, or {@code null} if it cannot be resolved
     */
    private static String requestPath(Observation.Context context) {
        Object carrier = invokeNoArg(context, "getCarrier");
        if (carrier == null) {
            return null;
        }

        Object requestUri = invokeNoArg(carrier, "getRequestURI");
        if (requestUri instanceof String value) {
            return blankToNull(value);
        }

        Object uri = invokeNoArg(carrier, "getURI");
        if (uri instanceof URI value) {
            return blankToNull(value.getPath());
        }
        if (uri != null) {
            return blankToNull(uri.toString());
        }

        return null;
    }

    /**
     * Invokes a no-argument method on the given target object using reflection.
     *
     * <p>
     * This method is intentionally tolerant. If the method does not exist or
     * invocation fails, it returns {@code null} instead of throwing an exception.
     * </p>
     *
     * @param target     target object
     * @param methodName method name to invoke
     * @return method result, or {@code null} if invocation fails
     */
    private static Object invokeNoArg(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Returns the first non-blank value from the given values.
     *
     * @param values candidate values
     * @return first non-blank value, or {@code null} if all values are blank
     */
    private static String firstNonBlank(String... values) {
        for (String value : values) {
            String normalized = blankToNull(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    /**
     * Converts blank strings to {@code null}.
     *
     * @param value value to normalize
     * @return trimmed value, or {@code null} if the value is {@code null} or blank
     */
    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

}
