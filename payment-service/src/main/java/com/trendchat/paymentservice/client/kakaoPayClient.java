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
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient (
        name = "kakaoPayClient",
        url = "https://open-api.kakaopay.com",
        configuration = KakaoPayFeignConfig.class
)
public interface kakaoPayClient {

    @PostMapping("/online/v1/payment/ready")
    KakaoPayReadyResponse ready(
            @RequestHeader("Authorization") String authorization,
            @RequestBody KakaoPayReadyRequest request
    );

    @PostMapping("/online/v1/payment/approve")
    KakaoPayApproveResponse approve(
            @RequestHeader("Authorization") String authorization,
            @RequestBody KakaoPayApproveRequest request
    );

    @PostMapping("/online/v1/payment/manage/inactive")
    KakaoPayInactiveResponse deactivate(
            @RequestHeader("Authorization") String authorization,
            @RequestBody KakaoPayInactiveRequest request
    );

    @PostMapping("/online/v1/payment/subscription/status")
    KakaoPaySubscriptionStatusResponse getStatus(
            @RequestHeader("Authorization") String authorization,
            @RequestBody KakaoPaySubscriptionStatusRequest request
    );
}
