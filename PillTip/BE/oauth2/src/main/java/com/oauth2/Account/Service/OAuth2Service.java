// author : mireutale
// description : OAuth2 서비스
package com.oauth2.Account.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.Account.Dto.OAuth2UserInfo;
import com.oauth2.Util.Exception.CustomException.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OAuth2Service {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2Service.class);
    private final RestTemplate restTemplate; // RestTemplate 객체 주입
    private final ObjectMapper objectMapper; // ObjectMapper 객체 주입

    // google 사용자 정보 엔드포인트
    @Value("${oauth2.google.userinfo-endpoint}")
    private String googleUserInfoEndpoint;

    // 카카오 사용자 정보 엔드포인트
    @Value("${oauth2.kakao.userinfo-endpoint}")
    private String kakaoUserInfoEndpoint;

    // 사용자 정보 조회
    public OAuth2UserInfo getUserInfo(String provider, String accessToken) {
        logger.info("OAuth2 사용자 정보 조회 시작 - Provider: {}, Token: {}", provider, accessToken != null ? accessToken.substring(0, Math.min(10, accessToken.length())) + "..." : "null");
        
        try {
            return switch (provider.toLowerCase()) {
                case "google" -> getGoogleUserInfo(accessToken);
                case "kakao" -> getKakaoUserInfo(accessToken);
                default -> {
                    logger.error("지원하지 않는 OAuth2 제공자: {}", provider);
                    throw new UnsupportedOauth2ProviderException();
                }
            };
        } catch (Exception e) {
            logger.error("OAuth2 사용자 정보 조회 실패 - Provider: {}, Error: {}", provider, e.getMessage(), e);
            throw e;
        }
    }

    // google 사용자 정보 조회
    private OAuth2UserInfo getGoogleUserInfo(String accessToken) {
        logger.info("Google 사용자 정보 조회 시작");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            googleUserInfoEndpoint,
            HttpMethod.GET,
            entity,
            String.class
        );

        logger.info("Google API 응답 상태: {}", response.getStatusCode());
        logger.debug("Google API 응답 본문: {}", response.getBody());

        try {
            JsonNode userInfo = objectMapper.readTree(response.getBody());
            
            // socialId는 필수값
            String socialId = userInfo.get("id").asText();
            if (socialId == null || socialId.isEmpty()) {
                logger.error("Google 사용자 ID가 없습니다. 응답: {}", response.getBody());
                throw new GoogleUserIdRequiredException();
            }

            logger.info("Google 사용자 정보 조회 성공 - SocialId: {}", socialId);
            
            return OAuth2UserInfo.builder()
                .socialId(socialId)
                .email(getNodeAsText(userInfo, "email"))           // 선택적
                .name(getNodeAsText(userInfo, "name"))            // 선택적
                .profileImage(getNodeAsText(userInfo, "picture")) // 선택적
                .build();
        } catch (Exception e) {
            logger.error("Google 사용자 정보 파싱 실패 - 응답: {}, 에러: {}", response.getBody(), e.getMessage(), e);
            throw new GoogleUserInfoParseFailedException();
        }
    }

    private OAuth2UserInfo getKakaoUserInfo(String accessToken) {
        logger.info("카카오 사용자 정보 조회 시작");
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            logger.info("카카오 API 요청 - URL: {}, Headers: {}", kakaoUserInfoEndpoint, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                kakaoUserInfoEndpoint,
                HttpMethod.GET,
                entity,
                String.class
            );

            logger.info("카카오 API 응답 상태: {}", response.getStatusCode());
            logger.info("카카오 API 응답 본문: {}", response.getBody());

            if (response.getStatusCode().is4xxClientError()) {
                logger.error("카카오 API 클라이언트 에러 - 상태: {}, 응답: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("카카오 API 클라이언트 에러: " + response.getStatusCode());
            }
            
            if (response.getStatusCode().is5xxServerError()) {
                logger.error("카카오 API 서버 에러 - 상태: {}, 응답: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("카카오 API 서버 에러: " + response.getStatusCode());
            }

            JsonNode userInfo = objectMapper.readTree(response.getBody());
            logger.info("카카오 응답 파싱 완료: {}", userInfo.toString());
            
            // socialId는 필수값
            JsonNode idNode = userInfo.get("id");
            if (idNode == null) {
                logger.error("카카오 응답에 id 필드가 없습니다. 응답: {}", response.getBody());
                throw new KakaoUserIdRequiredException();
            }
            
            String socialId = idNode.asText();
            if (socialId == null || socialId.isEmpty()) {
                logger.error("카카오 사용자 ID가 비어있습니다. 응답: {}", response.getBody());
                throw new KakaoUserIdRequiredException();
            }

            logger.info("카카오 사용자 ID 추출 성공: {}", socialId);

            JsonNode kakaoAccount = userInfo.get("kakao_account");
            if (kakaoAccount == null) {
                logger.warn("카카오 응답에 kakao_account 필드가 없습니다. 기본 정보만 사용합니다.");
            } else {
                logger.info("카카오 계정 정보: {}", kakaoAccount.toString());
            }
            
            JsonNode profile = kakaoAccount != null ? kakaoAccount.get("profile") : null;
            if (profile == null) {
                logger.warn("카카오 응답에 profile 필드가 없습니다. 기본 정보만 사용합니다.");
            } else {
                logger.info("카카오 프로필 정보: {}", profile.toString());
            }

            String email = getNodeAsText(kakaoAccount, "email");
            String name = profile != null ? getNodeAsText(profile, "nickname") : null;
            String profileImage = profile != null ? getNodeAsText(profile, "profile_image_url") : null;

            logger.info("카카오 사용자 정보 파싱 완료 - SocialId: {}, Email: {}, Name: {}, ProfileImage: {}", 
                       socialId, email, name, profileImage != null ? "있음" : "없음");

            return OAuth2UserInfo.builder()
                .socialId(socialId)
                .email(email)           // 선택적
                .name(name)  // 선택적
                .profileImage(profileImage) // 선택적
                .build();
        } catch (Exception e) {
            logger.error("카카오 사용자 정보 처리 실패 - 에러: {}", e.getMessage(), e);
            throw new KakaoUserInfoParseFailedException();
        }
    }

    // JsonNode에서 안전하게 값을 추출하는 헬퍼 메서드
    private String getNodeAsText(JsonNode node, String fieldName) {
        if (node == null) {
            logger.debug("JsonNode가 null입니다. 필드: {}", fieldName);
            return null;
        }
        JsonNode field = node.get(fieldName);
        if (field == null) {
            logger.debug("필드 {}가 null입니다.", fieldName);
            return null;
        }
        if (field.isNull()) {
            logger.debug("필드 {}가 null 값입니다.", fieldName);
            return null;
        }
        String value = field.asText();
        logger.debug("필드 {} 값: {}", fieldName, value);
        return value;
    }
}
