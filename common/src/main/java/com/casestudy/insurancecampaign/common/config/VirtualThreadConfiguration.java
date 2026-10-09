package com.casestudy.insurancecampaign.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;

/**
 * Configures virtual thread support for asynchronous task execution.
 *
 * <p>This configuration is enabled only when the property
 * {@code spring.threads.virtual.enabled=true} is defined.</p>
 *
 * <p>Virtual threads are useful for I/O-heavy workloads because they allow the
 * application to handle many concurrent tasks with lower resource usage compared
 * to traditional platform threads.</p>
 */
@Configuration
@EnableAsync
@ConditionalOnProperty(name = "spring.threads.virtual.enabled", havingValue = "true")
public class VirtualThreadConfiguration {

    /**
     * Creates the main application task executor backed by Java virtual threads.
     *
     * <p>Spring uses this bean for asynchronous execution when methods are
     * annotated with {@code @Async}.</p>
     *
     * @return virtual-thread-based {@link AsyncTaskExecutor}
     */
    @Bean(name = "applicationTaskExecutor")
    public AsyncTaskExecutor applicationTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

}
