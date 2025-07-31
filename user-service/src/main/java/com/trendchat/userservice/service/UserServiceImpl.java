package com.trendchat.userservice.service;

import com.trendchat.userservice.dto.UserRequest;
import com.trendchat.userservice.dto.UserResponse;
import com.trendchat.userservice.entity.BlacklistedUser;
import com.trendchat.userservice.entity.User;
import com.trendchat.userservice.repository.UserBlacklistRepository;
import com.trendchat.userservice.repository.UserRepository;
import com.trendchat.userservice.security.PrincipalDetails;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code UserServiceImpl}은 사용자 관련 비즈니스 로직을 처리하는 서비스 구현체입니다. {@link UserService} 인터페이스를 구현하며, 사용자 정보
 * 조회, 중복 이메일 확인, 계정 잠금 및 블랙리스트 관리 기능을 제공합니다.
 * <p>
 * 이 서비스는 {@link UserRepository}와 {@link UserBlacklistRepository}를 사용하여 데이터베이스와 상호작용합니다. 기본적으로 모든
 * 메서드는 읽기 전용 트랜잭션으로 실행되지만, 일부 메서드는 트랜잭션 전파 속성을 재정의합니다.
 * </p>
 *
 * @see com.trendchat.userservice.service.UserService
 * @see com.trendchat.userservice.repository.UserRepository
 * @see com.trendchat.userservice.repository.UserBlacklistRepository
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserBlacklistRepository userBlacklistRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 특정 이메일 주소가 이미 존재하는지 확인합니다. 이메일이 이미 존재할 경우, {@link IllegalStateException}을 발생시킵니다.
     *
     * @param email 확인할 이메일 문자열
     * @throws IllegalStateException 이메일이 이미 존재하는 경우
     */
    @Override
    public void existsByEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            log.error("Duplicate email requests occurred: email='{}'", email);
            throw new IllegalStateException("Already exists email");
        }
    }

    /**
     * 주어진 이메일로 사용자 정보를 로드하여 {@link PrincipalDetails} 객체를 반환합니다. 이 메서드는 Spring Security의 사용자 인증 과정에서
     * 사용됩니다. 사용자를 찾을 수 없는 경우 {@link UsernameNotFoundException}을 발생시킵니다.
     *
     * @param email 로드할 사용자의 이메일
     * @return 로드된 사용자 정보가 포함된 {@link PrincipalDetails} 객체
     * @throws UsernameNotFoundException 해당 이메일로 사용자를 찾을 수 없는 경우
     */
    @Override
    public PrincipalDetails loadUserByUsername(String email) {
        return new PrincipalDetails(isValidEmail(email));
    }

    /**
     * 특정 사용자 계정을 잠그고, 해당 사용자를 일정 기간 동안 블랙리스트에 추가합니다. 계정 잠금은 사용자의 상태를 변경하며, 블랙리스트 추가는
     * {@link BlacklistedUser} 엔티티를 저장합니다. 이 메서드는 새로운 트랜잭션으로 실행됩니다 (Propagation.REQUIRES_NEW).
     *
     * @param userId 잠그고 블랙리스트에 추가할 사용자의 고유 ID
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void lockAccount(String userId) {
        User user = isValidUser(userId);
        user.accountLock();

        long blacklistDurationMillis = 30 * 60 * 1000L;
        blacklistUser(userId, blacklistDurationMillis);
    }

    /**
     * 특정 사용자 ID에 해당하는 사용자 정보를 조회하여 {@link UserResponse.Get} DTO로 반환합니다. 사용자를 찾을 수 없는 경우
     * {@link NotFoundException}을 발생시킵니다.
     *
     * @param userId 조회할 사용자의 고유 ID
     * @return 조회된 사용자 정보가 포함된 {@link UserResponse.Get} 객체
     * @throws NotFoundException 해당 사용자 ID로 사용자를 찾을 수 없는 경우
     */
    @Override
    public UserResponse.Get getUser(String userId) {
        User user = isValidUser(userId);
        boolean isSocial = false;
        if (user.getPassword().equals("{noop}")) {
            isSocial = true;
        }
        return new UserResponse.Get(user, isSocial);
    }

    @Override
    @Transactional
    public void updateNickname(String userId, String newNickname) {
        if (newNickname == null || newNickname.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter a nickname.");
        }
        User user = isValidUser(userId);

        if (user.getNickname().equals(newNickname)) {
            throw new IllegalStateException("This nickname is already in use.");
        }

        user.updateNickname(newNickname);
    }

    @Override
    @Transactional
    public void updatePassword(String userId, UserRequest.UpdatePassword request) {
        User user = isValidUser(userId);

        if (user.getPassword().equals("{noop}")) {
            throw new IllegalStateException("Social login users cannot change password.");
        }

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        if (request.newPassword() == null || request.newPassword().length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters.");
        }

        user.updatePassword(passwordEncoder.encode(request.newPassword()));
    }

    /**
     * 주어진 사용자 ID를 지정된 기간 동안 블랙리스트에 추가합니다. 블랙리스트 정보는 {@link UserBlacklistRepository}를 통해 저장됩니다.
     *
     * @param userId         블랙리스트에 추가할 사용자의 고유 ID
     * @param durationMillis 블랙리스트 유지 기간 (밀리초 단위)
     */
    private void blacklistUser(String userId, long durationMillis) {
        long ttlInSeconds = durationMillis / 1000;

        BlacklistedUser blacklisted = new BlacklistedUser(
                userId,
                ttlInSeconds
        );

        userBlacklistRepository.save(blacklisted);
        log.info("User [{}] has been blacklisted for {} seconds", userId, ttlInSeconds);
    }

    /**
     * 주어진 이메일로 사용자 정보를 조회합니다. 사용자를 찾을 수 없는 경우 {@link UsernameNotFoundException}을 발생시킵니다.
     *
     * @param email 조회할 사용자의 이메일
     * @return 조회된 {@link User} 엔티티
     * @throws UsernameNotFoundException 해당 이메일로 사용자를 찾을 수 없는 경우
     */
    private User isValidEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("User not found with email: {}", email);
            return new UsernameNotFoundException("User not found");
        });
    }

    /**
     * 주어진 사용자 ID로 사용자 정보를 조회합니다. 사용자를 찾을 수 없는 경우 {@link NotFoundException}을 발생시킵니다.
     *
     * @param userId 조회할 사용자의 고유 ID
     * @return 조회된 {@link User} 엔티티
     * @throws NotFoundException 해당 사용자 ID로 사용자를 찾을 수 없는 경우
     */
    private User isValidUser(String userId) {
        return userRepository.findByUserId(userId).orElseThrow(() -> {
            log.error("User not found with userId: {}", userId);
            return new NotFoundException("User not found");
        });
    }
}
