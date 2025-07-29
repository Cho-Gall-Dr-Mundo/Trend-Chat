package com.trendchat.userservice.repository;

import com.trendchat.userservice.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * {@code UserRepository}는 {@link User} 엔티티에 대한 데이터베이스 접근을 담당하는 Spring Data JPA 레포지토리 인터페이스입니다.
 * <p>
 * {@link JpaRepository}를 확장하여 기본적인 CRUD(생성, 조회, 업데이트, 삭제) 작업을 제공하며, 사용자 이메일 및 고유 ID를 기반으로 사용자를
 * 조회하거나 존재 여부를 확인하는 사용자 정의 쿼리 메서드를 정의합니다.
 * </p>
 *
 * @see com.trendchat.userservice.entity.User
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 주어진 이메일 주소를 가진 사용자가 데이터베이스에 존재하는지 확인합니다.
     *
     * @param email 확인할 이메일 주소 문자열
     * @return 해당 이메일을 가진 사용자가 존재하면 {@code true}, 그렇지 않으면 {@code false}
     */
    boolean existsByEmail(String email);

    /**
     * 주어진 이메일 주소에 해당하는 사용자 엔티티를 조회합니다.
     *
     * @param email 조회할 이메일 주소 문자열
     * @return 해당 이메일을 가진 {@link User} 엔티티를 포함하는 {@link Optional}, 사용자를 찾을 수 없으면 빈 {@link Optional}
     */
    Optional<User> findByEmail(String email);

    /**
     * 주어진 사용자 고유 ID ({@code userId})에 해당하는 사용자 엔티티를 조회합니다.
     *
     * @param userId 조회할 사용자 고유 ID 문자열
     * @return 해당 사용자 고유 ID를 가진 {@link User} 엔티티를 포함하는 {@link Optional}, 사용자를 찾을 수 없으면 빈
     * {@link Optional}
     */
    Optional<User> findByUserId(String userId);
}
