package com.trendchat.paymentservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendchat.paymentservice.client.KakaoPayClient;
import com.trendchat.paymentservice.config.KakaoPayProperties;
import com.trendchat.paymentservice.dto.KakaoPayApproveRequest;
import com.trendchat.paymentservice.dto.KakaoPayApproveResponse;
import com.trendchat.paymentservice.dto.KakaoPayInactiveRequest;
import com.trendchat.paymentservice.dto.KakaoPayInactiveResponse;
import com.trendchat.paymentservice.dto.KakaoPayReadyRequest;
import com.trendchat.paymentservice.dto.KakaoPayReadyResponse;
import com.trendchat.paymentservice.dto.KakaoPaySubscriptionStatusRequest;
import com.trendchat.paymentservice.dto.KakaoPaySubscriptionStatusResponse;
import com.trendchat.paymentservice.entity.Subscription;
import com.trendchat.paymentservice.enums.SubscriptionStatus;
import com.trendchat.paymentservice.repository.SubscriptionRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoPayServiceImpl implements KakaoPayService {

    private final KakaoPayClient kakaoPayClient;
    private final KakaoPayProperties kakaoPayProperties;
    private final SubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public KakaoPayReadyResponse subscribe(String userId) {
        if (subscriptionRepository.existsByUserId(userId)) {
            throw new IllegalStateException("이미 구독 중인 사용자입니다.");
        }

        KakaoPayReadyRequest request = KakaoPayReadyRequest.builder()
                .cid(kakaoPayProperties.getCid())
                .partner_order_id("trendchat-subscription-" + userId)
                .partner_user_id(userId)
                .item_name("TrendChat 프리미엄 구독")
                .quantity(1)
                .total_amount(3900)
                .tax_free_amount(0)
                .vat_amount(354)
                .approval_url(kakaoPayProperties.getRedirect().getApproveUrl() + "?userId=" + userId)
                .cancel_url(kakaoPayProperties.getRedirect().getCancelUrl())
                .fail_url(kakaoPayProperties.getRedirect().getFailUrl())
                .build();

        log.info("카카오페이 요청: CID = {}", request.getCid());
        log.info("카카오페이 승인 URL = {}", request.getApproval_url());
        try {
            log.info("카카오페이 전체 요청 바디 = {}", objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            log.error("요청 바디 직렬화 실패", e);
        }

        KakaoPayReadyResponse response = kakaoPayClient.ready(request);

        log.info("카카오페이 응답 수신: {}", response);

        Subscription subscription = Subscription.builder()
                .userId(userId)
                .sid(response.getTid()) // TID 임시 저장, 실제 SID는 승인 시 할당됨
                .status(SubscriptionStatus.READY)
                .build();

        subscriptionRepository.save(subscription);
        return response;
    }

    @Override
    @Transactional
    public KakaoPayApproveResponse approve(String userId, String pgToken, String tid) {
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("구독 정보가 없습니다."));

        KakaoPayApproveRequest request = KakaoPayApproveRequest.builder()
                .cid(kakaoPayProperties.getCid())
                .tid(tid)
                .partnerOrderId("trendchat-subscription-" + userId)
                .partnerUserId(userId)
                .pgToken(pgToken)
                .build();

        KakaoPayApproveResponse response = kakaoPayClient.approve(request);

        if (response.getSid() != null ) {
            subscription.updateSid(response.getSid());
        }

        subscription.activate(LocalDateTime.now());
        subscriptionRepository.save(subscription);

        return response;
    }

    @Override
    @Transactional
    public KakaoPayInactiveResponse cancel(String userId) {
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("구독 정보가 없습니다."));

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("활성 상태가 아닙니다.");
        }

        KakaoPayInactiveRequest request = KakaoPayInactiveRequest.builder()
                .cid(kakaoPayProperties.getCid())
                .sid(subscription.getSid())
                .build();

        KakaoPayInactiveResponse response = kakaoPayClient.deactivate(request);

        subscription.deactivate(LocalDateTime.now());
        subscriptionRepository.save(subscription);

        return response;
    }

    @Override
    public KakaoPaySubscriptionStatusResponse getStatus(String userId) {
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("구독 정보가 없습니다."));

        KakaoPaySubscriptionStatusRequest request = KakaoPaySubscriptionStatusRequest.builder()
                .cid(kakaoPayProperties.getCid())
                .sid(subscription.getSid())
                .build();

        return kakaoPayClient.getStatus(request);
    }
}
