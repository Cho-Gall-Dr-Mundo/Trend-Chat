package com.trendchat.userservice.config;

import com.trendchat.trendchatcommon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@code JwtUtilConfig}는 JWT(JSON Web Token) 관련 유틸리티 클래스인 {@link JwtUtil}을 Spring 빈으로 구성하는 클래스입니다.
 * <p>
 * 이 클래스는 애플리케이션의 설정 파일(예: {@code application.yml} 또는 설정 서버)에서 JWT 비밀 키를 주입받아 {@link JwtUtil} 인스턴스를
 * 생성합니다. {@code @RefreshScope} 어노테이션은 설정이 변경될 경우 애플리케이션 재시작 없이 빈을 동적으로 갱신할 수 있도록 합니다.
 * </p>
 *
 * @see com.trendchat.trendchatcommon.util.JwtUtil
 * @see org.springframework.cloud.context.config.annotation.RefreshScope
 */
@RefreshScope
@Configuration
public class JwtUtilConfig {

    @Value("${jwt.token.access.secret.key}")
    private String accessSecretKey;

    @Value("${jwt.token.refresh.secret.key}")
    private String refreshSecretKey;

    /**
     * {@link JwtUtil} 빈을 생성하여 Spring 컨텍스트에 등록합니다. 주입받은 액세스 토큰 비밀 키와 리프레시 토큰 비밀 키를 사용하여
     * {@link JwtUtil} 인스턴스를 초기화합니다.
     *
     * @return 초기화된 {@link JwtUtil} 인스턴스
     */
    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(accessSecretKey, refreshSecretKey);
    }
}
