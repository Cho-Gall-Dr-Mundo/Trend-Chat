package com.trendchat.paymentservice.service;

import com.trendchat.paymentservice.dto.KakaoPayApproveResponse;
import com.trendchat.paymentservice.dto.KakaoPayInactiveResponse;
import com.trendchat.paymentservice.dto.KakaoPayReadyResponse;
import com.trendchat.paymentservice.dto.KakaoPaySubscriptionStatusResponse;

public interface KakaoPayService {

    KakaoPayReadyResponse subscribe(Long userId);

    KakaoPayApproveResponse approve(Long userId, String pgToken, String tid);

    KakaoPayInactiveResponse cancel(Long userId);

    KakaoPaySubscriptionStatusResponse getStatus(Long userId);
}
