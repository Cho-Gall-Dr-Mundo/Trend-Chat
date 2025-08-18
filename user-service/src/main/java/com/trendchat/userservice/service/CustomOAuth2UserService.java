package com.trendchat.userservice.service;

import com.trendchat.userservice.entity.User;
import com.trendchat.userservice.repository.UserRepository;
import com.trendchat.userservice.security.PrincipalOAuth2User;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code CustomOAuth2UserService}는 OAuth2 공급자(예: Google)로부터 사용자 정보를 로드하고 이를 우리 서비스의 {@link User}
 * 엔티티와 연동하는 역할을 합니다. DefaultOAuth2UserService를 확장하여 기본 로직을 사용하고, 추가적인 사용자 처리 로직을 구현합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * OAuth2 공급자로부터 사용자 정보를 로드합니다.
     * <p>
     * 1. {@link DefaultOAuth2UserService}를 통해 기본 OAuth2 사용자 정보를 가져옵니다.<br> 2. 소셜에서 받은 email로 이미 가입된
     * 사용자가 있는지 확인합니다.<br> 3. 등록된 사용자가 있으면, <b>기존 사용자 정보(닉네임 등)를 유지</b>하며 로그인 처리만 진행합니다.<br> 4. 등록된
     * 사용자가 없으면, 신규 사용자를 생성(닉네임, email 등) 후 저장합니다.<br> 5. 최종적으로 {@link PrincipalOAuth2User} 객체를 반환하여
     * Spring Security에 사용자 정보를 전달합니다.
     * </p>
     *
     * <p>
     * <b>NOTE:</b> 기존 사용자가 소셜로 로그인할 경우, 소셜 프로필 닉네임 등은 <b>덮어쓰지 않고</b> 기존 DB의 정보가 유지됩니다.
     * </p>
     *
     * @param userRequest OAuth2 사용자 정보를 요청하기 위한 정보
     * @return 로드된 OAuth2 사용자 정보와 우리 서비스의 User 엔티티가 포함된 {@link PrincipalOAuth2User}
     * @throws OAuth2AuthenticationException OAuth2 인증 실패 시 발생하는 예외
     *
     *                                       <p>
     *                                       <b>TODO (권장 표준 방식):</b>
     *                                       <ul>
     *                                         <li>1. 최초 로그인 시 소셜 프로바이더의 unique id(sub, id 등)를 별도의 테이블에 저장(소셜 연동 이력 관리).</li>
     *                                         <li>2. 이미 가입된 이메일과 소셜 로그인이 충돌할 경우, 추가 인증/병합 절차(예: 이메일 인증, 사용자 선택 병합 등) 구현.</li>
     *                                         <li>3. 소셜에서 받은 닉네임, 프로필 이미지 등은 최초 회원가입시만 반영. 이후에는 사용자가 직접 프로필을 변경/관리하도록 안내.</li>
     *                                         <li>4. "회원탈퇴 → 같은 이메일 소셜 가입" 시 중복 가입 방지, 휴면 처리 등 예외 케이스 핸들링.</li>
     *                                         <li>5. 유저가 여러 소셜 계정과 연결(연동/연결 해제)할 수 있도록 관리 UI/기능 추가 고려.</li>
     *                                       </ul>
     *                                       </p>
     */

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("OAuth2User attributes from {}: {}",
                userRequest.getClientRegistration().getRegistrationId(),
                oAuth2User.getAttributes());

        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = null;
        String nickname = null;

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if ("kakao".equals(registrationId)) {
            Object kakaoAccountObj = attributes.get("kakao_account");
            Map<String, Object> kakaoAccount;

            if (kakaoAccountObj instanceof Map) {
                // 안전하게 캐스팅
                kakaoAccount = (Map<String, Object>) kakaoAccountObj;

                email = (String) kakaoAccount.get("email");

                Object profileObj = kakaoAccount.get("profile");
                if (profileObj instanceof Map) {
                    Map<String, Object> profile = (Map<String, Object>) profileObj;
                    nickname = (String) profile.get("nickname");
                }
            } else {
                log.warn("kakao_account is missing or not a map");
            }
        } else if ("google".equals(registrationId)) {
            // 구글은 최상위 레벨에서 바로 email, name 추출 가능
            email = (String) attributes.get("email");
            nickname = (String) attributes.get("name");
        } else if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            if (response == null) {
                throw new OAuth2AuthenticationException("No response data from Naver");
            }

            email = (String) response.get("email");
            nickname = (String) response.get("nickname");

            if (email == null || email.isEmpty()) {
                throw new OAuth2AuthenticationException("Email not found from Naver");
            }
            if (nickname == null || nickname.isEmpty()) {
                nickname = "NaverUser"; // 기본 닉네임 설정 가능
            }
        }

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            log.info("Existing user ({}:{}) logged in via OAuth2.", user.getUserId(), email);
        } else {
            log.info("Registering new OAuth2 user ({}:{}).", email, nickname);
            user = User.ofOAuth2(email, nickname);
            user = userRepository.save(user);
        }

        return new PrincipalOAuth2User(user, attributes);
    }
}