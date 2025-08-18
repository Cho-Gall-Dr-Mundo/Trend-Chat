package com.trendchat.userservice.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.trendchat.trendchatcommon.exception.InvalidTokenException;
import com.trendchat.trendchatcommon.util.JwtUtil;
import com.trendchat.userservice.dto.Token;
import com.trendchat.userservice.entity.BlacklistedAccessToken;
import com.trendchat.userservice.entity.BlacklistedUser;
import com.trendchat.userservice.entity.RefreshToken;
import com.trendchat.userservice.repository.AccessTokenBlacklistRepository;
import com.trendchat.userservice.repository.RefreshTokenRepository;
import com.trendchat.userservice.repository.UserBlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * {@code TokenServiceImpl}은 토큰(JWT) 관련 비즈니스 로직을 처리하는 서비스 구현체입니다. {@link TokenService} 인터페이스를 구현하며,
 * 리프레시 토큰 관리, 액세스 토큰 갱신, 로그아웃 처리 및 토큰 블랙리스트 관리 기능을 제공합니다.
 * <p>
 * 이 서비스는 {@link RefreshTokenRepository}, {@link AccessTokenBlacklistRepository}를 사용하여 토큰 데이터를 관리하며,
 * {@link JwtUtil}을 통해 JWT 관련 작업을 수행합니다. 또한, {@link UserService}와 연동하여 사용자 계정 잠금 등의 보안 조치를 취할 수
 * 있습니다.
 * </p>
 * <p>
 * 기본적으로 모든 메서드는 읽기 전용 트랜잭션으로 실행되지만, 데이터를 변경하는 메서드들은 {@code @Transactional} 어노테이션으로 트랜잭션을 재정의합니다.
 * </p>
 *
 * @see com.trendchat.userservice.service.TokenService
 * @see com.trendchat.userservice.repository.RefreshTokenRepository
 * @see com.trendchat.userservice.repository.AccessTokenBlacklistRepository
 * @see com.trendchat.trendchatcommon.util.JwtUtil
 * @see com.trendchat.userservice.service.UserService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenServiceImpl implements TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;
    private final UserBlacklistRepository userBlacklistRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 주어진 {@code RefreshToken} 객체를 데이터베이스에 저장합니다.
     *
     * @param refreshToken 저장할 {@link RefreshToken} 객체
     */
    @Override
    @Transactional
    public void saveRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * 주어진 토큰 쌍(액세스 토큰 및 리프레시 토큰)을 사용하여 새로운 토큰 쌍을 발급합니다.
     * <p>
     * 이 과정에서 기존 리프레시 토큰의 유효성을 검사하며, 리프레시 토큰 불일치(변조 시도 등)가 감지될 경우 해당 사용자 계정을 잠그고 예외를 발생시킵니다.
     * </p>
     *
     * @param tokenPair 갱신할 액세스 토큰 및 리프레시 토큰을 포함하는 {@link Token.Pair} 객체
     * @return 새로 발급된 액세스 토큰 및 리프레시 토큰을 포함하는 {@link Token.Pair} 객체
     * @throws InvalidTokenException 제공된 액세스 토큰으로부터 사용자 정보를 추출할 수 없거나, 해당 사용자의 리프레시 토큰을 데이터베이스에서 찾을
     *                               수 없는 경우
     * @throws SecurityException     제공된 리프레시 토큰이 데이터베이스에 저장된 값과 일치하지 않아 토큰 변조 시도가 감지된 경우
     */
    @Override
    @Transactional
    public Token.Pair refreshTokens(Token.Pair tokenPair) {
        DecodedJWT info = jwtUtil.getUserInfoFromToken(tokenPair.accessToken());

        RefreshToken refreshToken = refreshTokenRepository.findById(info.getSubject())
                .orElseThrow(() -> {
                            log.error("Token not found with userId: {}", info.getSubject());
                            return new InvalidTokenException("Not found refreshToken");
                        }
                );

        if (!refreshToken.getRefreshToken().equals(tokenPair.refreshToken())) {
            log.warn("Anomaly Detection: token mismatch detected for user {}", info.getSubject());

            userService.lockAccount(info.getSubject());
            refreshTokenRepository.delete(refreshToken);
            userBlacklistRepository.save(new BlacklistedUser(info.getSubject(), 30 * 60L));

            throw new SecurityException("Detected tampered refresh token");
        } else {
            String newAccessToken = jwtUtil.createAccessToken(
                    info.getSubject(),
                    info.getClaim("nickname").asString(),
                    info.getClaim("role").asString()
            );
            String newRefreshToken = jwtUtil.createRefreshToken(info.getSubject());

            refreshTokenRepository.save(new RefreshToken(info.getSubject(), newRefreshToken));

            return new Token.Pair(newAccessToken, newRefreshToken);
        }
    }

    /**
     * 사용자 로그아웃을 처리합니다.
     * <p>
     * 제공된 액세스 토큰이 아직 유효 기간이 남아있다면 블랙리스트에 추가하여 즉시 무효화하고, 해당 사용자의 리프레시 토큰을 데이터베이스에서 삭제합니다.
     * </p>
     *
     * @param tokenPair 로그아웃할 사용자의 액세스 토큰 및 리프레시 토큰을 포함하는 {@link Token.Pair} 객체
     */
    @Override
    @Transactional
    public void logout(Token.Pair tokenPair) {
        String accessToken = tokenPair.accessToken();
        String refreshToken = tokenPair.refreshToken();

        if (StringUtils.hasText(accessToken)) {
            try {
                DecodedJWT decoded = jwtUtil.validateToken(accessToken);
                long remainingMillis =
                        decoded.getExpiresAt().getTime() - System.currentTimeMillis();

                if (remainingMillis > 0) {
                    blacklistAccessToken(accessToken, remainingMillis);
                }
            } catch (Exception e) {
                log.warn("Access token validation failed during logout: {}", e.getMessage());
            }
        }

        if (StringUtils.hasText(refreshToken)) {
            try {
                String userId = jwtUtil.getUserInfoFromToken(refreshToken).getSubject();
                deleteRefreshToken(userId);
            } catch (Exception e) {
                log.warn("Failed to parse refresh token during logout: {}", e.getMessage());
            }
        }
    }

    /**
     * 특정 액세스 토큰을 블랙리스트에 추가하여 무효화합니다. 토큰의 남은 유효 기간을 기반으로 블랙리스트 만료 시간을 설정합니다.
     *
     * @param token            블랙리스트에 추가할 액세스 토큰 문자열
     * @param expirationMillis 토큰의 남은 유효 기간 (밀리초 단위)
     */
    @Override
    @Transactional
    public void blacklistAccessToken(String token, long expirationMillis) {
        long ttl = expirationMillis / 1000;
        BlacklistedAccessToken blacklisted = new BlacklistedAccessToken(token, ttl);
        accessTokenBlacklistRepository.save(blacklisted);
        log.info("Access token blacklisted for {} seconds: {}", ttl, token);
    }

    /**
     * 특정 사용자 ID에 해당하는 리프레시 토큰을 데이터베이스에서 삭제합니다.
     *
     * @param userId 리프레시 토큰을 삭제할 사용자의 고유 ID
     */
    @Override
    @Transactional
    public void deleteRefreshToken(String userId) {
        refreshTokenRepository.deleteById(userId);
    }
}
