package com.trendchat.paymentservice.entity;

import com.trendchat.paymentservice.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@NoArgsConstructor
public class Payment{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, unique = true)
    private String tid;

    @Column(nullable = false)
    private String partnerOrderId;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private boolean roleUpgraded = false;

    public Payment(String userId, String tid, String paymentMethod, int amount, PaymentStatus status, String partnerOrderId) {
        this.userId = userId;
        this.tid = tid;
        this.partnerOrderId = partnerOrderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = status;
    }

    public void changeStatus(PaymentStatus status) {
        this.status = status;
    }

    public void markRoleUpgraded() {
        this.roleUpgraded = true;
    }
}
