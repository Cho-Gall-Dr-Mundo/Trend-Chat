package com.trendchat.paymentservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoPayInactiveRequest {
    private String cid;
    private String sid;
}
