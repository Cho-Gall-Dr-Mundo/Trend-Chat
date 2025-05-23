package com.trendchat.userservice.service;

import com.trendchat.userservice.dto.UserRequest;
import com.trendchat.userservice.dto.UserResponse;

public interface AuthService {

    UserResponse.Get createUser(UserRequest.Signup userRequest);
}
