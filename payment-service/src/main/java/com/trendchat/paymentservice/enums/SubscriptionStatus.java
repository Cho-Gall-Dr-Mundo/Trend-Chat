package com.trendchat.paymentservice.enums;

public enum SubscriptionStatus {
    READY,      // 결제 준비됨 (SID 발급됨)
    ACTIVE,     // 정기결제 승인 완료
    INACTIVE    // 정기결제 해지됨
}
