// author : mireutale
// description : 커스텀 OAuth2 사용자 서비스
package com.oauth2.Account.Service;

import com.oauth2.Account.Dto.CreateUserDto;
import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Entity.AccountToken;
import com.oauth2.Account.Entity.LoginType;
import com.oauth2.Account.Repository.AccountRepository;
import com.oauth2.User.UserInfo.Entity.*;
import com.oauth2.User.UserInfo.Repository.UserRepository;
import com.oauth2.User.UserInfo.Entity.UserPermissions;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import com.oauth2.Account.Dto.AuthMessageConstants;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TokenService tokenService;

    // Spring Security에서 제공하는 DefaultOAuth2UserService를 상속받아 커스텀 OAuth2 사용자 서비스 구현
    // Spring Security에서 로그인을 처리한 뒤, 사용자 정보를 DB에 저장
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        // 소셜 ID 추출
        String socialId = getSocialId(registrationId, attributes);

        // 기존 사용자 확인
        Account account = accountRepository.findBySocialId(socialId).orElse(null);
        if (account == null) {
            createNewUser(registrationId, attributes);
        }

        return oauth2User;
    }

    private String getSocialId(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> (String) attributes.get("sub");
            case "kakao" -> String.valueOf(attributes.get("id"));
            default ->
                    throw new OAuth2AuthenticationException(AuthMessageConstants.UNSUPPORTED_OAUTH2_PROVIDER + ": " + registrationId);
        };
    }

    private User createNewUser(String registrationId, Map<String, Object> attributes) {
        // 기본 닉네임 생성
        String defaultNickname = "User" + System.currentTimeMillis();
    
        // 소셜 닉네임 추출
        String socialNickname = null;
        if (registrationId.equalsIgnoreCase("google")) {
            socialNickname = (String) attributes.get("name");
        } else if (registrationId.equalsIgnoreCase("kakao")) {
            Object kakaoAccountObj = attributes.get("kakao_account");
            if (kakaoAccountObj instanceof Map) { // kakao_account 객체가 Map인지 확인
                Map<?, ?> kakaoAccountMap = (Map<?, ?>) kakaoAccountObj;
                Object profileObj = kakaoAccountMap.get("profile");
                if (profileObj instanceof Map) { // profile 객체가 Map인지 확인
                    Map<?, ?> profileMap = (Map<?, ?>) profileObj;
                    Object nicknameObj = profileMap.get("nickname");
                    if (nicknameObj instanceof String) { // nickname 객체가 String인지 확인
                        socialNickname = (String) nicknameObj;
                    }
                }
            }
        }
    
        String nickname = (socialNickname != null && !socialNickname.isEmpty()) ? socialNickname : defaultNickname;

        CreateUserDto createUserDto = new CreateUserDto(
          Account.builder()
                  .loginType(LoginType.SOCIAL)  // 모든 소셜 로그인은 LoginType.SOCIAL 사용
                  .socialId(getSocialId(registrationId, attributes))
                  .userEmail((String) attributes.get("email"))
                  .build(),
          User.builder()
                .nickname(nickname)
                .profilePhoto((String) attributes.get("picture"))
                .terms(true)
                .build()
        );

        Account account = createUserDto.account();
        User user = createUserDto.user();

        account = accountRepository.save(account);
        user = userRepository.save(user);

        // UserProfile 생성
        UserProfile userProfile = UserProfile.builder()
                .user(user)
                .age(0)
                .gender(Gender.MALE)  // 기본값으로 MALE 설정
                .birthDate(LocalDate.now())  // 기본값으로 현재 날짜 설정
                .height(new BigDecimal("0"))
                .weight(new BigDecimal("0"))
                .phone("")
                .build();

        // Interests 생성
        Interests interests = Interests.builder()
                .user(user)
                .diet(false)
                .health(false)
                .muscle(false)
                .aging(false)
                .nutrient(false)
                .build();

        // UserPermissions 생성
        UserPermissions userPermissions = UserPermissions.builder()
                .user(user)
                .locationPermission(false)
                .cameraPermission(false)
                .galleryPermission(false)
                .phonePermission(false)
                .smsPermission(false)
                .filePermission(false)
                .sensitiveInfoPermission(false)
                .medicalInfoPermission(false)
                .build();

        // UserLocation 생성
        UserLocation userLocation = UserLocation.builder()
                .user(user)
                .latitude(new BigDecimal("0.0"))
                .longitude(new BigDecimal("0.0"))
                .build();

        // UserToken 생성
        AccountToken accountToken = tokenService.generateTokens(account.getId());

        // 연관 관계 설정
        account.setAccountToken(accountToken);

        user.setUserProfile(userProfile);
        user.setInterests(interests);
        user.setUserPermissions(userPermissions);
        user.setUserLocation(userLocation);

        accountRepository.save(account);
        return userRepository.save(user);
    }
}
