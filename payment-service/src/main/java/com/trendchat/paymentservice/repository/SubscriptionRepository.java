package com.trendchat.paymentservice.repository;

import com.trendchat.paymentservice.entity.Subscription;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUserId(String userId);

    boolean existsByUserId(String userId);
}
