// author : mireutale
// description : 회원가입 요청 정보

package com.oauth2.Account.Dto;

import com.oauth2.Account.Entity.LoginType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    // User 엔티티의 필드와 매핑
    private LoginType loginType;
    private String loginId;
    private String password;
    private String nickname;
    private String token;      // OAuth2 access token
    private String provider;   // "google" 또는 "kakao"
    // UserProfile 엔티티의 필드와 매핑
    private String gender;
    private String birthDate;  // YYYY-MM-DD
    private Integer age;
    private Integer height;
    private Integer weight;
    private String phone;
    // Interests 엔티티의 필드와 매핑
    private String interest;   // 콤마로 구분된 관심사 문자열
}
