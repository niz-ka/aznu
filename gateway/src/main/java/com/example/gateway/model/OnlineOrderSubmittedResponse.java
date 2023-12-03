package com.example.gateway.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class OnlineOrderSubmittedResponse {
    private String id;
    private String message;
    private String timestamp;

    public static OnlineOrderSubmittedResponse construct(String id) {
        OnlineOrderSubmittedResponse response = new OnlineOrderSubmittedResponse();
        response.setId(id);
        response.setMessage("Online order was submitted successfully");
        response.setTimestamp(Instant.now().toString());

        return response;
    }
}
