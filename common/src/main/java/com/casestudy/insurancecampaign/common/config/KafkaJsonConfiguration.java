package com.casestudy.insurancecampaign.common.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configures Kafka producer support for publishing JSON messages.
 *
 * <p>This configuration is only enabled when {@link KafkaTemplate} is available
 * on the classpath. It creates a reusable {@link ProducerFactory} and
 * {@link KafkaTemplate} for sending JSON-based Kafka messages.</p>
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.kafka.core.KafkaTemplate")
public class KafkaJsonConfiguration {

    /**
     * Creates a Kafka producer factory configured with String keys and JSON values.
     *
     * <p>The Kafka bootstrap server address is read from
     * {@code spring.kafka.bootstrap-servers}. If the property is not defined,
     * {@code localhost:9092} is used as the default value.</p>
     *
     * <p>Type information headers are disabled to keep Kafka messages generic and
     * easier to consume by other services.</p>
     *
     * @param bootstrapServers Kafka bootstrap server address
     * @return configured {@link ProducerFactory} for Kafka JSON messages
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory(@Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * Creates a Kafka template for sending messages to Kafka topics.
     *
     * @param producerFactory configured Kafka producer factory
     * @return {@link KafkaTemplate} for publishing JSON messages
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

}
