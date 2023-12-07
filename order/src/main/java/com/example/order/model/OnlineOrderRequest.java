package com.example.order.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class OnlineOrderRequest {
    private String id;
    private Payment payment;
    private List<OrderProduct> order;
    private User user;
}
