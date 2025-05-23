package com.trendchat.userservice.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.trendchat.trendchatcommon.exception.InvalidTokenException;
import com.trendchat.trendchatcommon.util.JwtUtil;
import com.trendchat.userservice.dto.Token;
import com.trendchat.userservice.entity.RefreshToken;
import com.trendchat.userservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenServiceImpl implements TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void saveRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
    }

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
}
