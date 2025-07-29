package com.trendchat.userservice.config;

import com.trendchat.trendchatcommon.filter.AuthorizationFilterMvc;
import com.trendchat.trendchatcommon.util.BlacklistChecker;
import com.trendchat.trendchatcommon.util.JwtUtil;
import com.trendchat.userservice.security.AuthenticationFilter;
import com.trendchat.userservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * {@code SecurityConfig}는 TrendChat 사용자 서비스의 Spring Security 설정을 담당하는 구성 클래스입니다.
 * <p>
 * 이 클래스는 JWT(JSON Web Token) 기반 인증 및 권한 부여를 설정하며, 사용자 정의 인증 필터({@link AuthenticationFilter})와 권한 부여
 * 필터({@link AuthorizationFilterMvc})를 통합합니다. 또한, 비밀번호 암호화, 세션 관리 정책, 그리고 URL별 접근 규칙을 정의합니다.
 * </p>
 *
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 * @see com.trendchat.userservice.security.AuthenticationFilter
 * @see com.trendchat.trendchatcommon.filter.AuthorizationFilterMvc
 * @see com.trendchat.trendchatcommon.util.JwtUtil
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    /**
     * 비밀번호 암호화를 위한 {@link PasswordEncoder} 빈을 제공합니다. BCrypt 해싱 알고리즘을 사용합니다.
     *
     * @return {@link BCryptPasswordEncoder} 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security의 인증 관리자({@link AuthenticationManager}) 빈을 노출합니다. 이는 사용자 정의 인증 필터에서 인증 처리를
     * 위임하는 데 사용됩니다.
     *
     * @param authenticationConfiguration 인증 구성을 위한 {@link AuthenticationConfiguration}
     * @return {@link AuthenticationManager} 인스턴스
     * @throws Exception 인증 관리자 획득 중 발생할 수 있는 예외
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * JWT 기반의 권한 부여(인가)를 처리하는 {@link AuthorizationFilterMvc} 빈을 생성합니다. 이 필터는 요청 헤더의 JWT를 검증하고 사용자
     * 권한을 설정합니다.
     *
     * @param blacklistChecker 토큰 및 사용자 블랙리스트 확인을 위한 {@link BlacklistChecker}
     * @return 설정된 {@link AuthorizationFilterMvc} 인스턴스
     */
    @Bean
    public AuthorizationFilterMvc authorizationFilter(
            BlacklistChecker blacklistChecker
    ) {
        return new AuthorizationFilterMvc(jwtUtil, blacklistChecker);
    }

    /**
     * 사용자 로그인 인증을 처리하는 {@link AuthenticationFilter} 빈을 생성합니다.
     * <p>
     * 이 필터는 {@code /api/v1/auth/login} 경로의 요청을 가로채서 사용자 이메일과 비밀번호로 인증을 시도하고, 성공 시 JWT 토큰을 발급합니다.
     * </p>
     *
     * @param authenticationConfiguration 인증 관리자 설정을 위한 {@link AuthenticationConfiguration}
     * @param tokenService                토큰 관련 비즈니스 로직 처리를 위한 {@link TokenService}
     * @return 설정된 {@link AuthenticationFilter} 인스턴스
     * @throws Exception 인증 관리자 설정 중 발생할 수 있는 예외
     */
    @Bean
    public AuthenticationFilter authenticationFilter(
            AuthenticationConfiguration authenticationConfiguration,
            TokenService tokenService
    ) throws Exception {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(jwtUtil, tokenService);
        authenticationFilter.setAuthenticationManager(
                authenticationManager(authenticationConfiguration)
        );
        authenticationFilter.setFilterProcessesUrl("/api/v1/auth/login");
        return authenticationFilter;
    }

    /**
     * 애플리케이션의 Spring Security 필터 체인을 구성합니다.
     * <p>
     * 이 메서드는 다음을 설정합니다:
     * <ul>
     *     <li>CSRF 보호 비활성화 (REST API에 적합)</li>
     *     <li>세션 관리를 STATELESS로 설정하여 JWT 기반 인증 지원</li>
     *     <li>URL별 접근 권한 설정:
     *         <ul>
     *             <li>{@code /api/v1/auth/**}: 인증 없이 접근 허용 (회원가입, 로그인, 토큰 갱신 등)</li>
     *             <li>{@code /actuator/**}: 인증 없이 접근 허용 (모니터링 엔드포인트)</li>
     *             <li>{@code /api/v1/users/**}: 인증된 사용자만 접근 허용</li>
     *         </ul>
     *     </li>
     *     <li>사용자 정의 인증 필터({@link AuthenticationFilter})와 권한 부여 필터({@link AuthorizationFilterMvc})의 순서 정의:
     *         <ul>
     *             <li>{@link AuthorizationFilterMvc}는 {@link AuthenticationFilter} 이전에 실행되어,
     *                 로그인 이후의 요청에서 JWT를 검증합니다.</li>
     *             <li>{@link AuthenticationFilter}는 Spring Security의 기본 로그인 필터인
     *                 {@link UsernamePasswordAuthenticationFilter} 이전에 실행되어,
     *                 기존 로그인 흐름을 대체합니다.</li>
     *         </ul>
     *     </li>
     *     <li>기본 formLogin 및 httpBasic 인증 방식 비활성화</li>
     * </ul>
     * </p>
     *
     * @param httpSecurity         HTTP 보안 설정을 위한 {@link HttpSecurity} 객체
     * @param authenticationFilter 로그인 인증을 처리하는 사용자 정의 {@link AuthenticationFilter} 빈
     * @param authorizationFilter  JWT 기반 권한 부여를 처리하는 사용자 정의 {@link AuthorizationFilterMvc} 빈
     * @return 구성된 {@link SecurityFilterChain}
     * @throws Exception 필터 체인 구성 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity httpSecurity,
            AuthenticationFilter authenticationFilter,
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
                                .requestMatchers("/api/v1/auth/**").permitAll()
                                .requestMatchers("/actuator/**").permitAll()
                                .requestMatchers("/api/v1/users/**").authenticated()
                )
                .addFilterBefore(authorizationFilter, AuthenticationFilter.class)
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }
}
