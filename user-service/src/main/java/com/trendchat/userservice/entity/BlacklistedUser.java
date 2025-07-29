package com.trendchat.userservice.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

/**
 * {@code BlacklistedUser} 엔티티는 Redis에 저장되는 블랙리스트에 등록된 사용자 정보를 나타냅니다.
 * <p>
 * 이 클래스는 Spring Data Redis의 {@link RedisHash} 어노테이션을 사용하여 Redis 해시 구조에 매핑됩니다. {@code userId} 필드는
 * Redis 해시의 키 역할을 하며, {@link TimeToLive} 어노테이션이 붙은 {@code ttlInSeconds} 필드를 통해 이 엔티티의 Redis 만료
 * 시간(TTL)을 동적으로 설정할 수 있습니다.
 * </p>
 * <p>
 * Lombok의 {@code @Getter}를 통해 필드 접근자를 자동으로 생성하며,
 * {@code @NoArgsConstructor(access = AccessLevel.PROTECTED)}를 통해 기본 생성자를 보호 레벨로 설정하여 특정 생성자를 통한
 * 인스턴스 생성을 유도합니다.
 * </p>
 *
 * @see org.springframework.data.redis.core.RedisHash
 * @see org.springframework.data.redis.core.TimeToLive
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash("blacklisted_user")
public class BlacklistedUser {

    @Id
    private String userId;

    @TimeToLive
    private Long ttlInSeconds;

    /**
     * 새로운 {@code BlacklistedUser} 인스턴스를 생성합니다.
     *
     * @param userId       블랙리스트에 추가될 사용자의 고유 ID
     * @param ttlInSeconds 블랙리스트 항목이 유지될 시간 (초 단위)
     */
    public BlacklistedUser(String userId, Long ttlInSeconds) {
        this.userId = userId;
        this.ttlInSeconds = ttlInSeconds;
    }
}