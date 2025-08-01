package com.trendchat.userservice.controller;

import com.trendchat.trendchatcommon.auth.AuthUser;
import com.trendchat.trendchatcommon.util.JwtUtil;
import com.trendchat.userservice.dto.Token;
import com.trendchat.userservice.dto.UserRequest;
import com.trendchat.userservice.dto.UserResponse;
import com.trendchat.userservice.service.TokenService;
import com.trendchat.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code UserController}는 사용자 관련 HTTP 요청을 처리하는 REST 컨트롤러입니다.
 * <p>
 * 이 컨트롤러는 현재 인증된 사용자 정보를 조회하고, 사용자 로그아웃 기능을 제공합니다. {@link UserService}를 통해 사용자 비즈니스 로직을 호출하고,
 * {@link TokenService} 및 {@link JwtUtil}을 사용하여 토큰 관련 작업을 수행합니다. 모든 경로는 {@code /api/v1/users}로
 * 시작합니다.
 * </p>
 *
 * @see com.trendchat.userservice.service.UserService
 * @see com.trendchat.userservice.service.TokenService
 * @see com.trendchat.trendchatcommon.util.JwtUtil
 * @see com.trendchat.trendchatcommon.auth.AuthUser
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;

    /**
     * 현재 인증된 사용자의 상세 정보를 조회하여 반환합니다.
     * <p>
     * 이 엔드포인트는 {@code /api/v1/users/me} 경로의 GET 요청에 응답하며, {@code @AuthenticationPrincipal} 어노테이션을
     * 통해 현재 로그인한 사용자의 정보를 자동으로 주입받습니다.
     * </p>
     *
     * @param authUser 현재 인증된 사용자의 정보를 담고 있는 {@link AuthUser} 객체입니다. Spring Security의
     *                 {@code @AuthenticationPrincipal}을 통해 주입됩니다.
     * @return 조회된 사용자 정보({@link UserResponse.Get})와 HTTP 200 (OK) 상태를 포함하는 {@link ResponseEntity}
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse.Get> signup(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getUser(authUser.getUserId()));
    }

    /**
     * 사용자 로그아웃을 처리합니다.
     * <p>
     * 이 엔드포인트는 {@code /api/v1/users/logout} 경로의 POST 요청에 응답하며, 요청 헤더와 쿠키에서 액세스 토큰 및 리프레시 토큰을 추출합니다.
     * 추출된 토큰은 {@link TokenService#logout(Token.Pair)} 메서드를 통해 무효화되고, 클라이언트의 관련 쿠키는
     * {@link JwtUtil#clearAllCookies(HttpServletRequest, HttpServletResponse)}를 통해 제거됩니다.
     * </p>
     *
     * @param request  HTTP 요청 객체. 액세스 토큰 및 리프레시 토큰 추출에 사용됩니다.
     * @param response HTTP 응답 객체. 쿠키 제거 및 응답 설정에 사용됩니다.
     * @return HTTP 204 (No Content) 상태를 포함하는 {@link ResponseEntity}, 로그아웃 성공 시 반환됩니다.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = jwtUtil.getAccessTokenFromHeader(request);
        String refreshToken = jwtUtil.resolveTokenFromCookie(request);

        tokenService.logout(new Token.Pair(accessToken, refreshToken));
        jwtUtil.clearAllCookies(request, response);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/nickname")
    public ResponseEntity<Void> updateNickname(
            @RequestBody UserRequest.UpdateNickname request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        userService.updateNickname(authUser.getUserId(), request.nickname());
        String newAccessToken = jwtUtil.createAccessToken(
                authUser.getUserId(),
                request.nickname(),
                authUser.getUserRole().getAuthority()
        );

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + newAccessToken)
                .build();
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(
            @RequestBody UserRequest.UpdatePassword request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        userService.updatePassword(authUser.getUserId(), request);
        return ResponseEntity.noContent().build();
    }
}
