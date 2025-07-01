package com.trendchat.paymentservice.service;

import com.trendchat.paymentservice.dto.*;
import com.trendchat.paymentservice.entity.Payment;
import com.trendchat.paymentservice.enums.PaymentStatus;
import com.trendchat.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoPayServiceImpl implements KakaoPayService {

    @Value("${kakaopay.secret-key}")
    private String secretKey;

    @Value("${kakaopay.cid}")
    private String cid;

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public KakaoPayReadyResponse kakaoPayReady(KakaoPayReadyRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "SECRET_KEY " + secretKey);

        String orderId = UUID.randomUUID().toString();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("cid", cid);
        body.add("partner_order_id", orderId);
        body.add("partner_user_id", "user_" + request.getUserId());
        body.add("item_name", request.getItemName());
        body.add("quantity", String.valueOf(request.getQuantity()));
        body.add("total_amount", String.valueOf(request.getTotalAmount()));
        body.add("tax_free_amount", "0");
        body.add("approval_url", "http://localhost:3000/subscribe/checkout");
        body.add("cancel_url", "http://localhost:3000/subscribe/cancel");
        body.add("fail_url", "http://localhost:3000/subscribe/fail");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<KakaoPayReadyResponse> response = restTemplate.postForEntity(
                    "https://open-api.kakaopay.com/online/v1/payment/ready",
                    entity,
                    KakaoPayReadyResponse.class
            );

            KakaoPayReadyResponse responseBody = response.getBody();

            Payment payment = new Payment(
                    request.getUserId(),
                    responseBody.getTid(),
                    "KAKAOPAY",
                    request.getTotalAmount(),
                    PaymentStatus.READY,
                    orderId
            );
            paymentRepository.save(payment);

            return responseBody;
        } catch (Exception e) {
            log.error("[카카오페이 Ready 에러 응답]", e);
            throw new RuntimeException("카카오페이 결제 준비 실패");
        }
    }

    @Override
    public void kakaoPayApprove(KakaoPayApproveRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "SECRET_KEY " + secretKey);

        Payment payment = paymentRepository.findByTid(request.getTid())
                .orElseThrow(() -> new RuntimeException("결제 정보가 존재하지 않습니다."));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("cid", cid);
        body.add("tid", request.getTid());
        body.add("partner_order_id", payment.getPartnerOrderId());
        body.add("partner_user_id", "user_" + payment.getUserId());
        body.add("pg_token", request.getPgToken());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(
                    "https://open-api.kakaopay.com/online/v1/payment/approve",
                    entity,
                    Object.class
            );

            payment.changeStatus(PaymentStatus.SUCCESS);
            try {
                changeUserRoleToPremium(payment.getUserId());
                payment.markRoleUpgraded();
            } catch (Exception e) {
                log.warn("롤 변경 실패 - userId: {}, tid: {}", payment.getUserId(), payment.getTid(), e);
            }

            paymentRepository.save(payment);
        } catch (Exception e) {
            payment.changeStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("결제 승인 실패", e);
        }
    }

    @Override
    public void handleKakaoPayCancel(KakaoPayCancelRequest request) {
        Payment payment = new Payment(
                request.getUserId(),
                request.getTid(),
                "KAKAOPAY",
                0,
                PaymentStatus.CANCELLED,
                "CANCELLED_ORDER"
        );
        paymentRepository.save(payment);
    }

    @Override
    public void handleKakaoPayFail(KakaoPayFailRequest request) {
        Payment payment = new Payment(
                request.getUserId(),
                request.getTid(),
                "KAKAOPAY",
                0,
                PaymentStatus.FAILED,
                "FAILED_ORDER"
        );
        paymentRepository.save(payment);
    }

    @Override
    public void refundPayment(KakaoPayRefundRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 결제 ID입니다."));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("환불은 성공한 결제에 대해서만 가능합니다.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "SECRET_KEY " + secretKey);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("cid", cid);
        body.add("tid", payment.getTid());
        body.add("cancel_amount", String.valueOf(request.getCancelAmount()));
        body.add("cancel_tax_free_amount", String.valueOf(request.getCancelTaxFreeAmount()));

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(
                    "https://open-api.kakaopay.com/online/v1/payment/cancel",
                    entity,
                    String.class
            );

            payment.changeStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        } catch (Exception e) {
            throw new RuntimeException("환불 처리 중 오류 발생", e);
        }
    }

    public void changeUserRoleToPremium(String userId) {
        try {
            String url = "http://user-service/api/internal/users/" + userId + "/upgrade-role";
            restTemplate.postForEntity(url, null, Void.class);
        } catch (Exception e) {
            throw new RuntimeException("유저 롤 변경 실패", e);
        }
    }
}
