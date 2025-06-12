package com.trendchat.paymentservice.config;

import com.trendchat.trendchatcommon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RefreshScope
@Configuration
public class JwtUtilConfig {

    @Value("${jwt.token.access.secret.key}")
    private String accessSecretKey;

    @Value("${jwt.token.refresh.secret.key}")
    private String refreshSecretKey;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(accessSecretKey, refreshSecretKey);
    }
}
