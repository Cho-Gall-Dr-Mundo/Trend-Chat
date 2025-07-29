package com.trendchat.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class KakaoPayApproveResponse {

    private String aid;
    private String tid;
    private String cid;

    @JsonProperty("sid")
    private String sid;

    @JsonProperty("partner_order_id")
    private String partnerOrderId;

    @JsonProperty("partner_user_id")
    private String partnerUserId;

    @JsonProperty("item_name")
    private String itemName;

    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("amount")
    private Amount amount;

    @JsonProperty("approved_at")
    private String approvedAt;

    @Getter
    public static class Amount {
        private int total;

        @JsonProperty("tax_free")
        private int tax_free;

        private int vat;
        private int point;
        private int discount;
    }
}
