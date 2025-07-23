package com.trendchat.trendservice.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Micrometer의 {@code @Timed} 애노테이션을 활성화하기 위한 AOP 기반 설정 클래스입니다.
 * <p>
 * {@link io.micrometer.core.aop.TimedAspect}를 Bean으로 등록하여, 메서드 수준의 실행 시간 측정을 가능하게 합니다.
 * </p>
 */
@Configuration
@EnableAspectJAutoProxy
public class MetricsConfig {

    /**
     * {@code @Timed} 애노테이션이 부착된 메서드의 실행 시간을 측정할 수 있도록 지원하는 AOP Aspect를 등록합니다.
     * <p>
     * 이 Bean은 Micrometer의 {@link TimedAspect}로, 메서드 실행 시간을 자동으로 기록하여 등록된 {@link MeterRegistry}에
     * 전송합니다.
     * </p>
     *
     * @param meterRegistry Micrometer의 메트릭 저장소 객체
     * @return 메서드 실행 시간을 측정하는 {@link TimedAspect} 인스턴스
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry meterRegistry) {
        return new TimedAspect(meterRegistry);
    }
}