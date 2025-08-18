package com.trendchat.userservice.service;

import com.trendchat.userservice.dto.Token;
import com.trendchat.userservice.entity.RefreshToken;

/**
 * {@code TokenService} 인터페이스는 사용자 토큰(JWT)과 관련된 비즈니스 로직을 정의합니다.
 * <p>
 * 이 인터페이스는 리프레시 토큰의 저장 및 관리, 액세스 토큰 갱신, 사용자 로그아웃 처리 및 액세스 토큰 블랙리스트 관리와 같은 핵심 기능을 명시합니다.
 * </p>
 *
 * @see com.trendchat.userservice.dto.Token
 * @see com.trendchat.userservice.entity.RefreshToken
 */
public interface TokenService {

    /**
     * 주어진 리프레시 토큰을 저장소에 저장합니다.
     *
     * @param refreshToken 저장할 {@link RefreshToken} 엔티티
     */
    void saveRefreshToken(RefreshToken refreshToken);

    /**
     * 주어진 액세스 토큰 및 리프레시 토큰 쌍을 사용하여 새로운 토큰 쌍을 발급합니다. 이 과정에서 기존 리프레시 토큰의 유효성을 검사하고, 필요한 경우 새로운 토큰으로
     * 갱신합니다.
     *
     * @param tokenPair 갱신할 액세스 토큰 및 리프레시 토큰을 포함하는 {@link Token.Pair} 객체
     * @return 새로 발급된 액세스 토큰 및 리프레시 토큰을 포함하는 {@link Token.Pair} 객체
     */
    Token.Pair refreshTokens(Token.Pair tokenPair);

    /**
     * 사용자 로그아웃을 처리합니다. 이 메서드는 사용자의 액세스 토큰을 무효화하고 리프레시 토큰을 삭제하는 로직을 포함할 수 있습니다.
     *
     * @param tokenPair 로그아웃할 사용자의 액세스 토큰 및 리프레시 토큰을 포함하는 {@link Token.Pair} 객체
     */
    void logout(Token.Pair tokenPair);

    /**
     * 특정 액세스 토큰을 블랙리스트에 추가하여 즉시 무효화합니다. 토큰의 남은 유효 기간을 고려하여 블랙리스트 만료 시간을 설정할 수 있습니다.
     *
     * @param token            블랙리스트에 추가할 액세스 토큰 문자열
     * @param expirationMillis 토큰의 남은 유효 기간 (밀리초 단위)
     */
    void blacklistAccessToken(String token, long expirationMillis);

    /**
     * 특정 사용자 ID에 해당하는 리프레시 토큰을 저장소에서 삭제합니다.
     *
     * @param userId 리프레시 토큰을 삭제할 사용자의 고유 ID
     */
    void deleteRefreshToken(String userId);
}
