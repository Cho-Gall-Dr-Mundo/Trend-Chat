package com.trendchat.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoPayRefundRequest {
    private Long paymentId;
    private int cancelAmount;
    private int cancelTaxFreeAmount;
}
