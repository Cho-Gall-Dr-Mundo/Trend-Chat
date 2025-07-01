package com.trendchat.paymentservice.controller;

import com.trendchat.paymentservice.entity.Payment;
import com.trendchat.paymentservice.enums.PaymentStatus;
import com.trendchat.paymentservice.repository.PaymentRepository;
import com.trendchat.paymentservice.service.KakaoPayServiceImpl;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentAdminController {

    private final PaymentRepository paymentRepository;
    private final KakaoPayServiceImpl kakaoPayServiceImpl;

    @PostMapping("/v1/admin/payments/retry-role-upgrade")
    public ResponseEntity<?> retryRoleUpgrade() {
        List<Payment> payments = paymentRepository.findAllByStatusAndRoleUpgraded(PaymentStatus.SUCCESS, false);

        int successCount = 0;
        int failCount = 0;

        for (Payment payment : payments) {
            try {
                kakaoPayServiceImpl.changeUserRoleToPremium(payment.getUserId());
                payment.markRoleUpgraded();
                paymentRepository.save(payment);
                successCount++;
                log.info("롤 재전환 성공 - userId: {}", payment.getUserId());
            } catch (Exception e) {
                failCount++;
                log.warn("롤 재전환 실패 - userId: {}", payment.getUserId(), e);
            }
        }

        Map<String, Integer> result = Map.of(
                "success", successCount,
                "failed", failCount
        );
        return ResponseEntity.ok(result);
    }
}
