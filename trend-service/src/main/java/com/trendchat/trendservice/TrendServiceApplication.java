package com.trendchat.trendservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Trend Chat 프로젝트의 트렌드 마이크로서비스 애플리케이션 진입점입니다.
 * <p>
 * 이 애플리케이션은 다음 기능을 활성화합니다:
 * </p>
 * <ul>
 *     <li>{@link EnableJpaAuditing} – JPA Auditing을 통한 생성일 자동 처리</li>
 *     <li>{@link EnableFeignClients} – FeignClient를 통한 외부 서비스 연동 기능 활성화</li>
 *     <li>{@link EnableDiscoveryClient} – Eureka와 같은 서비스 디스커버리 등록 기능</li>
 *     <li>{@link SpringBootApplication} – Spring Boot 애플리케이션 기본 설정</li>
 * </ul>
 *
 * @see org.springframework.boot.SpringApplication
 */
@EnableJpaAuditing
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class TrendServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrendServiceApplication.class, args);
    }

}
