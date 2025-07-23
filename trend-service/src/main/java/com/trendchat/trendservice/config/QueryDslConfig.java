package com.trendchat.trendservice.config;

import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@code QueryDslConfig}는 QueryDSL을 사용하기 위한 설정을 정의한 클래스입니다.
 *
 * <p>{@link JPAQueryFactory}를 스프링 빈으로 등록하여, 서비스 또는 리포지토리 계층에서
 * 타입 안전한 JPQL 쿼리를 작성할 수 있도록 지원합니다.</p>
 *
 * <p>{@code EntityManager}는 {@code @PersistenceContext}를 통해 주입되며,
 * {@link JPQLTemplates#DEFAULT}는 표준 JPQL 문법 기반의 쿼리 생성을 지원합니다.</p>
 *
 * @author TrendChat
 */
@Configuration
class QueryDslConfig {
    
    @PersistenceContext
    private EntityManager em;

    /**
     * {@code JPAQueryFactory}를 빈으로 등록하여 QueryDSL 쿼리 생성을 지원합니다.
     *
     * @return {@code JPAQueryFactory} 인스턴스
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(JPQLTemplates.DEFAULT, em);
    }
}
