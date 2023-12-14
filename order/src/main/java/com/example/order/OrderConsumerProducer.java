package com.example.order;

import com.example.order.model.CompensationRequest;
import com.example.order.model.CompensationResponse;
import com.example.order.model.OnlineOrderRequest;
import com.example.order.model.OrderResponse;
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
    private final KafkaTemplate<String, OrderResponse> kafkaTemplate;
    private final KafkaTemplate<String, CompensationResponse> compensationKafkaTemplate;

    @Value(value = "${spring.kafka.output-topic}")
    private String outputTopic;
    @Value(value = "${spring.kafka.input-topic}")
    private String inputTopic;

    @Value(value = "${spring.kafka.compensation-input-topic}")
    private String compensationInputTopic;
    @Value(value = "${spring.kafka.compensation-output-topic}")
    private String compensationOutputTopic;

    public OrderConsumerProducer(
            OrderService orderService,
            KafkaTemplate<String, OrderResponse> kafkaTemplate,
            KafkaTemplate<String, CompensationResponse> compensationKafkaTemplate) {
        this.orderService = orderService;
        this.kafkaTemplate = kafkaTemplate;
        this.compensationKafkaTemplate = compensationKafkaTemplate;
    }

    @KafkaListener(topics = "${spring.kafka.input-topic}", containerFactory = "kafkaListenerContainerFactory")
    public void listenTopicTopic(OnlineOrderRequest request) {
        logger.info("Message received from topic {} with content {}", inputTopic, request);
        OrderResponse processed = orderService.processOrder(request);

        kafkaTemplate.send(outputTopic, processed);
        logger.info("Message sent to topic {} with content {}", outputTopic, processed);
    }

    @KafkaListener(topics = "${spring.kafka.compensation-input-topic}", containerFactory = "compensationKafkaListenerContainerFactory")
    public void listenCompensation(CompensationRequest request) {
        logger.info("Message received from topic {} with content {}", compensationInputTopic, request);
        CompensationResponse response = new CompensationResponse();
        response.setId(request.getId());
        response.setMessage(String.format("OrderService compensation with id %s", request.getId()));

        compensationKafkaTemplate.send(compensationOutputTopic, response);
    }
}
