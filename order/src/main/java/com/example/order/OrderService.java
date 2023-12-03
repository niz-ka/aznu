package com.example.order;

import org.springframework.stereotype.Service;

@Service
public class OrderService {

    public String processOrder(String message) {
        return message + " ordered!";
    }
}
