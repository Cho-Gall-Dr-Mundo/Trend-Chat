package com.trendchat.userservice.entity;

import com.trendchat.trendchatcommon.enums.UserRole;
import com.trendchat.userservice.dto.UserRequest;
import com.trendchat.userservice.security.PrincipalOAuth2User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * {@code User} 엔티티는 사용자 정보를 나타내는 JPA 엔티티입니다.
 * <p>
 * 이 클래스는 데이터베이스의 {@code users} 테이블에 매핑되며, 사용자의 이메일, 닉네임, 비밀번호, 고유 ID, 역할, 계정 잠금 상태 등의 정보를 관리합니다.
 * Spring Data JPA의 Auditing 기능을 사용하여 생성 및 마지막 수정 시간을 자동으로 기록합니다.
 * </p>
 * <p>
 * Lombok을 사용하여 보일러플레이트 코드를 줄였으며, 기본 생성자는 보호(protected) 레벨로 설정하여 직접적인 인스턴스 생성을 제한하고 정적 팩토리 메서드를 통해
 * 생성하도록 유도합니다.
 * </p>
 *
 * @see com.trendchat.trendchatcommon.enums.UserRole
 * @see com.trendchat.userservice.dto.UserRequest.Signup
 * @see org.springframework.data.jpa.domain.support.AuditingEntityListener
 */
@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Column(nullable = false)
    private Boolean isAccountNonLocked;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * 회원가입 요청과 암호화된 비밀번호를 사용하여 새로운 {@code User} 엔티티를 생성하는 private 생성자입니다. {@code userId}는 UUID로 자동
     * 생성되며, {@code userRole}은 기본적으로 {@code ROLE_FREE}로 설정됩니다. 계정은 기본적으로 잠겨있지 않은(non-locked) 상태로
     * 생성됩니다.
     *
     * @param userRequest     회원가입 요청 정보를 담고 있는 {@link UserRequest.Signup} DTO
     * @param encodedPassword 암호화된 사용자 비밀번호
     */
    private User(UserRequest.Signup userRequest, String encodedPassword) {
        email = userRequest.email();
        nickname = userRequest.nickname();
        password = encodedPassword;
        userId = UUID.randomUUID().toString();
        userRole = UserRole.ROLE_FREE;
        isAccountNonLocked = true;
    }

    /**
     * OAuth2 로그인 시 사용될 User 엔티티를 생성하는 private 생성자입니다. OAuth2 사용자는 비밀번호가 중요하지 않으므로, 비밀번호 필드는
     * "{noop}" 프리픽스를 사용하여 Spring Security에 암호화되지 않았음을 명시합니다.
     *
     * @param email    OAuth2 공급자로부터 받은 사용자의 이메일 주소
     * @param nickname OAuth2 공급자로부터 받은 사용자의 닉네임 (또는 이름)
     */
    private User(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
        this.password = "{noop}"; // 소셜 로그인 사용자는 비밀번호가 중요하지 않으므로 {noop}을 사용하여 암호화되지 않은 비밀번호임을 명시
        this.userId = UUID.randomUUID().toString();
        this.userRole = UserRole.ROLE_FREE;
        this.isAccountNonLocked = true;
    }


    /**
     * 회원가입 요청을 기반으로 새로운 {@code User} 엔티티 인스턴스를 생성하는 정적 팩토리 메서드입니다.
     *
     * @param userRequest     회원가입 요청 정보를 담고 있는 {@link UserRequest.Signup} DTO
     * @param encodedPassword 암호화된 사용자 비밀번호
     * @return 새로 생성된 {@link User} 엔티티 인스턴스
     */
    public static User ofSignup(UserRequest.Signup userRequest, String encodedPassword) {
        return new User(userRequest, encodedPassword);
    }

    /**
     * OAuth2 로그인 시 새로운 {@code User} 엔티티 인스턴스를 생성하는 정적 팩토리 메서드입니다.
     *
     * @param email    OAuth2 공급자로부터 받은 사용자의 이메일 주소
     * @param nickname OAuth2 공급자로부터 받은 사용자의 닉네임 (또는 이름)
     * @return 새로 생성된 {@link User} 엔티티 인스턴스
     */
    public static User ofOAuth2(String email, String nickname) {
        return new User(email, nickname);
    }


    /**
     * 사용자 계정을 잠금 상태로 변경합니다. {@code isAccountNonLocked} 필드를 {@code false}로 설정합니다.
     */
    public void accountLock() {
        isAccountNonLocked = false;
    }

    /**
     * Spring Security의 {@link org.springframework.security.core.userdetails.UserDetails} 인터페이스의
     * {@code getAuthorities()} 메서드와 유사하게, 사용자의 권한(역할) 목록을 반환합니다. 이는 {@link PrincipalOAuth2User}와
     * {@link com.trendchat.userservice.security.PrincipalDetails} 클래스에서 사용됩니다.
     *
     * @return 사용자가 가진 권한 목록을 포함하는 {@link Collection}
     */
    public Collection<? extends GrantedAuthority> getAuthorities() {
        UserRole role = this.getUserRole();
        String authority = role.getAuthority();

        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(authority);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(simpleGrantedAuthority);

        return authorities;
    }

    public void updateNickname(String newNickname) {
        nickname = newNickname;
    }
}
