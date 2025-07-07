package com.trendchat.paymentservice.controller;

import com.trendchat.paymentservice.dto.KakaoPayApproveResponse;
import com.trendchat.paymentservice.dto.KakaoPayInactiveResponse;
import com.trendchat.paymentservice.dto.KakaoPayReadyResponse;
import com.trendchat.paymentservice.dto.KakaoPaySubscriptionStatusResponse;
import com.trendchat.paymentservice.service.KakaoPayService;
import com.trendchat.trendchatcommon.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;

    /**
     * 정기결제 준비 요청 (카카오페이 결제 페이지로 redirect URL 반환)
     */
    @PostMapping("/subscribe")
    public ResponseEntity<KakaoPayReadyResponse> subscribe(@AuthenticationPrincipal AuthUser authUser) {
        String userId = authUser.getUserId();
        KakaoPayReadyResponse response = kakaoPayService.subscribe(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 정기결제 승인 처리 (카카오 redirect 후 pg_token으로 인증)
     * 프론트에서 호출 (ex: /subscribe/success?pg_token=abc&userId=1&tid=xxx)
     */
    @GetMapping("/kakaopay/approve")
    public ResponseEntity<KakaoPayApproveResponse> approve(
            @RequestParam("userId") String userId,
            @RequestParam("pg_token") String pgToken,
            @RequestParam("tid") String tid
    ) {
        KakaoPayApproveResponse response = kakaoPayService.approve(userId, pgToken, tid);
        return ResponseEntity.ok(response);
    }

    /**
     * 정기결제 해지 (카카오페이 측에 구독 취소 요청)
     */
    @PostMapping("/unsubscribe")
    public ResponseEntity<KakaoPayInactiveResponse> unsubscribe(@AuthenticationPrincipal AuthUser authUser) {
        Long userId = Long.parseLong(authUser.getUserId());
        KakaoPayInactiveResponse response = kakaoPayService.cancel(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 정기결제 상태 조회 (카카오페이에서 현재 구독 상태 확인)
     */
    @GetMapping("/status")
    public ResponseEntity<KakaoPaySubscriptionStatusResponse> getStatus(@AuthenticationPrincipal AuthUser authUser) {
        String userId = authUser.getUserId();
        KakaoPaySubscriptionStatusResponse response = kakaoPayService.getStatus(userId);
        return ResponseEntity.ok(response);
    }
}
