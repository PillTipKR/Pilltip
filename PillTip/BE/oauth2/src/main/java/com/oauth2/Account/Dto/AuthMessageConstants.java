// author : mireutale
// description : 공통 에러 메시지 상수
package com.oauth2.Account.Dto;

public class AuthMessageConstants {
    
    // 인증 관련 에러 메시지
    public static final String USER_NOT_AUTHENTICATED = "로그인이 필요합니다.";
    public static final String TOKEN_NOT_PROVIDED = "토큰이 제공되지 않았습니다. 로그인이 필요합니다.";
    public static final String INVALID_TOKEN = "토큰이 올바르지 않습니다.";
    public static final String TOKEN_EXPIRED = "토큰이 만료되었습니다.";
    public static final String INVALID_REFRESH_TOKEN = "리프레시 토큰이 올바르지 않습니다.";
    public static final String AUTHENTICATION_FAILED = "인증에 실패했습니다. 다시 로그인해주세요.";
    
    // 알람 관련 에러 메시지
    public static final String DOSAGE_TIME_NOT_PASSED = "복용 시간이 아직 지나지 않았습니다.";
    public static final String ALREADY_TAKEN = "이미 복용한 약입니다.";
    
    // 일반 에러 메시지
    public static final String OPERATION_FAILED = "작업에 실패했습니다.";
    public static final String INVALID_REQUEST = "요청이 올바르지 않습니다.";
    public static final String SERIALIZATION_FAILED = "데이터 처리에 실패했습니다.";
    public static final String DESERIALIZATION_FAILED = "데이터 변환에 실패했습니다.";
    public static final String FILE_UPLOAD_FAILED = "파일 업로드에 실패했습니다.";
    public static final String FILE_SAVE_FAILED = "파일 저장에 실패했습니다.";
    public static final String DIRECTORY_CREATE_FAILED = "폴더 생성에 실패했습니다.";
    
    // 로그인/회원가입 관련 에러 메시지
    public static final String LOGIN_FAILED = "로그인에 실패했습니다.";
    public static final String SIGNUP_FAILED = "회원가입에 실패했습니다.";
    public static final String LOGOUT_FAILED = "로그아웃에 실패했습니다.";
    public static final String TOKEN_REFRESH_FAILED = "토큰 갱신에 실패했습니다.";
    public static final String LOGIN_TYPE_REQUIRED = "로그인 타입을 선택해주세요 (IDPW 또는 SOCIAL)";
    public static final String SOCIAL_ACCOUNT_ALREADY_EXISTS = "이미 가입된 소셜 계정입니다.";
    public static final String TERMS_AGREEMENT_FAILED = "이용약관 동의에 실패했습니다.";
    
    // 검색 관련 에러 메시지
    public static final String SEARCH_FAILED = "검색에 실패했습니다.";
    
    // OAuth2 관련 에러 메시지
    public static final String UNSUPPORTED_OAUTH2_PROVIDER = "지원하지 않는 소셜 로그인입니다.";
    public static final String OAUTH2_USER_ID_REQUIRED = "소셜 로그인 ID가 필요합니다.";
    public static final String OAUTH2_USER_INFO_PARSE_FAILED = "소셜 로그인 정보 처리에 실패했습니다.";
    public static final String GOOGLE_USER_ID_REQUIRED = "Google ID가 필요합니다.";
    public static final String KAKAO_USER_ID_REQUIRED = "카카오 ID가 필요합니다.";
    public static final String GOOGLE_USER_INFO_PARSE_FAILED = "Google 정보 처리에 실패했습니다.";
    public static final String KAKAO_USER_INFO_PARSE_FAILED = "카카오 정보 처리에 실패했습니다.";
    
