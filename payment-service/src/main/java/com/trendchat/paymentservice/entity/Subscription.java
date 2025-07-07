package com.trendchat.paymentservice.entity;

import com.trendchat.paymentservice.enums.SubscriptionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    @Column(nullable = false)
    private String sid;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private LocalDateTime approvedAt;

    private LocalDateTime canceledAt;

    private LocalDateTime nextPaymentAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void activate(LocalDateTime approvedAt) {
        this.status = SubscriptionStatus.ACTIVE;
        this.approvedAt = approvedAt;
    }

    public void deactivate(LocalDateTime canceledAt) {
        this.status = SubscriptionStatus.INACTIVE;
        this.canceledAt = canceledAt;
    }
}
