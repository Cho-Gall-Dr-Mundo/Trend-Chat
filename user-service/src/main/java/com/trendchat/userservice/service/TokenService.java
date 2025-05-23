package com.trendchat.userservice.service;

import com.trendchat.userservice.dto.Token;
import com.trendchat.userservice.entity.RefreshToken;

public interface TokenService {

    void saveRefreshToken(RefreshToken refreshToken);

    Token.Pair refreshTokens(Token.Pair tokenPair);
}
