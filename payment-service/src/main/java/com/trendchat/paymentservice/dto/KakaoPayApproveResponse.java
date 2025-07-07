package com.trendchat.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class KakaoPayApproveResponse {

    private String aid;
    private String tid;

    @JsonProperty("partner_order_id")
    private String partnerOrderId;

    @JsonProperty("partner_user_id")
    private String partnerUserId;

    private String itemName;

    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("approved_at")
    private String approvedAt;
}
