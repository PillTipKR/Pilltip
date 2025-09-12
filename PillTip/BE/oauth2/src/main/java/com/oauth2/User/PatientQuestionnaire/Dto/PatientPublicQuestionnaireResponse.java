package com.oauth2.User.PatientQuestionnaire.Dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.User.PatientQuestionnaire.Entity.PatientQuestionnaire;
import com.oauth2.User.TakingPill.Service.TakingPillService;
import com.oauth2.Util.Encryption.EncryptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientPublicQuestionnaireResponse {
    private Long questionnaireId;
    private String questionnaireName;
    private String realName;
    private String address;
    private String phoneNumber;
    private String gender;
    private String birthDate;
    private String height;
    private String weight;
    private String pregnant;
    private LocalDate issueDate;
    private LocalDate lastModifiedDate;
    private String notes;
    private List<Map<String, Object>> medicationInfo;
    private List<Map<String, Object>> allergyInfo;
    private List<Map<String, Object>> chronicDiseaseInfo;
    private List<Map<String, Object>> surgeryHistoryInfo;
    private Long expirationDate; // 1시간 후 만료 시간(ms)

    public static PatientPublicQuestionnaireResponse from(PatientQuestionnaire questionnaire, String decryptedPhoneNumber, String decryptedRealName, String decryptedAddress, EncryptionUtil encryptionUtil, Long expirationDate) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return PatientPublicQuestionnaireResponse.builder()
                    .questionnaireId(questionnaire.getQuestionnaireId())
                    .questionnaireName(questionnaire.getQuestionnaireName())
                    .realName(decryptedRealName)
                    .address(decryptedAddress)
                    .phoneNumber(decryptedPhoneNumber)
                    .gender(questionnaire.getUser().getUserProfile() != null ? 
                           questionnaire.getUser().getUserProfile().getGender() != null ? 
                           questionnaire.getUser().getUserProfile().getGender().name() : null : null)
                    .birthDate(questionnaire.getUser().getUserProfile() != null && 
                             questionnaire.getUser().getUserProfile().getBirthDate() != null ? 
                             questionnaire.getUser().getUserProfile().getBirthDate().toString() : null)
                    .height(questionnaire.getUser().getUserProfile() != null && 
                           questionnaire.getUser().getUserProfile().getHeight() != null ? 
                           questionnaire.getUser().getUserProfile().getHeight().toString() : null)
                    .weight(questionnaire.getUser().getUserProfile() != null && 
                           questionnaire.getUser().getUserProfile().getWeight() != null ? 
                           questionnaire.getUser().getUserProfile().getWeight().toString() : null)
                    .pregnant(questionnaire.getUser().getUserProfile() != null ? 
                            questionnaire.getUser().getUserProfile().isPregnant() ? "Y" : "N" : null)
                    .issueDate(questionnaire.getIssueDate())
                    .lastModifiedDate(questionnaire.getLastModifiedDate())
                    .notes(questionnaire.getNotes())
                    .medicationInfo(parseEncryptedJsonToList(questionnaire.getMedicationInfo(), objectMapper, encryptionUtil))
                    .allergyInfo(parseEncryptedJsonToList(questionnaire.getAllergyInfo(), objectMapper, encryptionUtil))
                    .chronicDiseaseInfo(parseEncryptedJsonToList(questionnaire.getChronicDiseaseInfo(), objectMapper, encryptionUtil))
                    .surgeryHistoryInfo(parseEncryptedJsonToList(questionnaire.getSurgeryHistoryInfo(), objectMapper, encryptionUtil))
                    .expirationDate(expirationDate)
                    .build();
        } catch (Exception e) {
            // 파싱 실패 시에도 빈 리스트로 기본값 설정
            return PatientPublicQuestionnaireResponse.builder()
                    .questionnaireId(questionnaire.getQuestionnaireId())
                    .questionnaireName(questionnaire.getQuestionnaireName())
                    .realName(decryptedRealName)
                    .address(decryptedAddress)
                    .phoneNumber(decryptedPhoneNumber)
                    .gender(questionnaire.getUser().getUserProfile() != null ? 
                           questionnaire.getUser().getUserProfile().getGender() != null ? 
                           questionnaire.getUser().getUserProfile().getGender().name() : null : null)
                    .birthDate(questionnaire.getUser().getUserProfile() != null && 
                             questionnaire.getUser().getUserProfile().getBirthDate() != null ? 
                             questionnaire.getUser().getUserProfile().getBirthDate().toString() : null)
                    .height(questionnaire.getUser().getUserProfile() != null && 
                           questionnaire.getUser().getUserProfile().getHeight() != null ? 
                           questionnaire.getUser().getUserProfile().getHeight().toString() : null)
                    .weight(questionnaire.getUser().getUserProfile() != null && 
                           questionnaire.getUser().getUserProfile().getWeight() != null ? 
                           questionnaire.getUser().getUserProfile().getWeight().toString() : null)
                    .pregnant(questionnaire.getUser().getUserProfile() != null ? 
                            questionnaire.getUser().getUserProfile().isPregnant() ? "Y" : "N" : null)
                    .issueDate(questionnaire.getIssueDate())
                    .lastModifiedDate(questionnaire.getLastModifiedDate())
                    .notes(questionnaire.getNotes())
                    .medicationInfo(List.of())
                    .allergyInfo(List.of())
                    .chronicDiseaseInfo(List.of())
                    .surgeryHistoryInfo(List.of())
                    .expirationDate(expirationDate)
                    .build();
        }
    }

    // 실시간 taking-pill 정보를 포함한 공개 문진표 응답 생성
    public static PatientPublicQuestionnaireResponse fromWithRealTimeMedication(PatientQuestionnaire questionnaire, String decryptedPhoneNumber, String decryptedRealName, String decryptedAddress, EncryptionUtil encryptionUtil, TakingPillService takingPillService, Long expirationDate) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // 실시간으로 taking-pill에서 약물 정보 가져오기
            List<Map<String, Object>> realTimeMedicationInfo = takingPillService.getTakingPillsByUser(questionnaire.getUser()).stream()
                    .map(takingPill -> {
                        Map<String, Object> medication = new HashMap<>();
                        medication.put("medicationId", takingPill.getMedicationId());
                        medication.put("medicationName", takingPill.getMedicationName());
                        medication.put("submitted", true);
                        return medication;
                    })
                    .collect(Collectors.toList());

            return PatientPublicQuestionnaireResponse.builder()
                    .questionnaireId(questionnaire.getQuestionnaireId())
                    .questionnaireName(questionnaire.getQuestionnaireName())
                    .realName(decryptedRealName)
                    .address(decryptedAddress)
                    .phoneNumber(decryptedPhoneNumber)
                    .gender(questionnaire.getUser().getUserProfile() != null ? 
                           questionnaire.getUser().getUserProfile().getGender() != null ? 
                           questionnaire.getUser().getUserProfile().getGender().name() : null : null)
                    .birthDate(questionnaire.getUser().getUserProfile() != null && 
                             questionnaire.getUser().getUserProfile().getBirthDate() != null ? 
                             questionnaire.getUser().getUserProfile().getBirthDate().toString() : null)
                    .height(questionnaire.getUser().getUserProfile() != null && 
                           questionnaire.getUser().getUserProfile().getHeight() != null ? 
                           questionnaire.getUser().getUserProfile().getHeight().toString() : null)
                    .weight(questionnaire.getUser().getUserProfile() != null && 
                           questionnaire.getUser().getUserProfile().getWeight() != null ? 
                           questionnaire.getUser().getUserProfile().getWeight().toString() : null)
                    .pregnant(questionnaire.getUser().getUserProfile() != null ? 
                            questionnaire.getUser().getUserProfile().isPregnant() ? "Y" : "N" : null)
                    .issueDate(questionnaire.getIssueDate())
                    .lastModifiedDate(questionnaire.getLastModifiedDate())
                    .notes(questionnaire.getNotes())
                    .medicationInfo(realTimeMedicationInfo) // 실시간 taking-pill 정보 사용
                    .allergyInfo(parseEncryptedJsonToList(questionnaire.getAllergyInfo(), objectMapper, encryptionUtil))
                    .chronicDiseaseInfo(parseEncryptedJsonToList(questionnaire.getChronicDiseaseInfo(), objectMapper, encryptionUtil))
                    .surgeryHistoryInfo(parseEncryptedJsonToList(questionnaire.getSurgeryHistoryInfo(), objectMapper, encryptionUtil))
                    .expirationDate(expirationDate)
                    .build();
        } catch (Exception e) {
            // 파싱 실패 시에도 빈 리스트로 기본값 설정
            return PatientPublicQuestionnaireResponse.builder()
                    .questionnaireId(questionnaire.getQuestionnaireId())
                    .questionnaireName(questionnaire.getQuestionnaireName())
                    .realName(decryptedRealName)
                    .address(decryptedAddress)
                    .phoneNumber(decryptedPhoneNumber)
                    .gender(questionnaire.getUser().getUserProfile() != null ? 
                           questionnaire.getUser().getUserProfile().getGender() != null ? 
                           questionnaire.getUser().getUserProfile().getGender().name() : null : null)
                    .birthDate(questionnaire.getUser().getUserProfile() != null && 
                             questionnaire.getUser().getUserProfile().getBirthDate() != null ? 
                             questionnaire.getUser().getUserProfile().getBirthDate().toString() : null)
                    .height(questionnaire.getUser().getUserProfile() != null && 
                           questionnaire.getUser().getUserProfile().getHeight() != null ? 
                           questionnaire.getUser().getUserProfile().getHeight().toString() : null)
                    .weight(questionnaire.getUser().getUserProfile() != null && 
                           questionnaire.getUser().getUserProfile().getWeight() != null ? 
                           questionnaire.getUser().getUserProfile().getWeight().toString() : null)
                    .pregnant(questionnaire.getUser().getUserProfile() != null ? 
                            questionnaire.getUser().getUserProfile().isPregnant() ? "Y" : "N" : null)
                    .issueDate(questionnaire.getIssueDate())
                    .lastModifiedDate(questionnaire.getLastModifiedDate())
                    .notes(questionnaire.getNotes())
                    .medicationInfo(List.of())
                    .allergyInfo(List.of())
                    .chronicDiseaseInfo(List.of())
                    .surgeryHistoryInfo(List.of())
                    .expirationDate(expirationDate)
                    .build();
        }
    }
    
    private static List<Map<String, Object>> parseJsonToList(String json, ObjectMapper objectMapper) throws JsonProcessingException {
        if (json == null || json.trim().isEmpty() || "null".equals(json.trim())) {
            return List.of();
        }
        return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
    }

    private static List<Map<String, Object>> parseEncryptedJsonToList(String encryptedJson, ObjectMapper objectMapper, EncryptionUtil encryptionUtil) throws JsonProcessingException {
        if (encryptedJson == null || encryptedJson.trim().isEmpty() || "null".equals(encryptedJson.trim())) {
            return List.of();
        }
        
        try {
            // 암호화된 JSON을 복호화
            String decryptedJson = encryptionUtil.decrypt(encryptedJson);
            if (decryptedJson == null || decryptedJson.trim().isEmpty() || "null".equals(decryptedJson.trim())) {
                return List.of();
            }
            return objectMapper.readValue(decryptedJson, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            // 복호화 실패 시 원본 JSON으로 파싱 시도
            try {
                return parseJsonToList(encryptedJson, objectMapper);
            } catch (Exception parseException) {
                // 파싱도 실패하면 빈 리스트 반환
                return List.of();
            }
        }
    }
} 