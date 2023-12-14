package com.example.user;

import com.example.user.model.CompensationRequest;
import com.example.user.model.OnlineOrderRequest;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;
    @Value(value = "${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, OnlineOrderRequest> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                groupId);
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, "false");
        JsonDeserializer<OnlineOrderRequest> jsonDeserializer = new JsonDeserializer<>(OnlineOrderRequest.class);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OnlineOrderRequest>
    kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OnlineOrderRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, CompensationRequest> compensationConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                "consumer-user-2");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, "false");
        JsonDeserializer<CompensationRequest> jsonDeserializer = new JsonDeserializer<>(CompensationRequest.class);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CompensationRequest>
    compensationKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CompensationRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(compensationConsumerFactory());
        return factory;
    }
}