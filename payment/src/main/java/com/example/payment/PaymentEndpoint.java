package com.example.payment;

import com.example.payment.model.GetCompensationRequest;
import com.example.payment.model.GetCompensationResponse;
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

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getCompensationRequest")
    @ResponsePayload
    public GetCompensationResponse getCompensation(@RequestPayload GetCompensationRequest compensationRequest) {
        GetCompensationResponse response = new GetCompensationResponse();
        response.setId(compensationRequest.getId());
        response.setMessage(String.format("PaymentService compensation with id %s", compensationRequest.getId()));

        return response;
    }
}
