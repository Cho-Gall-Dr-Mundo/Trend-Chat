package com.trendchat.paymentservice.controller;

import com.trendchat.paymentservice.dto.KakaoPayApproveRequest;
import com.trendchat.paymentservice.dto.KakaoPayCancelRequest;
import com.trendchat.paymentservice.dto.KakaoPayFailRequest;
import com.trendchat.paymentservice.dto.KakaoPayReadyRequest;
import com.trendchat.paymentservice.dto.KakaoPayReadyResponse;
import com.trendchat.paymentservice.dto.KakaoPayRefundRequest;
import com.trendchat.paymentservice.service.KakaoPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;

    @PostMapping("/v1/payments/kakaopay/ready")
    public ResponseEntity<KakaoPayReadyResponse> ready(@RequestBody KakaoPayReadyRequest request) {
        return ResponseEntity.ok(kakaoPayService.kakaoPayReady(request));
    }

    @PostMapping("/v1/payments/kakaopay/approve")
    public ResponseEntity<String> approve(@RequestBody KakaoPayApproveRequest request) {
        kakaoPayService.kakaoPayApprove(request);
        return ResponseEntity.ok("구독 완료");
    }

    @PostMapping("/v1/payments/kakaopay/cancel")
    public ResponseEntity<Void> cancel(@RequestBody KakaoPayCancelRequest request) {
        kakaoPayService.handleKakaoPayCancel(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/payments/kakaopay/fail")
    public ResponseEntity<Void> fail(@RequestBody KakaoPayFailRequest request) {
        kakaoPayService.handleKakaoPayFail(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/payments/kakaopay/refund")
    public ResponseEntity<Void> refund(@RequestBody KakaoPayRefundRequest request) {
        kakaoPayService.refundPayment(request);
        return ResponseEntity.ok().build();
    }
}
