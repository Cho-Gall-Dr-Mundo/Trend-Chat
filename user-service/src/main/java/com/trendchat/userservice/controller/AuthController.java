package com.trendchat.userservice.controller;

import com.trendchat.trendchatcommon.util.JwtUtil;
import com.trendchat.userservice.dto.UserRequest;
import com.trendchat.userservice.dto.UserResponse;
import com.trendchat.userservice.service.AuthService;
import com.trendchat.userservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final TokenService tokenService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse.Get> signup(@RequestBody UserRequest.Signup userRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createUser(userRequest));
    }


}
