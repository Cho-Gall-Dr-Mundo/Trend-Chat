package com.trendchat.paymentservice.service;

import com.trendchat.paymentservice.dto.KakaoPayApproveRequest;
import com.trendchat.paymentservice.dto.KakaoPayCancelRequest;
import com.trendchat.paymentservice.dto.KakaoPayFailRequest;
import com.trendchat.paymentservice.dto.KakaoPayReadyRequest;
import com.trendchat.paymentservice.dto.KakaoPayReadyResponse;
import com.trendchat.paymentservice.dto.KakaoPayRefundRequest;
import com.trendchat.paymentservice.entity.Payment;
import com.trendchat.paymentservice.enums.PaymentStatus;
import com.trendchat.paymentservice.repository.PaymentRepository;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoPayService {

    @Value("${kakao.pay.admin-key}")
    private String adminKey;


    private final PaymentRepository paymentRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public KakaoPayReadyResponse kakaoPayReady(KakaoPayReadyRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + adminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", "TC0ONETIME");
        params.add("partner_order_id", UUID.randomUUID().toString());
        params.add("partner_user_id", "user_" + UUID.randomUUID());
        params.add("item_name", request.getItemName());
        params.add("quantity", String.valueOf(request.getQuantity()));
        params.add("total_amount", String.valueOf(request.getTotalAmount()));
        params.add("tax_free_amount", "0");
        params.add("approval_url", "http://localhost:3000/subscribe/checkout");
        params.add("cancel_url", "http://localhost:3000/subscribe/cancel");
        params.add("fail_url", "http://localhost:3000/subscribe/fail");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://kapi.kakao.com/v1/payment/ready",
                entity,
                Map.class
        );

        String tid = (String) response.getBody().get("tid");
        String nextUrl = (String) response.getBody().get("next_redirect_pc_url");

        log.info("카카오페이 결제 준비 완료 - tid: {}, nextUrl: {}", tid, nextUrl);

        return new KakaoPayReadyResponse(tid, nextUrl);
    }

    public void kakaoPayApprove(KakaoPayApproveRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + adminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", "TC0ONETIME");
        params.add("tid", request.getTid());
        params.add("partner_order_id", "order_" + request.getUserId());
        params.add("partner_user_id", "user_" + request.getUserId());
        params.add("pg_token", request.getPgToken());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            restTemplate.postForEntity("https://kapi.kakao.com/v1/payment/approve", entity, String.class);

            // 성공한 결제 내역 저장
            Payment payment = new Payment(
                    request.getUserId(),
                    request.getTid(),
                    "KAKAOPAY",
                    3900,
                    PaymentStatus.SUCCESS
            );
            paymentRepository.save(payment);

            log.info("✅ 결제 승인 성공 - userId: {}, tid: {}", request.getUserId(), request.getTid());
        } catch (Exception e) {
            // 실패한 결제 내역 저장
            Payment payment = new Payment(
                    request.getUserId(),
                    request.getTid(),
                    "KAKAOPAY",
                    3900,
                    PaymentStatus.FAILED
            );
            paymentRepository.save(payment);

            log.error("❌ 결제 승인 실패 - userId: {}, tid: {}", request.getUserId(), request.getTid(), e);
            throw new RuntimeException("결제 승인 중 오류 발생");
        }
    }

    public void handleKakaoPayCancel(KakaoPayCancelRequest request) {
        log.info("카카오페이 결제 취소 처리 - userId: {}, tid: {}", request.getUserId(), request.getTid());

        Payment payment = new Payment(request.getUserId(), request.getTid(), "KAKAOPAY", 0, PaymentStatus.CANCELLED);
        paymentRepository.save(payment);

        log.info("✅ 결제 취소 저장 완료");
    }

    public void handleKakaoPayFail(KakaoPayFailRequest request) {
        log.info("카카오페이 결제 실패 처리 - userId: {}, tid: {}", request.getUserId(), request.getTid());

        Payment payment = new Payment(request.getUserId(), request.getTid(), "KAKAOPAY", 0, PaymentStatus.FAILED);
        paymentRepository.save(payment);

        log.info("✅ 결제 실패 저장 완료");
    }

    public void refundPayment(KakaoPayRefundRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제 ID입니다."));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("환불은 성공한 결제에 대해서만 가능합니다.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + adminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", "TC0ONETIME");
        params.add("tid", payment.getTid());
        params.add("cancel_amount", String.valueOf(request.getCancelAmount()));
        params.add("cancel_tax_free_amount", String.valueOf(request.getCancelTaxFreeAmount()));

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            restTemplate.postForEntity(
                    "https://kapi.kakao.com/v1/payment/cancel",
                    entity,
                    String.class
            );

            payment.changeStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            log.info("환불 성공 - paymentId: {}, amount: {}", payment.getId(), request.getCancelAmount());

        } catch (Exception e) {
            log.error("환불 실패 - paymentId: {}", payment.getId(), e);
            throw new RuntimeException("환불 처리 중 오류가 발생했습니다.");
        }
    }
}
