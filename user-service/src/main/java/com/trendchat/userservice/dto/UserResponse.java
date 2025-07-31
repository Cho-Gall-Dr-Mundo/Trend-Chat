package com.trendchat.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trendchat.userservice.dto.UserResponse.Get;
import com.trendchat.userservice.entity.User;
import java.time.LocalDateTime;

/**
 * {@code UserResponse} 인터페이스는 사용자 관련 응답 데이터 전송 객체(DTO)를 정의하는 봉인된 인터페이스입니다.
 * <p>
 * 이 인터페이스는 현재 {@link Get} 레코드만 구현을 허용하고 있으며, 이는 다양한 유형의 사용자 응답을 명확하게 구분하고 관리하기 위한 확장 가능한 기반을
 * 제공합니다.
 * </p>
 *
 * @see com.trendchat.userservice.dto.UserResponse.Get
 */
public sealed interface UserResponse permits Get {

    /**
     * {@code Get} 레코드는 사용자 정보를 조회하여 클라이언트에 반환하는 데 사용되는 응답 DTO입니다.
     * <p>
     * 이 레코드는 {@link UserResponse} 인터페이스를 구현하며, 사용자 이메일, 닉네임, 고유 ID, 권한, 가입일을 포함합니다.
     * {@code @JsonInclude(JsonInclude.Include.NON_NULL)} 어노테이션은 이 레코드를 JSON으로 직렬화할 때 값이
     * {@code null}인 필드는 포함하지 않도록 합니다.
     * </p>
     *
     * @param email     사용자의 이메일 주소
     * @param nickname  사용자의 닉네임
     * @param userId    서비스에서 사용하는 사용자의 고유 ID
     * @param userRole  사용자의 권한 (예: "ROLE_FREE" 등)
     * @param createdAt 사용자의 가입일 (예: "2025-07-31T10:30:00")
     * @see com.trendchat.userservice.entity.User
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record Get(
            String email,
            String nickname,
            String userId,
            String userRole,
            LocalDateTime createdAt,
            Boolean isSocial
    ) implements UserResponse {

        /**
         * {@link User} 엔티티를 사용하여 {@code Get} 응답 DTO를 생성하는 생성자입니다. 이 생성자는 {@link User} 엔티티의 이메일,
         * 닉네임, 사용자 ID, 권한, 가입일을 추출하여 레코드 필드를 초기화합니다.
         *
         * @param user 응답 DTO로 변환할 {@link User} 엔티티
         */
        public Get(User user, boolean isSocial) {
            this(
                    user.getEmail(),
                    user.getNickname(),
                    user.getUserId(),
                    user.getUserRole().getAuthority(),
                    user.getCreatedAt(),
                    isSocial
            );
        }
    }
}