package com.trendchat.userservice.service;

import com.trendchat.userservice.dto.UserResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    void existsByEmail(String email);

    void lockAccount(String userId);

    UserResponse.Get getUser(String userId);
}
