// author : mireutale
// description : 중복 체크 요청 정보
package com.oauth2.Account.Dto;

public record DuplicateCheckRequest (
    String value, // 체크할 값 (아이디, 닉네임, 전화번호)
    String type // 체크 타입 (loginId, nickname, phoneNumber)
){}
