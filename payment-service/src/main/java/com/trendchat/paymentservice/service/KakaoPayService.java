package com.trendchat.paymentservice.service;

import com.trendchat.paymentservice.dto.KakaoPayApproveRequest;
import com.trendchat.paymentservice.dto.KakaoPayCancelRequest;
import com.trendchat.paymentservice.dto.KakaoPayFailRequest;
import com.trendchat.paymentservice.dto.KakaoPayReadyRequest;
import com.trendchat.paymentservice.dto.KakaoPayReadyResponse;
import com.trendchat.paymentservice.dto.KakaoPayRefundRequest;

public interface KakaoPayService {

    KakaoPayReadyResponse kakaoPayReady(KakaoPayReadyRequest request);
    void kakaoPayApprove(KakaoPayApproveRequest request);
    void handleKakaoPayCancel(KakaoPayCancelRequest request);
    void handleKakaoPayFail(KakaoPayFailRequest request);
    void refundPayment(KakaoPayRefundRequest request);
}
