package com.trendchat.userservice.security;

import com.trendchat.trendchatcommon.util.JwtUtil;
import com.trendchat.userservice.entity.RefreshToken;
import com.trendchat.userservice.entity.User;
import com.trendchat.userservice.service.TokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * {@code OAuth2LoginSuccessHandler}는 OAuth2 로그인 성공 후의 커스텀 로직을 처리하는 핸들러입니다. 주로 JWT 토큰을 생성하고 클라이언트에게
 * 전달하는 역할을 수행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${frontend.url}")
    private String frontendUrl;

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    /**
     * OAuth2 인증 성공 시 호출됩니다.
     * <p>
     * 1. {@link PrincipalOAuth2User}에서 우리 서비스의 {@link User} 엔티티를 추출합니다. 2. 사용자 정보를 기반으로 액세스 토큰과
     * 리프레시 토큰을 생성합니다. 3. 생성된 리프레시 토큰을 데이터베이스에 저장합니다. 4. 액세스 토큰은 HTTP 헤더에, 리프레시 토큰은 HTTP Only 쿠키에
     * 추가합니다. 5. 클라이언트를 특정 URL(예: 로그인 성공 페이지)로 리다이렉트합니다.
     * </p>
     *
     * @param request        HTTP 요청 객체
     * @param response       HTTP 응답 객체
     * @param authentication 인증 성공 정보를 담고 있는 {@link Authentication} 객체
     * @throws IOException      입출력 에러 발생 시
     * @throws ServletException 서블릿 에러 발생 시
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        // PrincipalOAuth2User 객체에서 우리 서비스의 User 엔티티 추출
        PrincipalOAuth2User principalOAuth2User = (PrincipalOAuth2User) authentication.getPrincipal();
        User user = principalOAuth2User.getUser();

        String userId = user.getUserId();
        String nickname = user.getNickname();
        String userRole = user.getUserRole().getAuthority();

        // JWT 토큰 생성
        String accessToken = jwtUtil.createAccessToken(userId, nickname, userRole);
        String refreshToken = jwtUtil.createRefreshToken(userId);

        // 리프레시 토큰 저장
        tokenService.saveRefreshToken(new RefreshToken(userId, refreshToken));

        // 토큰을 응답에 추가 (액세스 토큰은 헤더, 리프레시 토큰은 쿠키)
        jwtUtil.addAccessTokenToHeader(response, accessToken);
        jwtUtil.addRefreshTokenToCookie(response, refreshToken);

        log.info("OAuth2 login success, User: {}", userId);
        response.sendRedirect(frontendUrl + "/oauth2/callback?token=" + accessToken);
    }
}