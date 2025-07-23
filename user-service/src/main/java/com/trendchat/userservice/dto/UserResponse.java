package com.trendchat.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trendchat.userservice.dto.UserResponse.Get;
import com.trendchat.userservice.entity.User;

public sealed interface UserResponse permits Get {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record Get(
            String email,
            String nickname,
            String userId
    ) implements UserResponse {

        public Get(User user) {
            this(
                    user.getEmail(),
                    user.getNickname(),
                    user.getUserId()
            );
        }
    }
}