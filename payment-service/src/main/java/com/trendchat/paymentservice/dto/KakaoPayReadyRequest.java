package com.trendchat.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoPayReadyRequest {
    @JsonProperty("cid")
    private String cid;

    @JsonProperty("partner_order_id")
    private String partner_order_id;

    @JsonProperty("partner_user_id")
    private String partner_user_id;

    @JsonProperty("item_name")
    private String item_name;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("total_amount")
    private Integer total_amount;

    @JsonProperty("tax_free_amount")
    private Integer tax_free_amount;

    @JsonProperty("vat_amount")
    private Integer vat_amount;

    @JsonProperty("approval_url")
    private String approval_url;

    @JsonProperty("cancel_url")
    private String cancel_url;

    @JsonProperty("fail_url")
    private String fail_url;
}
