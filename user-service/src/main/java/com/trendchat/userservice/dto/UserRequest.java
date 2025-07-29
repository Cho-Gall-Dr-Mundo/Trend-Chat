package com.trendchat.userservice.dto;

import com.trendchat.userservice.dto.UserRequest.Login;
import com.trendchat.userservice.dto.UserRequest.Signup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * {@code UserRequest} 인터페이스는 사용자 관련 요청 데이터 전송 객체(DTO)를 정의하는 봉인된 인터페이스입니다.
 * <p>
 * 이 인터페이스는 현재 {@link Signup} (회원가입 요청) 및 {@link Login} (로그인 요청) 레코드만 구현을 허용하고 있으며, 이는 다양한 유형의 사용자
 * 요청을 명확하게 구분하고 관리하기 위한 확장 가능한 기반을 제공합니다.
 * </p>
 *
 * @see com.trendchat.userservice.dto.UserRequest.Signup
 * @see com.trendchat.userservice.dto.UserRequest.Login
 */
public sealed interface UserRequest permits Signup, Login {

    /**
     * {@code Signup} 레코드는 사용자 회원가입 요청 시 사용되는 DTO입니다.
     * <p>
     * 이 레코드는 {@link UserRequest} 인터페이스를 구현하며, 사용자 이메일, 닉네임, 비밀번호를 포함합니다. 각 필드에는 데이터 유효성 검사를 위한
     * Jakarta Validation(JSR 380) 어노테이션이 적용되어 있습니다.
     * </p>
     *
     * @param email    회원가입할 사용자의 이메일 주소. 이메일 형식이어야 하며, null이 아니어야 하고, 최소 2자 이상이어야 합니다.
     * @param nickname 회원가입할 사용자의 닉네임. null이 아니어야 하고, 최소 2자 이상이어야 합니다.
     * @param password 회원가입할 사용자의 비밀번호. null이 아니어야 하고, 8자 이상이어야 합니다.
     */
    record Signup(
            @Email
            @NotNull(message = "Email cannot be null")
            @Size(min = 2, message = "Email not be less than two characters")
            String email,

            @NotNull(message = "Nickname cannot be null")
            @Size(min = 2, message = "Nickname not be less than two characters")
            String nickname,

            @NotNull(message = "Password cannot be null")
            @Size(min = 8, message = "Password must equal or grater than 8 characters")
            String password
    ) implements UserRequest {

    }

    /**
     * {@code Login} 레코드는 사용자 로그인 요청 시 사용되는 DTO입니다.
     * <p>
     * 이 레코드는 {@link UserRequest} 인터페이스를 구현하며, 사용자 이메일과 비밀번호를 포함합니다. 각 필드에는 데이터 유효성 검사를 위한 Jakarta
     * Validation(JSR 380) 어노테이션이 적용되어 있습니다.
     * </p>
     *
     * @param email    로그인할 사용자의 이메일 주소. 이메일 형식이어야 하며, null이 아니어야 하고, 최소 2자 이상이어야 합니다.
     * @param password 로그인할 사용자의 비밀번호. null이 아니어야 하고, 8자 이상이어야 합니다.
     */
    record Login(
            @Email
            @NotNull(message = "Email cannot be null")
            @Size(min = 2, message = "Email not be less than two characters")
            String email,

            @NotNull(message = "Password cannot be null")
            @Size(min = 8, message = "Password must equal or grater than 8 characters")
            String password
    ) implements UserRequest {

    }
}
