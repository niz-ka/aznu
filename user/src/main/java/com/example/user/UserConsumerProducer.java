package com.example.user;

import com.example.user.model.OnlineOrderRequest;
import com.example.user.model.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserConsumerProducer {
    private final static Logger logger = LoggerFactory.getLogger(UserConsumerProducer.class);
    private final UserService orderService;
    private final KafkaTemplate<String, UserResponse> kafkaTemplate;

    @Value(value = "${spring.kafka.output-topic}")
    private String outputTopic;
    @Value(value = "${spring.kafka.input-topic}")
    private String inputTopic;

    public UserConsumerProducer(
            UserService orderService,
            KafkaTemplate<String, UserResponse> kafkaTemplate) {
        this.orderService = orderService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "${spring.kafka.input-topic}")
    public void listenTopicTopic(OnlineOrderRequest request) {
        logger.info("Message received from topic {} with content {}", inputTopic, request);
        UserResponse processed = orderService.processUser(request);

        kafkaTemplate.send(outputTopic, processed);
        logger.info("Message sent to topic {} with content {}", outputTopic, processed);
    }
}
