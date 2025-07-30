package com.trendchat.userservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        // 무한 루프 방지용 사파 코드
        return "Hello, this is home!";
    }
}