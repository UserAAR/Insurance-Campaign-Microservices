package com.casestudy.insurancecampaign.common.observability;

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
 * Configures HTTP server observations for metrics and distributed tracing.
 *
 * <p>This configuration improves trace readability by renaming HTTP server spans
 * with meaningful route names such as {@code http get /api/v1/campaigns}.
 * It also excludes actuator endpoints from tracing to reduce noise in Tempo.</p>
 */
@Configuration
public class HttpServerObservationConfiguration {

    private static final String HTTP_SERVER_REQUESTS = "http.server.requests";
    private static final Set<String> EMPTY_ROUTES = Set.of("UNKNOWN", "root", "/**", "none", "");

    /**
     * Ignores actuator HTTP observations.
     *
     * <p>Actuator endpoints such as {@code /actuator/prometheus} are called
     * frequently by Prometheus. Ignoring them prevents monitoring traffic from
     * polluting business traces in Grafana Tempo.</p>
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
     * Renames HTTP server span names using method and route information.
     *
     * <p>This makes traces easier to search in Grafana Tempo. Instead of generic
     * span names such as {@code http get}, spans can appear as
     * {@code http get /api/v1/dashboard/classifieds/statistics}.</p>
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
     * Reads a low-cardinality key value from the observation context.
     *
     * @param context observation context
     * @param key     key to search
     * @return matching value, or {@code null} if not found
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
     * Extracts the request path from the observation carrier.
     *
     * <p>The carrier type may differ depending on whether the application uses
     * Servlet MVC or WebFlux. Reflection is used here to support both styles
     * without directly depending on a specific request implementation.</p>
     *
     * @param context observation context
     * @return request path, or {@code null} if it cannot be detected
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
     * Invokes a no-argument method using reflection.
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
     * Returns the first non-blank value from the provided values.
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
     * @return trimmed value, or {@code null} if the value is blank
     */
    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

}