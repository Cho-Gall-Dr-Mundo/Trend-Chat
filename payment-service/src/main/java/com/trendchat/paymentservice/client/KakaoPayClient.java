package com.trendchat.paymentservice.client;

import com.trendchat.paymentservice.config.KakaoPayFeignConfig;
import com.trendchat.paymentservice.dto.KakaoPayApproveRequest;
import com.trendchat.paymentservice.dto.KakaoPayApproveResponse;
import com.trendchat.paymentservice.dto.KakaoPayInactiveRequest;
import com.trendchat.paymentservice.dto.KakaoPayInactiveResponse;
import com.trendchat.paymentservice.dto.KakaoPayReadyRequest;
import com.trendchat.paymentservice.dto.KakaoPayReadyResponse;
import com.trendchat.paymentservice.dto.KakaoPaySubscriptionStatusRequest;
import com.trendchat.paymentservice.dto.KakaoPaySubscriptionStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient (
        name = "kakaoPayClient",
        url = "https://open-api.kakaopay.com/online/v1/payment",
        configuration = KakaoPayFeignConfig.class
)
public interface KakaoPayClient {

    @PostMapping("/ready")
    KakaoPayReadyResponse ready(
            @RequestBody KakaoPayReadyRequest request
    );

    @PostMapping("/approve")
    KakaoPayApproveResponse approve(
            @RequestBody KakaoPayApproveRequest request
    );

    @PostMapping("/inactive")
    KakaoPayInactiveResponse deactivate(
            @RequestBody KakaoPayInactiveRequest request
    );

    @PostMapping("/status")
    KakaoPaySubscriptionStatusResponse getStatus(
            @RequestBody KakaoPaySubscriptionStatusRequest request
    );
}
