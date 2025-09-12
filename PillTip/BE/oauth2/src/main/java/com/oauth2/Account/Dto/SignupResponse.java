// author : mireutale
// description : 회원가입 return 정보
package com.oauth2.Account.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {
    private String accessToken;
    private String refreshToken;
}
