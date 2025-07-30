package com.trendchat.userservice.security;

import com.trendchat.userservice.entity.User;
import java.util.Collection;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * {@code PrincipalOAuth2User}는 Spring Security OAuth2 인증 시 사용자의 정보를 담는 클래스입니다. OAuth2User 인터페이스를
 * 구현하며, 내부적으로 우리 서비스의 {@link User} 엔티티를 포함합니다.
 */
@Getter
public class PrincipalOAuth2User implements OAuth2User {

    private final User user;
    private final Map<String, Object> attributes; // OAuth2 공급자로부터 받은 사용자 속성

    /**
     * 새로운 {@code PrincipalOAuth2User} 인스턴스를 생성합니다.
     *
     * @param user       우리 서비스의 {@link User} 엔티티
     * @param attributes OAuth2 공급자로부터 받은 사용자 속성 맵
     */
    public PrincipalOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    /**
     * 사용자의 권한(역할) 목록을 반환합니다. 우리 서비스의 {@link User} 엔티티에 정의된 권한을 사용합니다.
     *
     * @return 사용자가 가진 권한 목록을 포함하는 {@link Collection}
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities();
    }

    /**
     * OAuth2 공급자로부터 받은 사용자 속성 맵을 반환합니다.
     *
     * @return 사용자 속성 맵
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * OAuth2 공급자의 사용자 이름(식별자)을 반환합니다. 일반적으로 Google의 경우 'sub' 속성을 고유 식별자로 사용합니다.
     *
     * @return 사용자의 이름 또는 식별자
     */
    @Override
    public String getName() {
        // User 엔티티에 유니크한 이메일이 있으면 반환
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            return user.getEmail();
        }

        // 이메일이 없으면 attributes에서 공급자별 고유 ID 조회
        Object sub = attributes.get("sub"); // 구글용
        if (sub != null) {
            return sub.toString();
        }

        // 그 외 fallback 처리 (적절한 값 없으면 기본값 반환)
        return "unknown";
    }
}