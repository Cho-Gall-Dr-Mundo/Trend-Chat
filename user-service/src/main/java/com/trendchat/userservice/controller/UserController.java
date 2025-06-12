package com.trendchat.userservice.controller;

import com.trendchat.trendchatcommon.auth.AuthUser;
import com.trendchat.userservice.dto.UserResponse;
import com.trendchat.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse.Get> signup(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getUser(authUser.getUserId()));
    }
}
