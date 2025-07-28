package com.trendchat.paymentservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kakaopay")
public class KakaoPayProperties {
    private String cid;
    private String clientId;
    private String clientSecret;
    private String secretKey;
    private String adminKey;
    private Redirect redirect;

    @Getter
    @Setter
    public static class Redirect {
        private String approveUrl;
        private String cancelUrl;
        private String failUrl;
    }

}
