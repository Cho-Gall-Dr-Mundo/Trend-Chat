package com.trendchat.userservice.dto;

import com.trendchat.userservice.dto.Token.Pair;

/**
 * {@code Token} 인터페이스는 토큰 관련 데이터 전송 객체(DTO)를 정의하는 봉인된 인터페이스입니다.
 * <p>
 * 이 인터페이스는 현재 {@link Pair} 레코드만 구현을 허용하고 있으며, 이는 다양한 유형의 토큰 관련 데이터를 명확하게 구분하고 관리하기 위한 확장 가능한 기반을
 * 제공합니다.
 * </p>
 *
 * @see com.trendchat.userservice.dto.Token.Pair
 */
public sealed interface Token permits Pair {

    /**
     * {@code Pair} 레코드는 액세스 토큰과 리프레시 토큰 한 쌍을 담는 DTO입니다.
     * <p>
     * 이 레코드는 {@link Token} 인터페이스를 구현하며, JWT 기반 인증 시스템에서 사용되는 두 가지 주요 토큰을 구조화하여 전달하는 데 사용됩니다.
     * </p>
     *
     * @param accessToken  클라이언트가 리소스에 접근할 때 사용하는 짧은 유효 기간의 토큰
     * @param refreshToken 액세스 토큰이 만료되었을 때 새로운 액세스 토큰을 발급받는 데 사용되는 긴 유효 기간의 토큰
     */
    record Pair(
            String accessToken,
            String refreshToken
    ) implements Token {

    }
}
