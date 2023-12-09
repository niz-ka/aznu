package com.example.payment;

import com.example.payment.model.GetPaymentRequest;
import com.example.payment.model.GetPaymentResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.math.BigDecimal;

@Endpoint
public class PaymentEndpoint {
    private static final String NAMESPACE_URI = "http://model.payment.example.com";

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getPaymentRequest")
    @ResponsePayload
    public GetPaymentResponse getPayment(@RequestPayload GetPaymentRequest paymentRequest) {
        BigDecimal amount = paymentRequest.getAmount();
        GetPaymentResponse response = new GetPaymentResponse();
        response.setId(paymentRequest.getId());
        response.setStatus(amount.compareTo(new BigDecimal("15000")) > 0 ? "failed" : "completed");

        return response;
    }
}
