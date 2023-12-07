package com.example.order;

import com.example.order.model.OnlineOrderRequest;
import com.example.order.model.OrderResponse;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    public OrderResponse processOrder(OnlineOrderRequest request) {
        OrderResponse response = new OrderResponse();
        response.setId(request.getId());
        response.setStatus("completed");
        return response;
    }
}
