// author : mireutale
// description : 문진표 관련 메시지 상수
package com.oauth2.User.PatientQuestionnaire.Dto;

public class QuestionnaireMessageConstants {
    
    // 문진표 관련 성공 메시지
    public static final String PERMISSIONS_RETRIEVE_SUCCESS = "동의사항을 조회했습니다.";
    public static final String MEDICAL_PERMISSIONS_UPDATE_SUCCESS = "의료정보 동의사항이 수정되었습니다.";
    public static final String PERMISSION_UPDATE_SUCCESS = "동의사항이 수정되었습니다.";
    public static final String QUESTIONNAIRE_AVAILABILITY_CHECK_SUCCESS = "문진표 작성 가능 여부를 확인했습니다.";
    public static final String QUESTIONNAIRE_LIST_RETRIEVE_SUCCESS = "문진표 목록을 조회했습니다.";
    public static final String CURRENT_USER_QUESTIONNAIRE_RETRIEVE_SUCCESS = "현재 사용자 문진표를 조회했습니다.";
    public static final String QUESTIONNAIRE_CREATE_SUCCESS = "문진표가 작성되었습니다.";
    public static final String QUESTIONNAIRE_UPDATE_SUCCESS = "문진표가 수정되었습니다.";
    public static final String QUESTIONNAIRE_DELETE_SUCCESS = "문진표가 삭제되었습니다.";
    public static final String QR_URL_GENERATE_SUCCESS = "QR URL이 생성되었습니다.";
    public static final String PUBLIC_QUESTIONNAIRE_RETRIEVE_SUCCESS = "공개 문진표를 조회했습니다.";
    
    // 문진표 관련 에러 메시지
    public static final String PERMISSIONS_RETRIEVE_FAILED = "동의사항 조회에 실패했습니다.";
    public static final String MEDICAL_PERMISSIONS_UPDATE_FAILED = "의료정보 동의사항 수정에 실패했습니다.";
    public static final String PERMISSION_UPDATE_FAILED = "동의사항 수정에 실패했습니다.";
    public static final String QUESTIONNAIRE_AVAILABILITY_CHECK_FAILED = "문진표 작성 가능 여부 확인에 실패했습니다.";
    public static final String QUESTIONNAIRE_LIST_RETRIEVE_FAILED = "문진표 목록 조회에 실패했습니다.";
    public static final String CURRENT_USER_QUESTIONNAIRE_RETRIEVE_FAILED = "현재 사용자 문진표 조회에 실패했습니다.";
    public static final String QUESTIONNAIRE_RETRIEVE_FAILED = "문진표 조회에 실패했습니다.";
    public static final String QUESTIONNAIRE_CREATE_FAILED = "문진표 작성에 실패했습니다.";
    public static final String QUESTIONNAIRE_UPDATE_FAILED = "문진표 수정에 실패했습니다.";
    public static final String QUESTIONNAIRE_DELETE_FAILED = "문진표 삭제에 실패했습니다.";
    public static final String QR_URL_GENERATE_FAILED = "QR URL 생성에 실패했습니다.";
    public static final String PUBLIC_QUESTIONNAIRE_RETRIEVE_FAILED = "공개 문진표 조회에 실패했습니다.";
    
    // 문진표 관련 검증 메시지
    public static final String INVALID_PERMISSION_TYPE = "동의사항 타입이 올바르지 않습니다.";
    public static final String QUESTIONNAIRE_NOT_FOUND = "문진표가 없습니다.";
    public static final String QUESTIONNAIRE_ALREADY_EXISTS = "이미 작성된 문진표가 있습니다. 수정해주세요.";
    public static final String QUESTIONNAIRE_ACCESS_DENIED = "본인 문진표만 볼 수 있습니다.";
    public static final String QUESTIONNAIRE_UPDATE_DENIED = "본인 문진표만 수정할 수 있습니다.";
    public static final String QUESTIONNAIRE_DELETE_DENIED = "본인 문진표만 삭제할 수 있습니다.";
    public static final String INVALID_CUSTOM_TOKEN = "토큰이 올바르지 않습니다.";
    public static final String QUESTIONNAIRE_AVAILABLE_MESSAGE = "문진표 작성이 가능합니다.";
    public static final String QUESTIONNAIRE_UNAVAILABLE_MESSAGE = "문진표 작성을 위해 다음 항목을 완료해주세요: ";
    public static final String MISSING_PERMISSIONS = "동의사항 미완료";
    public static final String MISSING_PERSONAL_INFO = "실명/주소 미입력";
    
    // QR URL 관련 추가 메시지
    public static final String QR_URL_RETRIEVE_SUCCESS = "QR URL을 조회했습니다.";
    public static final String QR_URL_RETRIEVE_FAILED = "QR URL 조회에 실패했습니다.";
    public static final String QR_URL_DELETE_SUCCESS = "QR URL이 삭제되었습니다.";
    public static final String QR_URL_DELETE_FAILED = "QR URL 삭제에 실패했습니다.";
    public static final String QR_URL_NOT_FOUND = "QR URL이 존재하지 않습니다.";
    public static final String QR_URL_EXPIRED = "QR URL이 만료되었습니다.";
    public static final String QR_ACCESS_SUCCESS = "QR을 통한 문진표 접근 성공";
    public static final String QR_ACCESS_FAILED = "QR을 통한 문진표 접근에 실패했습니다.";
} 