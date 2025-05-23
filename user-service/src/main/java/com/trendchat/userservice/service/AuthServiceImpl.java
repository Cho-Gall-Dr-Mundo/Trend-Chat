package com.trendchat.userservice.service;

import com.trendchat.userservice.dto.UserRequest.Signup;
import com.trendchat.userservice.dto.UserResponse;
import com.trendchat.userservice.entity.User;
import com.trendchat.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse.Get createUser(Signup userRequest) {
        userService.existsByEmail(userRequest.email());

        String encodedPassword = passwordEncoder.encode(userRequest.password());
        User newUser = User.ofSignup(userRequest, encodedPassword);

        User savedUser = userRepository.save(newUser);

        return new UserResponse.Get(savedUser);
    }
}
