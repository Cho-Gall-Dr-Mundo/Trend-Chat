package com.trendchat.paymentservice.repository;

import com.trendchat.paymentservice.entity.Payment;
import com.trendchat.paymentservice.enums.PaymentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTid(String tid);

    List<Payment> findAllByStatusAndRoleUpgraded(PaymentStatus status, boolean roleUpgraded);
}
