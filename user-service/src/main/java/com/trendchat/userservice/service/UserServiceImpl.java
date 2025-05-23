package com.trendchat.userservice.service;

import com.trendchat.userservice.entity.User;
import com.trendchat.userservice.repository.UserRepository;
import com.trendchat.userservice.security.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public void existsByEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            log.error("Duplicate email requests occurred: email='{}'", email);
            throw new IllegalStateException("Already exists email");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return new PrincipalDetails(isValidEmail(email));
    }

    @Override
    public User isValidEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("User not found with email: {}", email);
            return new UsernameNotFoundException("User not found");
        });
    }
}
