package com.trendchat.userservice.dto;

import com.trendchat.userservice.dto.Token.Pair;

public sealed interface Token permits Pair {

    record Pair(
            String accessToken,
            String refreshToken
    ) implements Token {

    }
}
