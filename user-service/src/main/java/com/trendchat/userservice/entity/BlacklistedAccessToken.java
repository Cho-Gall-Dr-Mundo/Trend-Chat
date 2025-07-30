package com.trendchat.userservice.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

/**
 * {@code BlacklistedAccessToken} 엔티티는 Redis에 저장되는 블랙리스트에 등록된 액세스 토큰 정보를 나타냅니다.
 * <p>
 * 이 클래스는 Spring Data Redis의 {@link RedisHash} 어노테이션을 사용하여 Redis 해시 구조에 매핑됩니다. {@code token} 필드는
 * Redis 해시의 키 역할을 하며, {@link TimeToLive} 어노테이션이 붙은 {@code ttlInSeconds} 필드를 통해 이 엔티티의 Redis 만료
 * 시간(TTL)을 동적으로 설정할 수 있습니다.
 * </p>
 * <p>
 * Lombok의 {@code @Getter}를 통해 필드 접근자를 자동으로 생성하며, {@code @NoArgsConstructor}를 통해 기본 생성자를 제공합니다.
 * </p>
 *
 * @see org.springframework.data.redis.core.RedisHash
 * @see org.springframework.data.redis.core.TimeToLive
 */
@Getter
@NoArgsConstructor
@RedisHash("access_token_blacklist")
public class BlacklistedAccessToken {

    @Id
    private String token;

    @TimeToLive
    private Long ttlInSeconds;

    /**
     * 새로운 {@code BlacklistedAccessToken} 인스턴스를 생성합니다.
     *
     * @param token        블랙리스트에 추가될 액세스 토큰 문자열
     * @param ttlInSeconds 블랙리스트 항목이 유지될 시간 (초 단위)
     */
    public BlacklistedAccessToken(String token, Long ttlInSeconds) {
        this.token = token;
        this.ttlInSeconds = ttlInSeconds;
    }
}