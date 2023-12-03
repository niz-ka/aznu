package com.example.gateway.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderProduct {
    private String name;
    private Integer amount;
}
