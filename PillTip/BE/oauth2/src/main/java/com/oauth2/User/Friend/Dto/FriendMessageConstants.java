// author : mireutale
// description : 친구 관련 메시지 상수
package com.oauth2.User.Friend.Dto;

public class FriendMessageConstants {
    
    // 친구 관련 성공 메시지
    public static final String FRIEND_ADD_SUCCESS = "친구 추가가 완료되었습니다.";
    
    // 친구 관련 에러 메시지
    public static final String INVITE_TOKEN_REQUIRED = "초대 토큰을 입력해주세요.";
    public static final String CANNOT_ADD_SELF_AS_FRIEND = "자기 자신은 친구로 추가할 수 없습니다.";
    public static final String NOT_FRIEND = "서로 친구가 아닙니다.";
    public static final String FRIEND_ADD_FAILED = "친구 추가에 실패했습니다.";
    
    // 친구 관련 검증 메시지
    public static final String FRIEND_ALREADY_EXISTS = "이미 친구입니다.";
} 