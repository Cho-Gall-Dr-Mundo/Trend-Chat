package com.trendchat.paymentservice.util;

import com.trendchat.trendchatcommon.util.BlacklistChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * {@code RedisBlacklistChecker}는 {@link BlacklistChecker} 인터페이스의 구현체로, Redis를 활용하여 사용자 ID 또는 액세스
 * 토큰이 블랙리스트에 등록되었는지 확인합니다.
 * <p>
 * 이 컴포넌트는 스프링의 의존성 주입을 통해 {@link RedisTemplate}을 사용하여 Redis 인스턴스와 상호작용합니다. 사용자 블랙리스트 및 액세스 토큰
 * 블랙리스트를 위한 별도의 키 접두사를 관리합니다.
 * </p>
 *
 * @see com.trendchat.trendchatcommon.util.BlacklistChecker
 * @see org.springframework.data.redis.core.RedisTemplate
 */
@Component
@RequiredArgsConstructor
public class RedisBlacklistChecker implements BlacklistChecker {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String USER_BLACKLIST_PREFIX = "blacklisted_user:";
    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "access_token_blacklist:";

    /**
     * 제공된 {@code userId} 또는 {@code accessToken} 중 하나라도 Redis에 저장된 해당 블랙리스트에 존재하는지 확인합니다. 이 메서드는 둘 중
     * 적어도 하나가 블랙리스트에 있다면 {@code true}를 반환합니다.
     *
     * @param userId      확인할 사용자 ID입니다. 이 값은 Redis 키를 형성하기 위해 {@code USER_BLACKLIST_PREFIX}와
     *                    결합됩니다.
     * @param accessToken 확인할 액세스 토큰 문자열입니다. 이 값은 Redis 키를 형성하기 위해
     *                    {@code ACCESS_TOKEN_BLACKLIST_PREFIX}와 결합됩니다.
     * @return 사용자 ID 또는 액세스 토큰 (또는 둘 다)이 Redis의 해당 블랙리스트에 있다면 {@code true}, 그렇지 않으면 {@code false}를
     * 반환합니다.
     */
    @Override
    public boolean isBlacklisted(String userId, String accessToken) {
        boolean userBlacklisted = Boolean.TRUE.equals(redisTemplate
                .hasKey(USER_BLACKLIST_PREFIX + userId));

        boolean tokenBlacklisted = Boolean.TRUE.equals(redisTemplate
                .hasKey(ACCESS_TOKEN_BLACKLIST_PREFIX + accessToken));

        return userBlacklisted || tokenBlacklisted;
    }
}