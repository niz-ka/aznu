package com.example.gateway.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderServiceResponse {
    private String id;
    private String status;
}
