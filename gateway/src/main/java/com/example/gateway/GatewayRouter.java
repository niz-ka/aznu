package com.example.gateway;

import com.example.gateway.model.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.example.gateway.xml.model.GetPaymentResponse;
import com.example.gateway.xml.model.GetPaymentRequest;

import java.util.UUID;

@Component
public class GatewayRouter extends RouteBuilder {

    private final static Logger logger = LoggerFactory.getLogger(GatewayRouter.class);

    @Override
    public void configure() throws Exception {
        api();
        postOnlineOrder();
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
                .wireTap("direct:processOnlineOrder")
                .to("direct:responseWithId");

        from("direct:responseWithId")
                .to(String.format("sql:INSERT INTO ONLINE_ORDERS VALUES (:#${header.id}, '%s')", OnlineOrderStatus.SUBMITTED))
                .setBody(exchange -> OnlineOrderSubmittedResponse.construct((String) exchange.getMessage().getHeader("id")))
                .marshal().json();

        from("direct:processOnlineOrder")
                .wireTap("direct:callPaymentService")
                .to("direct:sendMessageToBroker");

        from("direct:callPaymentService")
                .setBody(exchange -> GetPaymentRequest.construct(exchange.getMessage().getBody(OnlineOrderRequest.class)))
                .marshal().jaxb()
                .log("Body is ${body}")
                .to("spring-ws:http://localhost:8082/ws")
                .unmarshal(new JaxbDataFormat(GetPaymentResponse.class.getPackage().getName()))
                .log("Response: ${body}");

        from("direct:sendMessageToBroker")
                .to("log:dummy");

        // Receive responses from UserService and OrderService
//        from("kafka:output-topic?brokers=127.0.0.1:9092&groupId=gateway")
//                .to("log:my-log");
    }

}