package com.trendchat.userservice.dto;

import com.trendchat.userservice.dto.UserRequest.Signup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public sealed interface UserRequest permits Signup {

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
}
