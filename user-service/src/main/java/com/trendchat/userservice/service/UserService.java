package com.trendchat.userservice.service;

import com.trendchat.userservice.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    void existsByEmail(String email);

    User isValidEmail(String email);
}
