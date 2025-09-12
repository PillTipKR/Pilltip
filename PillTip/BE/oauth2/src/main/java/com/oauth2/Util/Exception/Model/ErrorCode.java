package com.oauth2.Util.Exception.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C002", "입력값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C003", "허용되지 않는 요청입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "데이터 타입이 올바르지 않습니다."),
    BAD_CREDENTIALS(HttpStatus.BAD_REQUEST, "C005", "로그인 정보가 올바르지 않습니다."),

    // 인증 관련
    USER_NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED, "E001", "로그인이 필요합니다."),
    TOKEN_NOT_PROVIDED(HttpStatus.UNAUTHORIZED, "E002", "토큰이 제공되지 않았습니다. 로그인이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "E003", "토큰이 올바르지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "E004", "토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "E005", "리프레시 토큰이 올바르지 않습니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "E006", "인증에 실패했습니다. 다시 로그인해주세요."),

    // 알람 관련
    DOSAGE_TIME_NOT_PASSED(HttpStatus.BAD_REQUEST, "E007", "복용 시간이 아직 지나지 않았습니다."),
    ALREADY_TAKEN(HttpStatus.BAD_REQUEST, "E008", "이미 복용한 약입니다."),

    // 공통 에러
    OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E009", "작업에 실패했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "E010", "요청이 올바르지 않습니다."),
    SERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E011", "데이터 처리에 실패했습니다."),
    DESERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E012", "데이터 변환에 실패했습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E013", "파일 업로드에 실패했습니다."),
    FILE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E014", "파일 저장에 실패했습니다."),
    DIRECTORY_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E015", "폴더 생성에 실패했습니다."),

    // 로그인 / 회원가입
    LOGIN_FAILED(HttpStatus.BAD_REQUEST, "E016", "로그인에 실패했습니다."),
    SIGNUP_FAILED(HttpStatus.BAD_REQUEST, "E017", "회원가입에 실패했습니다."),
    LOGOUT_FAILED(HttpStatus.BAD_REQUEST, "E018", "로그아웃에 실패했습니다."),
    TOKEN_REFRESH_FAILED(HttpStatus.BAD_REQUEST, "E019", "토큰 갱신에 실패했습니다."),
    LOGIN_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "E020", "로그인 타입을 선택해주세요 (IDPW 또는 SOCIAL)"),
    SOCIAL_ACCOUNT_ALREADY_EXISTS(HttpStatus.CONFLICT, "E021", "이미 가입된 소셜 계정입니다."),
    TERMS_AGREEMENT_FAILED(HttpStatus.BAD_REQUEST, "E022", "이용약관 동의에 실패했습니다."),

    // 검색
    SEARCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E023", "검색에 실패했습니다."),

    // OAuth2
    UNSUPPORTED_OAUTH2_PROVIDER(HttpStatus.BAD_REQUEST, "E024", "지원하지 않는 소셜 로그인입니다."),
    OAUTH2_USER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "E025", "소셜 로그인 ID가 필요합니다."),
    OAUTH2_USER_INFO_PARSE_FAILED(HttpStatus.BAD_REQUEST, "E026", "소셜 로그인 정보 처리에 실패했습니다."),
    SOCIAL_ACCOUNT_ALREADY_EXISTS_DETAIL(HttpStatus.CONFLICT, "E027", "이미 가입된 소셜 계정입니다."),
    INVALID_PHONE_NUMBER_FORMAT(HttpStatus.BAD_REQUEST, "E028", "전화번호 형식이 올바르지 않습니다."),

    // 약물
    DRUG_NOT_FOUND(HttpStatus.NOT_FOUND, "E029", "약물을 찾을 수 없습니다."),
    DRUG_INTERACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "E030", "약물 상호작용을 찾을 수 없습니다."),
    UNSUPPORTED_DRUG_TYPE(HttpStatus.BAD_REQUEST, "E031", "지원하지 않는 약물 유형입니다."),

    // 리뷰
    REVIEW_DELETE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "E032", "삭제 권한이 없습니다."),
    UNKNOWN_TAG_TYPE(HttpStatus.BAD_REQUEST, "E033", "알 수 없는 태그입니다."),

    // Elasticsearch
    ELASTICSEARCH_OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E034", "검색 작업에 실패했습니다."),

    // JSON
    JSON_CONVERSION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E035", "데이터 변환에 실패했습니다."),
    JSON_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E036", "데이터 처리에 실패했습니다."),

    // 기타
    UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E037", "예상치 못한 오류가 발생했습니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "E038", "입력값이 올바르지 않습니다."),

    // 플랫폼별
    GOOGLE_USER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "E039", "Google ID가 필요합니다."),
    KAKAO_USER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "E040", "카카오 ID가 필요합니다."),
    GOOGLE_USER_INFO_PARSE_FAILED(HttpStatus.BAD_REQUEST, "E041", "Google 정보 처리에 실패했습니다."),
    KAKAO_USER_INFO_PARSE_FAILED(HttpStatus.BAD_REQUEST, "E042", "카카오 정보 처리에 실패했습니다."),

    // 토큰
    INVALID_REFRESH_TOKEN_DETAIL(HttpStatus.UNAUTHORIZED, "E043", "리프레시 토큰이 올바르지 않습니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "E044", "토큰을 찾을 수 없습니다."),
    TOKEN_EXPIRED_DETAIL(HttpStatus.UNAUTHORIZED, "E045", "토큰이 만료되었습니다."),
    TOKEN_INVALID_RETRY_LOGIN(HttpStatus.UNAUTHORIZED, "E046", "토큰이 올바르지 않습니다. 다시 로그인해주세요."),
    TOKEN_EXPIRED_RETRY_LOGIN(HttpStatus.UNAUTHORIZED, "E047", "토큰이 만료되었습니다. 다시 로그인해주세요."),
    USER_INFO_NOT_FOUND_RETRY_LOGIN(HttpStatus.UNAUTHORIZED, "E048", "사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요."),

    // 회원가입 상세
    LOGIN_TYPE_REQUIRED_DETAIL(HttpStatus.BAD_REQUEST, "E049", "로그인 타입을 선택해주세요 (IDPW 또는 SOCIAL)"),
    USER_ID_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "E050", "아이디와 비밀번호를 입력해주세요."),
    TOKEN_REQUIRED_FOR_SOCIAL(HttpStatus.BAD_REQUEST, "E051", "소셜 로그인 토큰이 필요합니다."),

    // 에러 메시지 키워드
    ERROR_KEYWORD_LOGIN_TYPE(HttpStatus.BAD_REQUEST, "E052", "LoginType"),
    ERROR_KEYWORD_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "E053", "전화번호"),
    ERROR_KEYWORD_NICKNAME(HttpStatus.BAD_REQUEST, "E054", "Nickname"),
    ERROR_KEYWORD_USER_ID(HttpStatus.BAD_REQUEST, "E055", "User ID"),

    PROFILE_IS_MAIN(HttpStatus.BAD_REQUEST, "E056", "메인 프로필은 삭제 불가능합니다."),
    INVALID_PROFILE_ID(HttpStatus.BAD_REQUEST, "E056", "인가되지않은 프로필 id 입니다."),


    DUPLICATE_CHECK_FAILED(HttpStatus.CONFLICT, "E200", "이미 사용 중인 %s입니다."),
    DUPLICATE_PHONE_FORMAT(HttpStatus.CONFLICT, "E201", "전화번호가 중복인 것 같습니다. 다른 전화번호를 입력해주세요."),
    DUPLICATE_NICKNAME_FORMAT(HttpStatus.CONFLICT, "E202", "이미 사용 중인 닉네임입니다."),
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "E203", "이미 사용 중인 아이디입니다."),
    INVALID_CHECK_TYPE(HttpStatus.BAD_REQUEST, "E204", "유효하지 않은 체크 타입입니다."),


    NOT_EXIST_USER(HttpStatus.BAD_REQUEST, "U001", "사용자를 찾을 수 없습니다."),
    NOT_EXIST_DOSAGELOG(HttpStatus.BAD_REQUEST, "L001", "복용 기록을 찾을 수 없습니다."),
    MISSING_FCMTOKEN(HttpStatus.BAD_REQUEST, "A001", "알림 토큰이 없습니다."),
    NOT_FRIEND(HttpStatus.BAD_REQUEST, "F001", "친구가 아닙니다."),

    NOT_EXIST_INGREDIENT(HttpStatus.BAD_REQUEST, "I001", "해당 성분을 찾을 수 없습니다."),
    NOT_EXIST_SUPPLEMENT(HttpStatus.BAD_REQUEST, "I002", "해당 건강기능식품을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}