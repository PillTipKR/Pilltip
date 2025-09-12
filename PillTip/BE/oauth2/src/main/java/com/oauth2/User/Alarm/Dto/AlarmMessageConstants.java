// author : mireutale
// description : 알람 관련 메시지 상수
package com.oauth2.User.Alarm.Dto;

public class AlarmMessageConstants {
    
    // 알람 관련 성공 메시지
    public static final String ALARM_RESEND_SUCCESS = "5분 후 재전송됩니다.";
    public static final String DOSAGE_HISTORY_UPDATE_SUCCESS = "복용 기록이 수정되었습니다.";
    public static final String FRIEND_ALARM_SEND_SUCCESS = "친구에게 복용 알림을 보냈습니다.";
    
    // 알람 관련 에러 메시지
    public static final String DOSAGE_TIME_NOT_PASSED = "복용 시간이 아직 지나지 않았습니다.";
    public static final String FRIEND_ALARM_SEND_FAILED = "복용 알림 전송에 실패했습니다.";
    public static final String ALARM_RESEND_FAILED = "알람 재전송에 실패했습니다.";
    public static final String DOSAGE_HISTORY_UPDATE_FAILED = "복용 기록 수정에 실패했습니다.";
    
    // 알람 메시지 템플릿
    public static final String MEDICATION_TIME_MESSAGE = " 복용할 시간입니다!";
    public static final String FRIEND_WORRY_TITLE_TEMPLATE = "님이 걱정하고 있습니다. 약을 드셨나요?";
    public static final String FRIEND_WORRY_BODY_TEMPLATE = "에 복용 예정이었던 ";
    public static final String FRIEND_WORRY_BODY_END = " 복용 시간이 지났습니다.";
    
    // Firebase 관련 에러 메시지
    public static final String FIREBASE_CONFIG_ERROR = "Firebase 설정 오류: ";
} 