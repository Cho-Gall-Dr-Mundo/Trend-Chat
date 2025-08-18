package com.trendchat.paymentservice.config;

import com.trendchat.trendchatcommon.filter.AuthorizationFilterMvc;
import com.trendchat.trendchatcommon.util.BlacklistChecker;
import com.trendchat.trendchatcommon.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;

/**
 * Spring Security MVC 설정 클래스입니다.
 * <p>
 * PaymentService에서 JWT 기반 인증을 처리하고, 블랙리스트 토큰 검증 및 보안 필터 체인을 구성합니다.
 * </p>
 *
 * <h2>주요 역할</h2>
 * <ul>
 *     <li>JWT 및 블랙리스트 체크를 수행하는 {@link AuthorizationFilterMvc} Bean 등록</li>
 *     <li>세션을 생성하지 않는 Stateless 보안 정책 적용</li>
 *     <li>CSRF, 기본 인증, 폼 로그인을 비활성화하여 REST API 환경에 적합화</li>
 *     <li>경로별 인가 정책 설정:
 *         <ul>
 *             <li>{@code /actuator/**} → 헬스체크/모니터링을 위한 공개 접근 허용</li>
 *             <li>그 외 모든 요청 → 인증 필요</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <h2>필터 체인</h2>
 * <p>
 * {@link AuthorizationFilterMvc}는 {@link AuthenticationFilter} 앞에 위치시켜,
 * Spring Security의 표준 인증 처리 전에 JWT 유효성 및 블랙리스트 검증을 수행합니다.
 * </p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    /**
     * JWT 인증 및 블랙리스트 검증을 수행하는 MVC 전용 필터를 Bean으로 등록합니다.
     *
     * @param blacklistChecker 블랙리스트 토큰 검사 유틸리티
     * @return {@link AuthorizationFilterMvc} 인스턴스
     */
    @Bean
    public AuthorizationFilterMvc authorizationFilter(
            BlacklistChecker blacklistChecker
    ) {
        return new AuthorizationFilterMvc(jwtUtil, blacklistChecker);
    }

    /**
     * Spring Security MVC 필터 체인을 구성합니다.
     * <p>
     * - CSRF 비활성화<br> - Stateless 세션 정책 적용<br> - Actuator 경로 공개, 그 외 요청은 인증 필요<br> - 커스텀 JWT 필터를
     * {@link AuthenticationFilter} 앞에 삽입<br>
     * </p>
     *
     * @param httpSecurity        MVC 보안 설정 빌더
     * @param authorizationFilter JWT 인증 및 블랙리스트 검증 필터
     * @return 구성된 {@link SecurityFilterChain}
     * @throws Exception 보안 설정 과정에서 오류 발생 시
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity httpSecurity,
            AuthorizationFilterMvc authorizationFilter
    ) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers("/actuator/**").permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(authorizationFilter, AuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }
}
