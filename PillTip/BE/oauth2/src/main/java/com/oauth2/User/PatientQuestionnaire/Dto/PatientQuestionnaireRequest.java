package com.oauth2.User.PatientQuestionnaire.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientQuestionnaireRequest {
    //private String questionnaireName;

    // 약물 정보는 Taking Pill에서 자동으로 가져옴 (생성/수정 시 모두)
    // private List<InfoItem> medicationInfo;
    private List<InfoItem> allergyInfo;
    private List<InfoItem> chronicDiseaseInfo;
    private List<InfoItem> surgeryHistoryInfo;

    // private String notes;ㄴ

    // Add these fields for user info update
    private String realName;
    private String address;
    private String phoneNumber;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InfoItem {
        private String medicationName;  // 복용 중인 약물 이름만
        private String allergyName;     // 알레르기 이름
        private String chronicDiseaseName;  // 만성질환 이름
        private String surgeryHistoryName;  // 수술 이력 이름
        private boolean submitted;
    }
} 