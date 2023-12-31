package com.example.gateway;

import com.example.gateway.model.*;
import com.example.gateway.xml.model.GetCompensationRequest;
import com.example.gateway.xml.model.GetCompensationResponse;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.gateway.xml.model.GetPaymentResponse;
import com.example.gateway.xml.model.GetPaymentRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class GatewayRouter extends RouteBuilder {

    private final static Logger logger = LoggerFactory.getLogger(GatewayRouter.class);

    @Value("${kafka.host}")
    private String kafkaHost;

    @Value("${payment.host}")
    private String paymentHost;


    @Override
    public void configure() throws Exception {
        api();
        postOnlineOrder();
        getOnlineOrder();
    }

    public void api() {
        restConfiguration()
                .enableCORS(true)
                .contextPath("/api")
                .apiContextPath("/docs")
                .apiProperty("api.title", "Online Shopping Platform API docs")
                .apiProperty("api.version", "1.0.0")
                .apiProperty("cors", "true");

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
                .to(String.format("sql:INSERT INTO ONLINE_ORDERS VALUES (:#${header.id}, '%s', '%s', '%s', 'F', 'F', 'F', 'F')",
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
                .to(String.format("spring-ws:http://%s/ws", paymentHost))
                .unmarshal(new JaxbDataFormat(GetPaymentResponse.class.getPackage().getName()))
                .to("sql:UPDATE ONLINE_ORDERS SET PAYMENT_STATUS = :#${body.status} WHERE id = :#${header.id}")
                .choice()
                .when(simple("${body.status} == 'failed'"))
                .to("direct:compensation");

        // Send request to OrderService and UserService via Kafka
        from("direct:sendMessageToBroker")
                .marshal().json()
                .to(String.format("kafka:input-topic?brokers=%s&valueSerializer=org.apache.kafka.common.serialization.ByteArraySerializer", kafkaHost));

        // Receive responses from OrderService via Kafka
        from(String.format("kafka:output-topic-order?brokers=%s&groupId=gateway&valueDeserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer", kafkaHost))
                .unmarshal(new JacksonDataFormat(OrderServiceResponse.class))
                .to("sql:UPDATE ONLINE_ORDERS SET ORDER_STATUS = :#${body.status} WHERE id = :#${body.id}")
                .choice()
                .when(simple("${body.status} == 'failed'"))
                .to("direct:compensation");

        // Receive responses from UserService via Kafka
        from(String.format("kafka:output-topic-user?brokers=%s&groupId=gateway&valueDeserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer", kafkaHost))
                .unmarshal(new JacksonDataFormat(UserServiceResponse.class))
                .to("sql:UPDATE ONLINE_ORDERS SET USER_STATUS = :#${body.status} WHERE id = :#${body.id}")
                .choice()
                .when(simple("${body.status} == 'failed'"))
                .to("direct:compensation");

        // Compensation
        from("direct:compensation")
                .transacted()
                .to("sql:SELECT ID, IS_COMPENSATED FROM ONLINE_ORDERS WHERE ID = :#${body.id} FOR UPDATE")
                .choice()
                .when(simple("${body.get(0).get('IS_COMPENSATED')} == 'F'"))
                .process(exchange -> {
                    List<Map<String, Object>> body = (List<Map<String, Object>>) exchange.getMessage().getBody();
                    exchange.getMessage().setHeader("id", body.getFirst().get("ID"));
                })
                .log("##### Compensation started for ${header.id}")
                .setBody(exchange -> GetCompensationRequest.construct(exchange.getMessage().getHeader("id", String.class)))
                .marshal().jaxb()
                .to(String.format("spring-ws:http://%s/ws", paymentHost))
                .unmarshal(new JaxbDataFormat(GetCompensationResponse.class.getPackage().getName()))
                .log("##### Compensation response for ${body.id} - ${body.message}")
                .process(exchange -> {
                    CompensationRequest compensationRequest = new CompensationRequest();
                    compensationRequest.setId(exchange.getMessage().getHeader("id", String.class));
                    exchange.getMessage().setBody(compensationRequest);
                })
                .log("BODY BODY: ${body}")
                .marshal(new JacksonDataFormat(CompensationRequest.class))
                .to(String.format("kafka:input-compensation-topic?brokers=%s&valueSerializer=org.apache.kafka.common.serialization.ByteArraySerializer", kafkaHost))
                .to("sql:UPDATE ONLINE_ORDERS SET IS_COMPENSATED = 'T', PAYMENT_COMPENSATED = 'T' WHERE ID = :#${header.id}");

        from(String.format("kafka:output-compensation-topic?brokers=%s&groupId=gateway&valueDeserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer", kafkaHost))
                .unmarshal(new JacksonDataFormat(CompensationResponse.class))
                .log("##### Compensation response for ${body.id} - ${body.message}")
                .choice()
                .when(simple("${body.message.toLowerCase().contains('order')}"))
                .to("sql:UPDATE ONLINE_ORDERS SET ORDER_COMPENSATED = 'T' WHERE ID = :#${body.id}")
                .otherwise()
                .to("sql:UPDATE ONLINE_ORDERS SET USER_COMPENSATED = 'T' WHERE ID = :#${body.id}");
    }

    public void getOnlineOrder() {
        from("direct:get-shopping")
                .to("sql:SELECT ID, USER_STATUS, ORDER_STATUS, PAYMENT_STATUS, USER_COMPENSATED, ORDER_COMPENSATED, PAYMENT_COMPENSATED FROM ONLINE_ORDERS WHERE id = :#${header.id}")
                .setBody(exchange -> StatusResponse.fromDatabase((List<Map<String, Object>>) exchange.getMessage().getBody()));
    }

}