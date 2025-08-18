package com.trendchat.userservice.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * {@code MetricsConfig}는 애플리케이션의 메트릭스(Metrics) 수집을 위한 Spring 설정을 담당하는 구성 클래스입니다.
 * <p>
 * 이 클래스는 {@link EnableAspectJAutoProxy} 어노테이션을 통해 AspectJ 자동 프록시를 활성화하여, Micrometer의 {@code @Timed}
 * 어노테이션이 붙은 메서드 호출에 대한 실행 시간을 자동으로 측정할 수 있도록 합니다.
 * </p>
 *
 * @see io.micrometer.core.aop.TimedAspect
 * @see io.micrometer.core.instrument.MeterRegistry
 * @see org.springframework.context.annotation.EnableAspectJAutoProxy
 */
@Configuration
@EnableAspectJAutoProxy
public class MetricsConfig {

    /**
     * 메서드 실행 시간을 측정하기 위한 {@link TimedAspect} 빈을 제공합니다.
     * <p>
     * 이 애스펙트는 {@code @Timed} 어노테이션이 붙은 메서드 호출에 대한 타이밍 메트릭을 자동으로 기록하며, 기록된 메트릭은 제공된
     * {@link MeterRegistry}를 통해 관리됩니다.
     * </p>
     *
     * @param meterRegistry 메트릭을 등록하고 관리하는 데 사용되는 {@link MeterRegistry} 인스턴스
     * @return 초기화된 {@link TimedAspect} 인스턴스
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry meterRegistry) {
        return new TimedAspect(meterRegistry);
    }
}