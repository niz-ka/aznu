package com.example.user;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    public String processUser(String message) {
        return message + " usered!";
    }
}
