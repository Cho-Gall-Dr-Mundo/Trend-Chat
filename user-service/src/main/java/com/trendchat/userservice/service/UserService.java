package com.trendchat.userservice.service;

import com.trendchat.userservice.dto.UserRequest;
import com.trendchat.userservice.dto.UserResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * {@code UserService} 인터페이스는 사용자 관련 비즈니스 로직을 정의합니다.
 * <p>
 * 이 인터페이스는 Spring Security의 {@link UserDetailsService}를 확장하여 사용자 인증 및 권한 부여를 위한 사용자 상세 정보 로드 기능을
 * 포함합니다. 또한, 사용자 계정 관리 (이메일 중복 확인, 계정 잠금) 및 사용자 정보 조회와 같은 서비스 계층의 핵심 기능을 명시합니다.
 * </p>
 *
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see com.trendchat.userservice.dto.UserResponse.Get
 */
public interface UserService extends UserDetailsService {

    /**
     * 주어진 이메일 주소가 시스템에 이미 존재하는지 확인합니다. 구현체는 중복 이메일이 발견될 경우 적절한 예외를 발생시킬 수 있습니다.
     *
     * @param email 확인할 이메일 문자열
     */
    void existsByEmail(String email);

    /**
     * 특정 사용자 계정을 잠금 처리합니다. 이 메서드는 계정 잠금 외에 관련 비즈니스 로직(예: 블랙리스트 추가)을 포함할 수 있습니다.
     *
     * @param userId 잠금 처리할 사용자의 고유 ID
     */
    void lockAccount(String userId);

    /**
     * 특정 사용자 ID에 해당하는 사용자 정보를 조회하여 {@link UserResponse.Get} DTO 형태로 반환합니다. 사용자를 찾을 수 없는 경우 구현체는
     * {@link jakarta.ws.rs.NotFoundException} 등 적절한 예외를 발생시킬 수 있습니다.
     *
     * @param userId 조회할 사용자의 고유 ID
     * @return 조회된 사용자 정보가 포함된 {@link UserResponse.Get} 객체
     */
    UserResponse.Get getUser(String userId);

    void updateNickname(String userId, String newNickname);

    void updatePassword(String userId, UserRequest.UpdatePassword request);
}
