package com.trendchat.userservice.service;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    void existsByEmail(String email);

    void lockAccount(String userId);
}
