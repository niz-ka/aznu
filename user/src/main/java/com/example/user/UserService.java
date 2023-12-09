package com.example.user;

import com.example.user.model.OnlineOrderRequest;
import com.example.user.model.UserResponse;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public UserResponse processUser(OnlineOrderRequest request) {
        boolean isEmailForbidden = request.getUser().getEmail().endsWith(".ru");
        UserResponse response = new UserResponse();
        response.setId(request.getId());
        response.setStatus(isEmailForbidden ? "failed" : "completed");

        return response;
    }
}