    // 토큰 관련 에러 메시지
    public static final String INVALID_REFRESH_TOKEN_DETAIL = "리프레시 토큰이 올바르지 않습니다.";
    public static final String TOKEN_NOT_FOUND = "토큰을 찾을 수 없습니다.";
    public static final String TOKEN_EXPIRED_DETAIL = "토큰이 만료되었습니다.";
    public static final String TOKEN_INVALID_RETRY_LOGIN = "토큰이 올바르지 않습니다. 다시 로그인해주세요.";
    public static final String TOKEN_EXPIRED_RETRY_LOGIN = "토큰이 만료되었습니다. 다시 로그인해주세요.";
    public static final String USER_INFO_NOT_FOUND_RETRY_LOGIN = "사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요.";
    
    // 회원가입 관련 에러 메시지
    public static final String LOGIN_TYPE_REQUIRED_DETAIL = "로그인 타입을 선택해주세요 (IDPW 또는 SOCIAL)";
    public static final String USER_ID_PASSWORD_REQUIRED = "아이디와 비밀번호를 입력해주세요.";
    public static final String TOKEN_REQUIRED_FOR_SOCIAL = "소셜 로그인 토큰이 필요합니다.";
    public static final String SOCIAL_ACCOUNT_ALREADY_EXISTS_DETAIL = "이미 가입된 소셜 계정입니다.";
    public static final String INVALID_PHONE_NUMBER_FORMAT = "전화번호 형식이 올바르지 않습니다.";
    
    // 에러 메시지 키워드
    public static final String ERROR_KEYWORD_LOGIN_TYPE = "LoginType";
    public static final String ERROR_KEYWORD_PHONE_NUMBER = "전화번호";
    public static final String ERROR_KEYWORD_NICKNAME = "Nickname";
    public static final String ERROR_KEYWORD_USER_ID = "User ID";
    
    // 약물 관련 에러 메시지
    public static final String DRUG_NOT_FOUND = "약물을 찾을 수 없습니다.";
    public static final String DRUG_INTERACTION_NOT_FOUND = "약물 상호작용을 찾을 수 없습니다.";
    public static final String UNSUPPORTED_DRUG_TYPE = "지원하지 않는 약물 유형입니다.";
    
    // 리뷰 관련 에러 메시지
    public static final String REVIEW_DELETE_PERMISSION_DENIED = "삭제 권한이 없습니다.";
    public static final String UNKNOWN_TAG_TYPE = "알 수 없는 태그입니다.";
    
    // Elasticsearch 관련 에러 메시지
    public static final String ELASTICSEARCH_OPERATION_FAILED = "검색 작업에 실패했습니다.";
    
    // JSON 관련 에러 메시지
    public static final String JSON_CONVERSION_FAILED = "데이터 변환에 실패했습니다.";
    public static final String JSON_PARSE_FAILED = "데이터 처리에 실패했습니다.";
    
    // 기타
    public static final String UNEXPECTED_ERROR = "예상치 못한 오류가 발생했습니다.";
    public static final String VALIDATION_FAILED = "입력값이 올바르지 않습니다.";
    
    // 성공 메시지 상수
    public static final String LOGIN_SUCCESS = "로그인되었습니다.";
    public static final String SOCIAL_LOGIN_SUCCESS = "소셜 로그인되었습니다.";
    public static final String SIGNUP_SUCCESS = "회원가입되었습니다.";
    public static final String LOGOUT_SUCCESS = "로그아웃되었습니다.";
    public static final String TOKEN_REFRESH_SUCCESS = "토큰이 갱신되었습니다.";
    public static final String TERMS_AGREEMENT_SUCCESS = "이용약관에 동의했습니다.";
    public static final String DUPLICATE_CHECK_SUCCESS = "사용 가능한 %s입니다.";
    public static final String DUPLICATE_CHECK_FAILED = "이미 사용 중인 %s입니다.";
    public static final String DUPLICATE_PHONE_FORMAT = "전화번호가 중복인 것 같습니다. 다른 전화번호를 입력해주세요.";
    public static final String DUPLICATE_NICKNAME_FORMAT = "이미 사용 중인 닉네임입니다.";
    public static final String DUPLICATE_LOGIN_ID = "이미 사용 중인 아이디입니다.";
    public static final String INVALID_CHECK_TYPE = "유효하지 않은 체크 타입입니다.";
} 