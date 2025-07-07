package com.trendchat.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class KakaoPaySubscriptionStatusResponse {

    private String sid;

    @JsonProperty("status")
    private String status; // ACTIVE / INACTIVE 등

    @JsonProperty("last_approved_at")
    private String lastApprovedAt;

    @JsonProperty("next_approve_at")
    private String nextApproveAt;
}
