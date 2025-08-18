package com.trendchat.userservice.security;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

/**
 * {@code HttpCookieOAuth2AuthorizationRequestRepository}는 OAuth2 인증 요청을 세션 대신 HTTP 쿠키에 저장하고 검색하는
 * 역할을 합니다. 이는 Spring Security의 {@code AuthorizationRequestRepository} 인터페이스를 구현하여 STATELESS 환경에서
 * OAuth2 로그인을 가능하게 합니다.
 */
@Slf4j
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository implements
        AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private static final int cookieExpireSeconds = 180;

    /**
     * HTTP 요청에서 OAuth2 인증 요청을 로드합니다. 쿠키에서 인증 요청 정보를 찾아 Base64 디코딩 후 역직렬화합니다.
     *
     * @param request HTTP 요청 객체
     * @return 로드된 OAuth2AuthorizationRequest 객체 (존재하지 않으면 null)
     */
    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        OAuth2AuthorizationRequest authRequest = getAuthorizationRequestFromCookie(request);
        if (authRequest != null) {
            log.info("[OAuth2] Loading: {}", authRequest.getAttributes()); // <== 여기
        } else {
            log.info("[OAuth2] Loading: authorizationRequest is null");
        }
        return authRequest;
    }

    /**
     * OAuth2 인증 요청을 HTTP 응답 쿠키에 저장합니다. 요청 객체의 주요 정보를 Map으로 변환하여 Base64 인코딩 후 쿠키에 저장합니다.
     *
     * @param authorizationRequest 저장할 OAuth2AuthorizationRequest 객체
     * @param request              HTTP 요청 객체
     * @param response             HTTP 응답 객체
     */
    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 인증 요청이 null이면 관련 쿠키를 모두 제거
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }

        // OAuth2AuthorizationRequest 객체의 주요 필드를 Map<String, String>으로 변환하여 쿠키에 저장하기 용이하게 만듬
        Map<String, String> attributesMap = new HashMap<>();
        attributesMap.put("authorizationUri", authorizationRequest.getAuthorizationRequestUri());
        attributesMap.put("authorizationGrantType", authorizationRequest.getGrantType().getValue());
        attributesMap.put("redirectUri", authorizationRequest.getRedirectUri());
        attributesMap.put("clientId", authorizationRequest.getClientId());
        attributesMap.put("state", authorizationRequest.getState());
        attributesMap.put("registrationId",
                authorizationRequest.getAttribute("registrationId") != null
                        ? authorizationRequest.getAttribute("registrationId").toString()
                        : extractRegistrationIdFromRedirectUri(
                                authorizationRequest.getRedirectUri())
        );

        // Scope 정보가 있다면 저장
        if (authorizationRequest.getScopes() != null && !authorizationRequest.getScopes()
                .isEmpty()) {
            attributesMap.put("scopes", String.join(",", authorizationRequest.getScopes()));
        }
        // 추가 파라미터가 있다면 저장
        if (authorizationRequest.getAdditionalParameters() != null
                && !authorizationRequest.getAdditionalParameters().isEmpty()) {
            authorizationRequest.getAdditionalParameters().forEach((key, value) -> {
                attributesMap.put("additionalParameters." + key,
                        value != null ? value.toString() : "");
            });
        }

        // OAuth2 인증 요청 정보를 쿠키에 추가
        addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serialize(attributesMap));

        // 로그인 후 사용자가 리다이렉트될 원래 URI가 요청 파라미터로 제공되었다면, 이를 별도의 쿠키에 저장
        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
            addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin);
        }
    }

    /**
     * HTTP 요청에서 OAuth2 인증 요청을 제거하고 반환합니다. 이 메서드는 인증 요청 처리 후 쿠키를 정리하는 데 사용됩니다.
     *
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @return 제거된 OAuth2AuthorizationRequest 객체 (존재하지 않으면 null)
     */
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        OAuth2AuthorizationRequest authorizationRequest = getAuthorizationRequestFromCookie(
                request);
        removeAuthorizationRequestCookies(request, response); // 쿠키 삭제

        return authorizationRequest;
    }

    /**
     * HTTP 요청의 쿠키에서 OAuth2 인증 요청 정보를 가져옵니다.
     *
     * @param request HTTP 요청 객체
     * @return OAuth2AuthorizationRequest 객체 또는 null
     */
    private OAuth2AuthorizationRequest getAuthorizationRequestFromCookie(
            HttpServletRequest request) {
        String cookieValue = getCookieValue(request);
        if (StringUtils.isBlank(cookieValue)) {
            return null;
        }
        try {
            Map<String, String> attributes = deserialize(cookieValue);

            OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.authorizationCode()
                    .authorizationUri(Objects.requireNonNull(attributes.get("authorizationUri")))
                    .clientId(Objects.requireNonNull(attributes.get("clientId")))
                    .redirectUri(Objects.requireNonNull(attributes.get("redirectUri")))
                    .state(attributes.get("state"));

            if (StringUtils.isNotBlank(attributes.get("scopes"))) {
                String[] scopesArray = attributes.get("scopes").split(",");
                builder.scope(scopesArray);
            }

            Map<String, Object> additionalParameters = new HashMap<>();
            attributes.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("additionalParameters."))
                    .forEach(entry -> additionalParameters.put(
                            entry.getKey().substring("additionalParameters.".length()),
                            entry.getValue()));
            if (!additionalParameters.isEmpty()) {
                builder.additionalParameters(additionalParameters);
            }

            OAuth2AuthorizationRequest authorizationRequest = builder.build();

            Map<String, Object> attrCopy = new HashMap<>(attributes);
            if (!attrCopy.containsKey("registrationId") || StringUtils.isBlank(
                    (String) attrCopy.get("registrationId"))) {
                String redirectUri = (String) attrCopy.get("redirectUri");
                String regId = extractRegistrationIdFromRedirectUri(redirectUri);
                attrCopy.put("registrationId", regId);
            }
            builder.attributes(map -> map.putAll(attrCopy));

            return authorizationRequest;

        } catch (Exception e) {
            System.err.println("Failed to deserialize OAuth2AuthorizationRequest from cookie: "
                    + e.getMessage());
            return null;
        }
    }

    /**
     * OAuth2 인증 요청 관련 쿠키들을 HTTP 응답에서 삭제합니다.
     *
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     */
    public void removeAuthorizationRequestCookies(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }

    /**
     * HTTP 응답에 새 쿠키를 추가합니다.
     */
    private void addCookie(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(cookieExpireSeconds);
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "None");
        response.addCookie(cookie);
    }

    /**
     * HTTP 요청에서 특정 이름의 쿠키 값을 가져옵니다.
     */
    private String getCookieValue(HttpServletRequest request) {
        jakarta.servlet.http.Cookie cookie = WebUtils
                .getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        return (cookie != null) ? cookie.getValue() : null;
    }

    /**
     * HTTP 응답에서 특정 이름의 쿠키를 삭제합니다.
     */
    private void deleteCookie(HttpServletRequest request, HttpServletResponse response,
            String name) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue(""); // 쿠키 값 비움
                    cookie.setPath("/"); // 경로 일치
                    cookie.setMaxAge(0); // 만료 시간을 0으로 설정하여 즉시 삭제
                    response.addCookie(cookie);
                    return;
                }
            }
        }
    }

    // 간단한 Map<String, String>을 Base64 인코딩된 문자열로 변환
    private String serialize(Map<String, String> map) {
        String queryString = map.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
        return Base64.getUrlEncoder().encodeToString(queryString.getBytes());
    }

    private Map<String, String> deserialize(String encodedString) {
        Map<String, String> map = new HashMap<>();
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(encodedString));
            String[] pairs = decoded.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    map.put(pair.substring(0, idx), pair.substring(idx + 1));
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to decode or parse cookie string: " + e.getMessage());
        }
        return map;
    }

    private String extractRegistrationIdFromRedirectUri(String redirectUri) {
        if (redirectUri == null) {
            return "";
        }
        String[] parts = redirectUri.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : "";
    }
}