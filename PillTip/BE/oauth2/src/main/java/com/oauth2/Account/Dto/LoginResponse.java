// author : mireutale
// description : 로그인 return 정보
package com.oauth2.Account.Dto;

public record LoginResponse (
    String accessToken,
    String refreshToken
){}
