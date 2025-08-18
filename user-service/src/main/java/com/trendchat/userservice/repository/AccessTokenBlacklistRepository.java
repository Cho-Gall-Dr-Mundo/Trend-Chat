package com.trendchat.userservice.repository;

import com.trendchat.userservice.entity.BlacklistedAccessToken;
import org.springframework.data.repository.CrudRepository;

/**
 * {@code AccessTokenBlacklistRepository}는 {@link BlacklistedAccessToken} 엔티티에 대한 데이터베이스 접근을 담당하는
 * Spring Data 레포지토리 인터페이스입니다.
 * <p>
 * {@link CrudRepository}를 확장하여 블랙리스트에 등록된 액세스 토큰 엔티티에 대한 기본적인 CRUD(생성, 조회, 업데이트, 삭제) 작업을 제공합니다. 이
 * 인터페이스는 {@link BlacklistedAccessToken} 엔티티의 ID 타입이 {@code String}임을 명시합니다.
 * </p>
 *
 * @see com.trendchat.userservice.entity.BlacklistedAccessToken
 * @see org.springframework.data.repository.CrudRepository
 */
public interface AccessTokenBlacklistRepository extends
        CrudRepository<BlacklistedAccessToken, String> {

}