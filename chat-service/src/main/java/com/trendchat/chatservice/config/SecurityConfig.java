package com.trendchat.chatservice.config;

import com.trendchat.trendchatcommon.filter.AuthorizationFilterWebFlux;
import com.trendchat.trendchatcommon.util.BlacklistChecker;
import com.trendchat.trendchatcommon.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

/**
 * Spring Security WebFlux 설정 클래스입니다.
 * <p>
 * ChatService에서 JWT 기반 인증을 처리하고, 블랙리스트 토큰 검증 및 보안 필터 체인을 구성합니다.
 * </p>
 *
 * <h2>주요 역할</h2>
 * <ul>
 *     <li>JWT 유틸리티 및 블랙리스트 체크를 활용한 {@link AuthorizationFilterWebFlux} 등록</li>
 *     <li>CSRF, 기본 인증, 폼 로그인을 비활성화하여 REST API 환경에 최적화</li>
 *     <li>인증 필요 여부를 경로별로 정의:
 *         <ul>
 *             <li>{@code /api/v1/chat/stream}, {@code /api/v1/rooms/**} → 공개 접근 허용</li>
 *             <li>그 외 모든 요청 → 인증 필요</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <h2>Security Context</h2>
 * <p>
 * Reactive 환경(WebFlux)에서는 상태를 세션에 저장하지 않고,
 * 매 요청마다 {@link NoOpServerSecurityContextRepository}를 통해 stateless 방식으로 처리합니다.
 * </p>
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    /**
     * JWT 인증 및 블랙리스트 검증을 수행하는 WebFlux 전용 필터를 Bean으로 등록합니다.
     *
     * @param blacklistChecker 블랙리스트 토큰 검사 유틸리티
     * @return {@link AuthorizationFilterWebFlux} 인스턴스
     */
    @Bean
    public AuthorizationFilterWebFlux authorizationFilter(
            BlacklistChecker blacklistChecker
    ) {
        return new AuthorizationFilterWebFlux(jwtUtil, blacklistChecker);
    }

    /**
     * Spring Security WebFlux 필터 체인을 구성합니다.
     * <p>
     * - CSRF, 기본 인증, 폼 로그인을 비활성화합니다.<br> - SecurityContextRepository를 NoOp로 설정하여 상태 없는(stateless)
     * 인증을 보장합니다.<br> - 경로별 인가 정책을 설정하고, 커스텀 JWT 인증 필터를
     * {@link SecurityWebFiltersOrder#AUTHORIZATION} 위치에 추가합니다.
     * </p>
     *
     * @param httpSecurity        WebFlux 보안 설정 빌더
     * @param authorizationFilter JWT 인증 및 블랙리스트 검증 필터
     * @return 구성된 {@link SecurityWebFilterChain}
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity httpSecurity,
            AuthorizationFilterWebFlux authorizationFilter
    ) {
        return httpSecurity
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/v1/chat/stream").permitAll()
                        .pathMatchers("/api/v1/rooms/**").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(authorizationFilter, SecurityWebFiltersOrder.AUTHORIZATION)
                .build();
    }
}