package com.example.gateway.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class Payment {
    private String cardNumber;
    private String paymentId;
    private BigDecimal amount;
}
