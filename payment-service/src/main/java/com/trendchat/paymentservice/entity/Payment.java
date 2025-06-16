package com.trendchat.paymentservice.entity;

import com.trendchat.paymentservice.enums.PaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Payment{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private String tid;

    private String paymentMethod;

    private int amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    public Payment(String userId, String tid, String paymentMethod, int amount, PaymentStatus status) {
        this.userId = userId;
        this.tid = tid;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = status;
    }

    public void changeStatus(PaymentStatus status) {
        this.status = status;
    }

}
