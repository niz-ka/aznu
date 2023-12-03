package com.example.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumerProducer {
    private final static Logger logger = LoggerFactory.getLogger(OrderConsumerProducer.class);
    private final OrderService orderService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${spring.kafka.output-topic}")
    private String outputTopic;
    @Value(value = "${spring.kafka.input-topic}")
    private String inputTopic;

    public OrderConsumerProducer(
            OrderService orderService,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.orderService = orderService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "${spring.kafka.input-topic}")
    public void listenTopicTopic(String message) {
        logger.info("Message received from topic {} with content {}", inputTopic, message);
        String processed = orderService.processOrder(message);

        kafkaTemplate.send(outputTopic, processed);
        logger.info("Message sent to topic {} with content {}", outputTopic, processed);
    }
}
