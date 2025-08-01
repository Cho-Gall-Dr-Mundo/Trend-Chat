package com.trendchat.userservice.service;

import com.trendchat.userservice.dto.UserRequest.Signup;
import com.trendchat.userservice.dto.UserResponse;
import com.trendchat.userservice.entity.User;
import com.trendchat.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code AuthServiceImpl}은 사용자 인증 및 회원가입 관련 비즈니스 로직을 처리하는 서비스 구현체입니다. {@link AuthService} 인터페이스를
 * 구현하며, 주로 신규 사용자 생성(회원가입) 기능을 제공합니다.
 * <p>
 * 이 서비스는 {@link UserRepository}를 통해 사용자 데이터를 영속화하고, {@link UserService}를 사용하여 이메일 중복 확인 등의 보조 로직을
 * 수행합니다. 또한, {@link PasswordEncoder}를 사용하여 사용자 비밀번호를 안전하게 암호화합니다.
 * </p>
 * <p>
 * 기본적으로 모든 메서드는 읽기 전용 트랜잭션으로 실행되지만, 사용자 생성과 같이 데이터를 변경하는 메서드는 {@code @Transactional} 어노테이션으로 트랜잭션을
 * 재정의합니다.
 * </p>
 *
 * @see com.trendchat.userservice.service.AuthService
 * @see com.trendchat.userservice.repository.UserRepository
 * @see com.trendchat.userservice.service.UserService
 * @see org.springframework.security.crypto.password.PasswordEncoder
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 새로운 사용자 계정을 생성(회원가입)합니다.
     * <p>
     * 1. 제공된 이메일의 중복 여부를 {@link UserService#existsByEmail(String)}를 통해 확인합니다. 만약 이메일이 이미 존재하면
     * {@link IllegalStateException}이 발생합니다. 2. 사용자 비밀번호를 {@link PasswordEncoder}를 사용하여 암호화합니다. 3.
     * 암호화된 비밀번호와 요청 데이터를 기반으로 새로운 {@link User} 엔티티를 생성합니다. 4. 생성된 사용자 엔티티를 데이터베이스에 저장합니다. 5. 저장된
     * 사용자 정보를 {@link UserResponse.Get} DTO로 변환하여 반환합니다.
     * </p>
     * 이 메서드는 데이터를 변경하므로 읽기-쓰기 트랜잭션으로 실행됩니다.
     *
     * @param userRequest 회원가입에 필요한 사용자 정보를 담고 있는 {@link Signup} DTO
     * @return 생성된 사용자 정보를 담고 있는 {@link UserResponse.Get} DTO
     * @throws IllegalStateException 제공된 이메일이 이미 존재하는 경우
     */
    @Override
    @Transactional
    public UserResponse.Get createUser(Signup userRequest) {
        userService.existsByEmail(userRequest.email());

        String encodedPassword = passwordEncoder.encode(userRequest.password());
        User newUser = User.ofSignup(userRequest, encodedPassword);

        User savedUser = userRepository.save(newUser);

        return new UserResponse.Get(savedUser, false);
    }
}

