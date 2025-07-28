package com.trendchat.paymentservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoPaySubscriptionStatusRequest {
    private String cid;
    private String sid;
}
