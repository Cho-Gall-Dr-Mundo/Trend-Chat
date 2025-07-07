package com.trendchat.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoPayReadyRequest {

    private String cid;

    @JsonProperty("partner_order_id")
    private String partnerOrderId;

    @JsonProperty("partner_user_id")
    private String partnerUserId;

    @JsonProperty("item_name")
    private String itemName;

    private int quantity;

    @JsonProperty("total_amount")
    private int totalAmount;

    @JsonProperty("tax_free_amount")
    private int taxFreeAmount;

    @JsonProperty("vat_amount")
    private int vatAmount;

    @JsonProperty("approval_url")
    private String approvalUrl;

    @JsonProperty("cancel_url")
    private String cancelUrl;

    @JsonProperty("fail_url")
    private String failUrl;
}
