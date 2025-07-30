package com.trendchat.userservice.security;

import com.trendchat.userservice.entity.User;
import java.util.Collection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * {@code PrincipalDetails}는 Spring Security에서 사용자 인증 및 권한 부여를 위해 {@link UserDetails} 인터페이스를 구현한
 * 클래스입니다.
 * <p>
 * 이 클래스는 애플리케이션의 {@link User} 엔티티 정보를 Spring Security 컨텍스트에 맞게 변환하여 제공합니다. Lombok의 {@code @Getter}와
 * {@code @RequiredArgsConstructor}를 사용하여 보일러플레이트 코드를 줄였습니다.
 * </p>
 *
 * @see org.springframework.security.core.userdetails.UserDetails
 * @see com.trendchat.userservice.entity.User
 * @see com.trendchat.trendchatcommon.enums.UserRole
 */
@Getter
@RequiredArgsConstructor
public class PrincipalDetails implements UserDetails {

    private final User user;

    /**
     * 사용자의 권한(역할) 목록을 반환합니다. 이 메서드는 {@link User} 엔티티의 {@code userRole}을 기반으로
     * {@link SimpleGrantedAuthority} 객체를 생성하여 반환합니다.
     *
     * @return 사용자가 가진 권한 목록을 포함하는 {@link Collection}
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities();
    }

    /**
     * 사용자의 비밀번호를 반환합니다.
     *
     * @return 사용자의 암호화된 비밀번호 문자열
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 사용자의 아이디(이메일)를 반환합니다. Spring Security에서 사용자를 식별하는 데 사용됩니다.
     *
     * @return 사용자의 이메일 주소 문자열
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * 계정 만료 여부를 나타냅니다. 현재 구현에서는 항상 {@code true}를 반환하여 계정이 만료되지 않았음을 나타냅니다.
     *
     * @return 계정이 만료되지 않았다면 {@code true}
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠금 여부를 나타냅니다. {@link User} 엔티티의 {@code isAccountNonLocked} 필드 값을 반환합니다.
     *
     * @return 계정이 잠겨 있지 않다면 {@code true}
     */
    @Override
    public boolean isAccountNonLocked() {
        return user.getIsAccountNonLocked();
    }

    /**
     * 자격 증명(비밀번호) 만료 여부를 나타냅니다. 현재 구현에서는 항상 {@code true}를 반환하여 자격 증명이 만료되지 않았음을 나타냅니다.
     *
     * @return 자격 증명이 만료되지 않았다면 {@code true}
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 사용자 계정의 활성화 여부를 나타냅니다. 현재 구현에서는 항상 {@code true}를 반환하여 계정이 활성화되었음을 나타냅니다.
     *
     * @return 계정이 활성화되었다면 {@code true}
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}