package com.trendchat.paymentservice.service;

import com.trendchat.paymentservice.client.kakaoPayClient;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoPayServiceImpl implements KakaoPayService {

    private final kakaoPayClient kakaoPayClient;
    private final SubscriptionRepository subscriptionRepository;
//    private final UserRoleUpdateService userRoleUpdateService;

    @Value("${kakaopay.cid}")
    private String cid;

    @Value("${kakaopay.client-id}")
    private String clientId;

    @Value("${kakaopay.client-secret}")
    private String clientSecret;

    @Value("${kakaopay.secret-key}")
    private String secretKey;

    @Value("${kakaopay.admin-key}")
    private String adminKey;

    private static final String APPROVAL_URL = "http://localhost:3000/subscribe/success";
    private static final String CANCEL_URL = "http://localhost:3000/subscribe/cancel";
    private static final String FAIL_URL = "http://localhost:3000/subscribe/fail";

    private String authHeader() {
        return "KakaoAK" + adminKey;
    }

    @Override
    @Transactional
    public KakaoPayReadyResponse subscribe(String userId) {
        if (subscriptionRepository.existsByUserId(userId)) {
            throw new IllegalStateException("이미 구독 중인 사용자입니다.");
        }

        KakaoPayReadyRequest request = KakaoPayReadyRequest.builder()
                .cid(cid)
                .partnerOrderId("trendchat-subscription-" + userId)
                .partnerUserId(String.valueOf(userId))
                .itemName("TrendChat 프리미엄 구독")
                .quantity(1)
                .totalAmount(1000)
                .taxFreeAmount(0)
                .vatAmount(0)
                .approvalUrl(APPROVAL_URL + "?userId=" + userId)
                .cancelUrl(CANCEL_URL)
                .failUrl(FAIL_URL)
                .build();

        KakaoPayReadyResponse response = kakaoPayClient.ready(authHeader(), request);

        Subscription subscription = Subscription.builder()
                .userId(userId)
                .sid(response.getTid()) // 임시 저장
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
                .cid(cid)
                .tid(tid)
                .partnerOrderId("trendchat-subscription-" + userId)
                .partnerUserId(userId)
                .pgToken(pgToken)
                .build();

        KakaoPayApproveResponse response = kakaoPayClient.approve(authHeader(), request);

        if (response.getSid() != null ) {
            subscription.updateSid(response.getSid());
        }

        subscription.activate(LocalDateTime.now());
        subscriptionRepository.save(subscription);

//        userRoleUpdateService.upgradeToPremium(userId);

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
                .cid(cid)
                .sid(subscription.getSid())
                .build();

        KakaoPayInactiveResponse response = kakaoPayClient.deactivate(authHeader(), request);

        subscription.deactivate(LocalDateTime.now());
        subscriptionRepository.save(subscription);

//        userRoleUpdateService.downgradeToFree(userId);

        return response;
    }

    @Override
    public KakaoPaySubscriptionStatusResponse getStatus(String userId) {
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("구독 정보가 없습니다."));

        KakaoPaySubscriptionStatusRequest request = KakaoPaySubscriptionStatusRequest.builder()
                .cid(cid)
                .sid(subscription.getSid())
                .build();

        return kakaoPayClient.getStatus(authHeader(), request);
    }
}
