// author : mireutale
// description : 소셜 로그인 요청 정보

package com.oauth2.Account.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequest {
    private String token;      // OAuth2 access token
    private String provider;   // "google" 또는 "kakao"
} 