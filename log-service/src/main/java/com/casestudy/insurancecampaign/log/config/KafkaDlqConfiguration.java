package com.casestudy.insurancecampaign.log.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;
import org.apache.kafka.common.TopicPartition;

/**
 * Kafka configuration for retry and Dead Letter Queue handling.
 */
@Configuration
public class KafkaDlqConfiguration {

    /**
     * Creates an error handler that retries failed records and sends them to a DLQ topic.
     *
     * @param kafkaTemplate Kafka template used for DLQ publishing
     * @return configured Kafka error handler
     */
    @Bean
    public DefaultErrorHandler defaultErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (ConsumerRecord<?, ?> record, Exception exception) -> new TopicPartition(record.topic() + ".DLQ", record.partition())
        );
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1_000L, 3));
    }

    /**
     * Creates the Kafka listener factory with custom error handling.
     *
     * @param consumerFactory Kafka consumer factory
     * @param defaultErrorHandler retry and DLQ error handler
     * @return configured Kafka listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            DefaultErrorHandler defaultErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(defaultErrorHandler);
        return factory;
    }

}
