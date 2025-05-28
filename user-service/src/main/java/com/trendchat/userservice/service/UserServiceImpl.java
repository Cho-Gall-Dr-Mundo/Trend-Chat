package com.trendchat.userservice.service;

import com.trendchat.userservice.dto.UserResponse;
import com.trendchat.userservice.entity.User;
import com.trendchat.userservice.repository.UserRepository;
import com.trendchat.userservice.security.PrincipalDetails;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
    public PrincipalDetails loadUserByUsername(String email) {
        return new PrincipalDetails(isValidEmail(email));
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void lockAccount(String userId) {
        User user = isValidUser(userId);
        user.accountLock();
    }

    @Override
    public UserResponse.Get getUser(String userId) {
        return new UserResponse.Get(isValidUser(userId));
    }

    private User isValidEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("User not found with email: {}", email);
            return new UsernameNotFoundException("User not found");
        });
    }

    private User isValidUser(String userId) {
        return userRepository.findByUserId(userId).orElseThrow(() -> {
            log.error("User not found with userId: {}", userId);
            return new NotFoundException("User not found");
        });
    }
}
