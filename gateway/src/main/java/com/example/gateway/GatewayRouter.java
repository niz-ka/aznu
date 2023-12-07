package com.example.gateway;

import com.example.gateway.model.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.example.gateway.xml.model.GetPaymentResponse;
import com.example.gateway.xml.model.GetPaymentRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class GatewayRouter extends RouteBuilder {

    private final static Logger logger = LoggerFactory.getLogger(GatewayRouter.class);

    @Override
    public void configure() throws Exception {
        api();
        postOnlineOrder();
        getOnlineOrder();
    }

    public void api() {
        rest("/shopping")
                .post()
                .description("Submit Online Order")
                .consumes("application/json")
                .produces("application/json")
                .type(OnlineOrderRequest.class)
                .outType(OnlineOrderSubmittedResponse.class)
                .bindingMode(RestBindingMode.json)
                .to("direct:post-shopping")

                .get("/{id}")
                .description("Get Online Order")
                .consumes("application/json")
                .produces("application/json")
                .outType(StatusResponse.class)
                .bindingMode(RestBindingMode.json)
                .to("direct:get-shopping");
    }

    public void postOnlineOrder() {
        from("direct:post-shopping")
                .log("POST shopping: ${body}")
                .process(exchange -> {
                    String id = UUID.randomUUID().toString();
                    OnlineOrderRequest onlineOrderRequest = exchange.getMessage().getBody(OnlineOrderRequest.class);
                    onlineOrderRequest.setId(id);
                    exchange.getMessage().setHeader("id", id);
                    exchange.getMessage().setBody(onlineOrderRequest);
                })
                .to(String.format("sql:INSERT INTO ONLINE_ORDERS VALUES (:#${header.id}, '%s', '%s', '%s')",
                        OnlineOrderStatus.SUBMITTED,
                        OnlineOrderStatus.SUBMITTED,
                        OnlineOrderStatus.SUBMITTED))
                .wireTap("direct:processOnlineOrder")
                .to("direct:responseWithId");

        from("direct:responseWithId")
                .setBody(exchange -> OnlineOrderSubmittedResponse.construct((String) exchange.getMessage().getHeader("id")));

        from("direct:processOnlineOrder")
                .wireTap("direct:callPaymentService")
                .to("direct:sendMessageToBroker");

        // Send request and receive response from PaymentService
        from("direct:callPaymentService")
                .setBody(exchange -> GetPaymentRequest.construct(exchange.getMessage().getBody(OnlineOrderRequest.class)))
                .marshal().jaxb()
                .to("spring-ws:http://localhost:8082/ws")
                .unmarshal(new JaxbDataFormat(GetPaymentResponse.class.getPackage().getName()))
                .to("sql:UPDATE ONLINE_ORDERS SET PAYMENT_STATUS = :#${body.status} WHERE id = :#${header.id}");

        // Send request to OrderService and UserService via Kafka
        from("direct:sendMessageToBroker")
                .log("${body}")
                .marshal().json()
                .to("kafka:input-topic?brokers=127.0.0.1:9092&valueSerializer=org.apache.kafka.common.serialization.ByteArraySerializer");

        // Receive responses from OrderService via Kafka
        from("kafka:output-topic-order?brokers=127.0.0.1:9092&groupId=gateway&valueDeserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer")
                .unmarshal(new JacksonDataFormat(OrderServiceResponse.class))
                .to("sql:UPDATE ONLINE_ORDERS SET ORDER_STATUS = :#${body.status} WHERE id = :#${body.id}");

        // Receive responses from UserService via Kafka
        from("kafka:output-topic-user?brokers=127.0.0.1:9092&groupId=gateway&valueDeserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer")
                .unmarshal(new JacksonDataFormat(UserServiceResponse.class))
                .to("sql:UPDATE ONLINE_ORDERS SET USER_STATUS = :#${body.status} WHERE id = :#${body.id}");
    }

    public void getOnlineOrder() {
        from("direct:get-shopping")
                .to("sql:SELECT ID, USER_STATUS, ORDER_STATUS, PAYMENT_STATUS FROM ONLINE_ORDERS WHERE id = :#${header.id}")
                .setBody(exchange -> StatusResponse.fromDatabase((List<Map<String, Object>>) exchange.getMessage().getBody()));
    }

}