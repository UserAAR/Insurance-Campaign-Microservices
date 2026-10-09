package com.casestudy.insurancecampaign.discovery.observability;

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
 * Observation configuration for Discovery Server HTTP requests.
 *
 * <p>
 * This configuration customizes Micrometer observations created for HTTP server requests.
 * It is mainly used to reduce noisy traces and make span names more readable in tracing tools
 * such as Grafana Tempo.
 * </p>
 *
 * <p>
 * Main responsibilities:
 * </p>
 *
 * <ul>
 *     <li>Ignore Actuator endpoints such as {@code /actuator/health} and {@code /actuator/prometheus}.</li>
 *     <li>Set cleaner HTTP span names using method and route information.</li>
 *     <li>Fallback to request path when route information is not available.</li>
 * </ul>
 */
@Configuration
public class DiscoveryServerHttpServerObservationConfiguration {

    private static final String HTTP_SERVER_REQUESTS = "http.server.requests";
    private static final Set<String> EMPTY_ROUTES = Set.of("UNKNOWN", "root", "/**", "none", "");

    /**
     * Creates an observation predicate that skips Actuator HTTP server observations.
     *
     * <p>
     * Actuator endpoints are frequently called by health checks and Prometheus scraping.
     * Excluding them keeps distributed traces cleaner and easier to analyze.
     * </p>
     *
     * @return predicate that allows non-Actuator observations and ignores Actuator request observations
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
     * Creates an observation filter that improves HTTP server span names.
     *
     * <p>
     * The filter uses HTTP method and route information from low-cardinality key values.
     * If the route is missing, it falls back to the raw request path.
     * </p>
     *
     * <p>
     * Example span name:
     * </p>
     *
     * <pre>
     * http get /eureka/apps
     * </pre>
     *
     * @return observation filter that sets contextual names for HTTP server observations
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
     * Finds a low-cardinality value from the observation context by key.
     *
     * @param context observation context
     * @param key     low-cardinality key to search for
     * @return matching value, or {@code null} if the key does not exist or the value is blank
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
     * Different request implementations expose URI information with different method names.
     * This method uses reflection to support common carrier methods such as:
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
     * Invokes a no-argument method on the target object.
     *
     * <p>
     * This helper is intentionally tolerant. If the method does not exist or invocation fails,
     * it returns {@code null} instead of throwing an exception.
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
     * Converts {@code null} or blank strings to {@code null}.
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
