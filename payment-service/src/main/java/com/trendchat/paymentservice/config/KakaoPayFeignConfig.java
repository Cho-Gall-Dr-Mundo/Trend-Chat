package com.trendchat.paymentservice.config;

import feign.Logger;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KakaoPayFeignConfig {

    private final KakaoPayProperties kakaoPayProperties;

    @Bean
    public RequestInterceptor kakaoPayRequestInterceptor() {
        return requestTemplate -> {
            String secretKeyHeader = "SECRET_KEY " + kakaoPayProperties.getSecretKey();
            log.info("카카오페이 Authorization 헤더: {}", secretKeyHeader);
            requestTemplate.header("Authorization", secretKeyHeader);
        };
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
