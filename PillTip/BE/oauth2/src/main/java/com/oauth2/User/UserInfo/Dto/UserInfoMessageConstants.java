// author : mireutale
// description : 사용자 정보 관련 메시지 상수
package com.oauth2.User.UserInfo.Dto;

public class UserInfoMessageConstants {
    
    // 사용자 정보 관련 성공 메시지
    public static final String PERSONAL_INFO_UPDATE_SUCCESS = "개인정보가 수정되었습니다.";
    public static final String NICKNAME_UPDATE_SUCCESS = "닉네임이 수정되었습니다.";
    public static final String PROFILE_PHOTO_UPDATE_SUCCESS = "프로필 사진이 수정되었습니다.";
    public static final String ACCOUNT_DELETE_SUCCESS = "계정이 삭제되었습니다.";
    public static final String PROFILE_UPDATE_SUCCESS = "프로필이 수정되었습니다.";
    public static final String SENSITIVE_INFO_RETRIEVE_SUCCESS = "민감정보를 조회했습니다.";
    public static final String SENSITIVE_INFO_UPDATE_SUCCESS = "민감정보가 수정되었습니다.";
    public static final String SENSITIVE_INFO_CATEGORY_UPDATE_SUCCESS = "민감정보 카테고리가 수정되었습니다.";
    public static final String SENSITIVE_INFO_DELETE_SUCCESS = "민감정보가 삭제되었습니다.";
    public static final String SENSITIVE_INFO_ALL_DELETE_SUCCESS = "모든 민감정보가 삭제되었습니다.";
    
    // 사용자 정보 관련 에러 메시지
    public static final String USER_NOT_AUTHENTICATED = "로그인이 필요합니다.";
    public static final String GET_CURRENT_USER_FAILED = "사용자 정보 조회에 실패했습니다.";
    public static final String PERSONAL_INFO_UPDATE_FAILED = "개인정보 수정에 실패했습니다.";
    public static final String NICKNAME_EMPTY = "닉네임을 입력해주세요.";
    public static final String NICKNAME_UPDATE_FAILED = "닉네임 수정에 실패했습니다.";
    public static final String PROFILE_PHOTO_UPDATE_FAILED = "프로필 사진 수정에 실패했습니다.";
    public static final String PROFILE_PHOTO_DIR_CREATE_FAILED = "프로필 사진 저장 폴더 생성에 실패했습니다.";
    public static final String ACCOUNT_DELETE_FAILED = "계정 삭제에 실패했습니다.";
    public static final String PROFILE_UPDATE_FAILED = "프로필 수정에 실패했습니다.";
    public static final String SENSITIVE_INFO_NOT_FOUND = "민감정보가 없습니다.";
    public static final String SENSITIVE_INFO_RETRIEVE_FAILED = "민감정보 조회에 실패했습니다.";
    public static final String SENSITIVE_INFO_UPDATE_FAILED = "민감정보 수정에 실패했습니다.";
    public static final String SENSITIVE_INFO_CATEGORY_UPDATE_FAILED = "민감정보 카테고리 수정에 실패했습니다.";
    public static final String SENSITIVE_INFO_DELETE_FAILED = "민감정보 삭제에 실패했습니다.";
    public static final String SENSITIVE_INFO_ALL_DELETE_FAILED = "민감정보 전체 삭제에 실패했습니다.";
    public static final String INVALID_CATEGORY = "카테고리가 올바르지 않습니다.";
    public static final String SENSITIVE_INFO_SERIALIZE_FAILED = "민감정보 처리에 실패했습니다.";
} 