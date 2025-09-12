// author : mireutale
// description : 로그인 요청 정보

package com.oauth2.Account.Dto;

public record LoginRequest (
    String loginId,
    String password
){}
