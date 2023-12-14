package com.example.gateway.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class StatusResponse {
    private String id;
    private String userStatus;
    private String orderStatus;
    private String paymentStatus;
    private String isUserCompensated;
    private String isOrderCompensated;
    private String isPaymentCompensated;

    public static StatusResponse fromDatabase(List<Map<String, Object>> rows) {
        Map<String, Object> row = rows.getFirst();
        StatusResponse response = new StatusResponse();
        response.setId((String) row.get("ID"));
        response.setOrderStatus((String) row.get("ORDER_STATUS"));
        response.setPaymentStatus((String) row.get("PAYMENT_STATUS"));
        response.setUserStatus((String) row.get("USER_STATUS"));
        response.setIsOrderCompensated((String) row.get("ORDER_COMPENSATED"));
        response.setIsPaymentCompensated((String) row.get("PAYMENT_COMPENSATED"));
        response.setIsUserCompensated((String) row.get("USER_COMPENSATED"));
        return response;
    }
}
