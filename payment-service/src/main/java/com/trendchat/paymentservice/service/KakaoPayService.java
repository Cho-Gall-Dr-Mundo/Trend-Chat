package com.trendchat.paymentservice.service;

import com.trendchat.paymentservice.dto.KakaoPayApproveResponse;
import com.trendchat.paymentservice.dto.KakaoPayInactiveResponse;
import com.trendchat.paymentservice.dto.KakaoPayReadyResponse;
import com.trendchat.paymentservice.dto.KakaoPaySubscriptionStatusResponse;

public interface KakaoPayService {

    KakaoPayReadyResponse subscribe(String userId);

    KakaoPayApproveResponse approve(String userId, String pgToken, String tid);

    KakaoPayInactiveResponse cancel(String userId);

    KakaoPaySubscriptionStatusResponse getStatus(String userId);
}
