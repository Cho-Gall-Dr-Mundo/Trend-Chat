package com.trendchat.userservice.service;

import com.trendchat.userservice.dto.UserRequest;
import com.trendchat.userservice.dto.UserResponse;

/**
 * {@code AuthService} 인터페이스는 사용자 인증 및 회원가입과 관련된 비즈니스 로직을 정의합니다.
 * <p>
 * 이 인터페이스는 주로 신규 사용자 계정 생성(회원가입) 기능을 명시하며, 사용자의 인증 흐름에 필요한 추가적인 메서드를 포함할 수 있습니다.
 * </p>
 *
 * @see com.trendchat.userservice.dto.UserRequest.Signup
 * @see com.trendchat.userservice.dto.UserResponse.Get
 */
public interface AuthService {

    /**
     * 새로운 사용자 계정을 생성(회원가입)합니다.
     * <p>
     * 이 메서드는 제공된 사용자 정보를 기반으로 새로운 사용자를 시스템에 등록합니다. 구현체는 비밀번호 암호화, 이메일 중복 확인 등의 비즈니스 규칙을 적용할 수
     * 있습니다.
     * </p>
     *
     * @param userRequest 회원가입에 필요한 사용자 정보를 담고 있는 {@link UserRequest.Signup} DTO
     * @return 생성된 사용자 정보를 담고 있는 {@link UserResponse.Get} DTO
     */
    UserResponse.Get createUser(UserRequest.Signup userRequest);
}
