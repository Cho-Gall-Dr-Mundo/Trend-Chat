package com.trendchat.userservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendchat.trendchatcommon.util.JwtUtil;
import com.trendchat.userservice.dto.UserRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException {
        try {
            UserRequest.Login loginRequest = new ObjectMapper()
                    .readValue(request.getInputStream(), UserRequest.Login.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult
    ) {
        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();
        String userId = principalDetails.getUser().getUserId();
        String nickname = principalDetails.getUser().getNickname();
        String userRole = principalDetails.getUser().getUserRole().getAuthority();

        String accessToken = jwtUtil.createAccessToken(userId, nickname, userRole);
        String refreshToken = jwtUtil.createRefreshToken(userId);

        jwtUtil.addAccessTokenToHeader(response, accessToken);
        jwtUtil.addRefreshTokenToCookie(response, refreshToken);
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed
    ) throws IOException, ServletException {
        super.unsuccessfulAuthentication(request, response, failed);
        response.setStatus(401);
    }
}
