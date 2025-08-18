package com.trendchat.userservice.controller;

import com.trendchat.trendchatcommon.util.JwtUtil;
import com.trendchat.userservice.dto.Token;
import com.trendchat.userservice.dto.UserRequest;
import com.trendchat.userservice.dto.UserResponse;
import com.trendchat.userservice.service.AuthService;
import com.trendchat.userservice.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code AuthController}는 사용자 인증 및 계정 생성과 관련된 HTTP 요청을 처리하는 REST 컨트롤러입니다.
 * <p>
 * 이 컨트롤러는 사용자 회원가입 및 JWT 토큰 갱신 기능을 제공합니다. {@link AuthService}를 통해 사용자 생성 로직을 호출하고,
 * {@link TokenService} 및 {@link JwtUtil}을 사용하여 토큰 관련 작업을 수행합니다. 모든 경로는 {@code /api/v1/auth}로
 * 시작합니다.
 * </p>
 *
 * @see com.trendchat.userservice.service.AuthService
 * @see com.trendchat.userservice.service.TokenService
 * @see com.trendchat.trendchatcommon.util.JwtUtil
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final TokenService tokenService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    /**
     * 새로운 사용자 계정을 생성(회원가입)하는 엔드포인트입니다.
     * <p>
     * 이 엔드포인트는 {@code /api/v1/auth/signup} 경로의 POST 요청에 응답하며, 요청 본문에 포함된 사용자 정보를 기반으로
     * {@link AuthService#createUser(UserRequest.Signup)}를 호출하여 사용자 계정을 생성합니다.
     * </p>
     *
     * @param userRequest 회원가입에 필요한 사용자 정보를 담고 있는 {@link UserRequest.Signup} DTO
     * @return 생성된 사용자 정보({@link UserResponse.Get})와 HTTP 201 (Created) 상태를 포함하는
     * {@link ResponseEntity}
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponse.Get> signup(@RequestBody UserRequest.Signup userRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createUser(userRequest));
    }

    /**
     * 액세스 토큰 및 리프레시 토큰을 갱신하는 엔드포인트입니다.
     * <p>
     * 이 엔드포인트는 {@code /api/v1/auth/refresh} 경로의 POST 요청에 응답하며, 요청 헤더와 쿠키에서 기존 액세스 토큰 및 리프레시 토큰을
     * 추출합니다. 추출된 토큰 쌍을 {@link TokenService#refreshTokens(Token.Pair)}에 전달하여 새로운 토큰 쌍을 발급받고, 이를 응답
     * 헤더와 쿠키에 추가합니다.
     * </p>
     *
     * @param request  HTTP 요청 객체. 액세스 토큰 및 리프레시 토큰 추출에 사용됩니다.
     * @param response HTTP 응답 객체. 새로운 액세스 토큰 및 리프레시 토큰을 추가하는 데 사용됩니다.
     * @return 새로 발급된 토큰 쌍({@link Token.Pair})과 HTTP 201 (Created) 상태를 포함하는 {@link ResponseEntity}
     */
    @PostMapping("/refresh")
    public ResponseEntity<Token.Pair> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String accessToken = jwtUtil.getAccessTokenFromHeader(request);
        String refreshToken = jwtUtil.resolveTokenFromCookie(request);

        Token.Pair oldTokens = new Token.Pair(accessToken, refreshToken);
        Token.Pair newTokens = tokenService.refreshTokens(oldTokens);

        jwtUtil.addAccessTokenToHeader(response, newTokens.accessToken());
        jwtUtil.addRefreshTokenToCookie(response, newTokens.refreshToken());

        return ResponseEntity.status(HttpStatus.CREATED).body(newTokens);
    }
}
