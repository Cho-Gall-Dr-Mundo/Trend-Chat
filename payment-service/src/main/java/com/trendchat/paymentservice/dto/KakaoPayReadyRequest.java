package com.trendchat.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoPayReadyRequest {
    private String itemName;
    private int quantity;
    private int totalAmount;
}
