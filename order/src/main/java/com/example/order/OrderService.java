package com.example.order;

import com.example.order.model.OnlineOrderRequest;
import com.example.order.model.OrderResponse;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class OrderService {

    public OrderResponse processOrder(OnlineOrderRequest request) {

        Set<String> forbiddenProducts = new HashSet<>();
        forbiddenProducts.add("beer");
        forbiddenProducts.add("vodka");
        forbiddenProducts.add("wine");

        boolean orderContainsForbiddenProducts = request.getOrder().stream()
                .anyMatch(product -> forbiddenProducts.contains(product.getName().toLowerCase()));

        OrderResponse response = new OrderResponse();
        response.setId(request.getId());
        response.setStatus(orderContainsForbiddenProducts ? "failed" : "completed");

        return response;
    }
}
