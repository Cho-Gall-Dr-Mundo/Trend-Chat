package com.trendchat.userservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendchat.trendchatcommon.util.JwtUtil;
import com.trendchat.userservice.dto.UserRequest;
import com.trendchat.userservice.entity.RefreshToken;
import com.trendchat.userservice.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * {@code AuthenticationFilter}는 Spring Security에서 사용자 인증을 처리하는 커스텀 필터입니다.
 * <p>
 * {@link UsernamePasswordAuthenticationFilter}를 확장하여 로그인 요청을 가로채고, 요청 본문에서 이메일과 비밀번호를 추출하여 인증을
 * 시도합니다. 인증 성공 시 JWT(JSON Web Token) 형태의 액세스 토큰과 리프레시 토큰을 발급하고, 응답 헤더 및 쿠키에 이를 추가합니다. 인증 실패 시에는 적절한
 * HTTP 상태 코드를 반환합니다.
 * </p>
 * <p>
 * 이 필터는 {@link JwtUtil}을 사용하여 토큰을 생성하고 관리하며, {@link TokenService}를 통해 리프레시 토큰을 저장합니다.
 * </p>
 *
 * @see org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
 * @see com.trendchat.trendchatcommon.util.JwtUtil
 * @see com.trendchat.userservice.service.TokenService
 * @see com.trendchat.userservice.security.PrincipalDetails
 */
@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    /**
     * 로그인 시도를 처리합니다.
     * <p>
     * HTTP 요청의 입력 스트림에서 {@link UserRequest.Login} 형식으로 이메일과 비밀번호를 읽어와
     * {@link UsernamePasswordAuthenticationToken}을 생성한 후,
     * {@link org.springframework.security.authentication.AuthenticationManager}에 인증을 위임합니다.
     * </p>
     *
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @return 인증 성공 시 {@link Authentication} 객체
     * @throws AuthenticationException 인증 실패 시 발생하는 예외
     * @throws RuntimeException        요청 본문을 읽는 중 {@link IOException}이 발생할 경우
     */
    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException {
        try {
            UserRequest.Login loginRequest = new ObjectMapper()
                    .readValue(request.getInputStream(), UserRequest.Login.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 사용자 인증 성공 시 호출됩니다.
     * <p>
     * 인증된 {@link PrincipalDetails} 객체에서 사용자 정보를 추출하여 액세스 토큰과 리프레시 토큰을 생성합니다. 생성된 리프레시 토큰은 데이터베이스에
     * 저장되고, 액세스 토큰은 응답 헤더에, 리프레시 토큰은 HTTP Only 쿠키에 추가됩니다.
     * </p>
     *
     * @param request    HTTP 요청 객체
     * @param response   HTTP 응답 객체
     * @param chain      필터 체인
     * @param authResult 인증 성공 결과 ({@link Authentication} 객체)
     */
    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult
    ) {
        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();

        String userId = principalDetails.getUser().getUserId();
        String nickname = principalDetails.getUser().getNickname();
        String userRole = principalDetails.getUser().getUserRole().getAuthority();

        String accessToken = jwtUtil.createAccessToken(userId, nickname, userRole);
        String refreshToken = jwtUtil.createRefreshToken(userId);

        tokenService.saveRefreshToken(new RefreshToken(userId, refreshToken));

        jwtUtil.addAccessTokenToHeader(response, accessToken);
        jwtUtil.addRefreshTokenToCookie(response, refreshToken);
    }

    /**
     * 사용자 인증 실패 시 호출됩니다.
     * <p>
     * HTTP 응답 상태 코드를 401 (Unauthorized)로 설정하고, 부모 클래스의 실패 처리 로직을 호출합니다.
     * </p>
     *
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param failed   인증 실패 시 발생하는 {@link AuthenticationException}
     * @throws IOException      입출력 에러 발생 시
     * @throws ServletException 서블릿 에러 발생 시
     */
    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed
    ) throws IOException, ServletException {
        super.unsuccessfulAuthentication(request, response, failed);
        response.setStatus(401);
    }
}
