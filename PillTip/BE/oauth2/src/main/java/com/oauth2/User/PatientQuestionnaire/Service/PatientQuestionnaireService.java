package com.oauth2.User.PatientQuestionnaire.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.User.PatientQuestionnaire.Dto.PatientQuestionnaireRequest;
import com.oauth2.User.PatientQuestionnaire.Dto.PatientQuestionnaireSummaryResponse;
import com.oauth2.User.PatientQuestionnaire.Entity.PatientQuestionnaire;
import com.oauth2.User.PatientQuestionnaire.Repository.PatientQuestionnaireRepository;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.PatientQuestionnaire.Dto.QuestionnaireMessageConstants;
import com.oauth2.User.UserInfo.Service.UserService;
import com.oauth2.User.TakingPill.Service.TakingPillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientQuestionnaireService {
    private final PatientQuestionnaireRepository questionnaireRepository;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final TakingPillService takingPillService;

    @Transactional
    public PatientQuestionnaire createQuestionnaire(User user, PatientQuestionnaireRequest request) throws JsonProcessingException {
        // User를 다시 조회하여 영속성 컨텍스트에 연결
        User managedUser = userService.getCurrentUser(user.getId());
        
        // 문진표 존재 여부 먼저 확인
        if (!questionnaireRepository.findByUser(managedUser).isEmpty()) {
            throw new IllegalStateException("이미 작성된 문진표가 존재합니다. 수정 기능을 이용해주세요.");
        }
        
        // Update user realName and address
        User updatedUser = userService.updatePersonalInfo(managedUser, request.getRealName(), request.getAddress());
        userService.updatePhoneNumber(updatedUser, request.getPhoneNumber());
        
        // Taking-pill에서 모든 약 이름 가져오기 (생성 시에는 자동으로 가져옴)
        List<Map<String, Object>> takingPillMedications = takingPillService.getTakingPillsByUser(updatedUser).stream()
                .map(takingPill -> {
                    Map<String, Object> medication = new HashMap<>();
                    medication.put("medicationId", takingPill.getMedicationId());
                    medication.put("medicationName", takingPill.getMedicationName());
                    medication.put("submitted", true);
                    return medication;
                })
                .collect(Collectors.toList());
        
        String allergyInfoJson = objectMapper.writeValueAsString(
                toKeyedList(request.getAllergyInfo(), "allergyName")
        );
        String chronicDiseaseInfoJson = objectMapper.writeValueAsString(
                toKeyedList(request.getChronicDiseaseInfo(), "chronicDiseaseName")
        );
        String surgeryHistoryInfoJson = objectMapper.writeValueAsString(
                toKeyedList(request.getSurgeryHistoryInfo(), "surgeryHistoryName")
        );
        String medicationInfoJson = objectMapper.writeValueAsString(takingPillMedications);

        PatientQuestionnaire questionnaire = PatientQuestionnaire.builder()
                .user(updatedUser)
                .questionnaireName(request.getRealName() + "님의 문진표")
                .notes("")
                .issueDate(LocalDate.now())
                .lastModifiedDate(LocalDate.now())
                .allergyInfo(allergyInfoJson)
                .chronicDiseaseInfo(chronicDiseaseInfoJson)
                .surgeryHistoryInfo(surgeryHistoryInfoJson)
                .medicationInfo(medicationInfoJson)
                .build();
        
        PatientQuestionnaire savedQuestionnaire = questionnaireRepository.save(questionnaire);
        
        return savedQuestionnaire;
    }

    private List<Map<String, ?>> toKeyedList(List<PatientQuestionnaireRequest.InfoItem> list, String keyName) {
        if (list == null || list.isEmpty()) return List.of();
        return list.stream()
                .map(item -> {
                    String value = null;
                    if ("allergyName".equals(keyName)) {
                        value = item.getAllergyName();
                    } else if ("chronicDiseaseName".equals(keyName)) {
                        value = item.getChronicDiseaseName();
                    } else if ("surgeryHistoryName".equals(keyName)) {
                        value = item.getSurgeryHistoryName();
                    } else if ("medicationName".equals(keyName)) {
                        value = item.getMedicationName();
                    }
                    return Map.of(
                            keyName, value,
                            "submitted", item.isSubmitted()
                    );
                })
                .collect(Collectors.toList());
    }

    public List<PatientQuestionnaireSummaryResponse> getUserQuestionnaireSummaries(User user) {
        return questionnaireRepository.findByUser(user).stream()
            .map(q -> new PatientQuestionnaireSummaryResponse(
                q.getQuestionnaireId(),
                q.getQuestionnaireName(),
                q.getIssueDate(),
                q.getLastModifiedDate()
            ))
            .collect(Collectors.toList());
    }

    public PatientQuestionnaire getQuestionnaireById(User user, Long id) {
        PatientQuestionnaire questionnaire = questionnaireRepository.findByIdWithUser(id)
            .orElseThrow(() -> new IllegalArgumentException("문진표를 찾을 수 없습니다."));
        
        // 본인 문진표만 조회 가능
        if (!questionnaire.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인 문진표만 조회할 수 있습니다.");
        }
        
        return questionnaire;
    }

    @Transactional
    public void deleteQuestionnaire(User user, Long id) {
        PatientQuestionnaire q = questionnaireRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("문진표를 찾을 수 없습니다."));
        if (!q.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인 문진표만 삭제할 수 있습니다.");
        }
        questionnaireRepository.delete(q);
    }

    // @Transactional
    // public PatientQuestionnaire updateQuestionnaire(User user, Long id, PatientQuestionnaireRequest request) throws JsonProcessingException {
    //     // User를 다시 조회하여 영속성 컨텍스트에 연결
    //     User managedUser = userService.getCurrentUser(user.getId());
        
    //     // Update user realName and address
    //     User updatedUser = userService.updatePersonalInfo(managedUser, request.getRealName(), request.getAddress());
    //     userService.updatePhoneNumber(updatedUser, request.getPhoneNumber());
        
    //     PatientQuestionnaire q = questionnaireRepository.findByIdWithUser(id)
    //         .orElseThrow(() -> new IllegalArgumentException("문진표를 찾을 수 없습니다."));
    //     if (!q.getUser().getId().equals(updatedUser.getId())) {
    //         throw new SecurityException("본인 문진표만 수정할 수 있습니다.");
    //     }
        
    //     // Taking-pill에서 모든 약 이름 가져오기 (검증용)
    //     List<String> takingPillMedicationNames = takingPillService.getTakingPillsByUser(updatedUser).stream()
    //             .map(takingPill -> takingPill.getMedicationName())
    //             .collect(Collectors.toList());
        
    //     // Request body의 medicationInfo와 taking-pill 데이터 검증 (수정 시에만)
    //     if (request.getMedicationInfo() != null) {
    //         List<String> requestMedicationNames = request.getMedicationInfo().stream()
    //                 .map(item -> item.getMedicationName())
    //                 .filter(name -> name != null && !name.trim().isEmpty())
    //                 .collect(Collectors.toList());
            
    //         // taking-pill에 없는 약이 있는지 확인
    //         List<String> invalidMedications = requestMedicationNames.stream()
    //                 .filter(name -> !takingPillMedicationNames.contains(name))
    //                 .collect(Collectors.toList());
            
    //                 if (!invalidMedications.isEmpty()) {
    //         throw new IllegalArgumentException("다음 약물은 복용 중인 약 목록에 없습니다: " + String.join(", ", invalidMedications));
    //     }
    // }
    
    // q.setQuestionnaireName(request.getRealName() + "님의 문진표");
    // q.setNotes("");
    // q.setAllergyInfo(objectMapper.writeValueAsString(toKeyedList(request.getAllergyInfo(), "allergyName")));
    //     q.setChronicDiseaseInfo(objectMapper.writeValueAsString(toKeyedList(request.getChronicDiseaseInfo(), "chronicDiseaseName")));
    //     q.setSurgeryHistoryInfo(objectMapper.writeValueAsString(toKeyedList(request.getSurgeryHistoryInfo(), "surgeryHistoryName")));
    //     q.setMedicationInfo(objectMapper.writeValueAsString(toKeyedList(request.getMedicationInfo(), "medicationName")));
    //     q.setLastModifiedDate(LocalDate.now());
        
    //     return q;
    // }

    @Transactional
    public List<PatientQuestionnaireSummaryResponse> deleteQuestionnaireAndReturnList(User user, Long id) {
        PatientQuestionnaire q = questionnaireRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("문진표를 찾을 수 없습니다."));
        if (!q.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인 문진표만 삭제할 수 있습니다.");
        }
        questionnaireRepository.delete(q);
        // 삭제 후 남아있는 리스트 반환
        return getUserQuestionnaireSummaries(user);
    }

    // 사용자의 최신 문진표 조회
    public PatientQuestionnaire getLatestQuestionnaireByUser(User user) {
        return questionnaireRepository.findTopByUserOrderByIssueDateDesc(user)
                .orElse(null);
    }

    // 현재 접속한 유저의 문진표 조회
    public PatientQuestionnaire getCurrentUserQuestionnaire(User user) {
        // User를 다시 조회하여 영속성 컨텍스트에 연결
        User managedUser = userService.getCurrentUser(user.getId());
        
        return questionnaireRepository.findByUser(managedUser).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(QuestionnaireMessageConstants.QUESTIONNAIRE_NOT_FOUND));
    }

    // 현재 접속한 유저의 문진표 삭제
    @Transactional
    public void deleteCurrentUserQuestionnaire(User user) {
        // User를 다시 조회하여 영속성 컨텍스트에 연결
        User managedUser = userService.getCurrentUser(user.getId());
        
        // 사용자의 문진표가 존재하는지 확인
        if (questionnaireRepository.findByUser(managedUser).isEmpty()) {
            throw new IllegalArgumentException(QuestionnaireMessageConstants.QUESTIONNAIRE_NOT_FOUND);
        }
        
        // 사용자의 문진표 삭제
        questionnaireRepository.deleteByUser(managedUser);
        
        // 영속성 컨텍스트 플러시
        questionnaireRepository.flush();
    }

    // 현재 접속한 유저의 문진표 수정
    @Transactional
    public PatientQuestionnaire updateCurrentUserQuestionnaire(User user, PatientQuestionnaireRequest request) throws JsonProcessingException {
        // User를 다시 조회하여 영속성 컨텍스트에 연결
        User managedUser = userService.getCurrentUser(user.getId());
        
        // Update user realName and address
        User updatedUser = userService.updatePersonalInfo(managedUser, request.getRealName(), request.getAddress());
        userService.updatePhoneNumber(updatedUser, request.getPhoneNumber());
        
        // 업데이트된 User 정보를 다시 조회하여 최신 상태 확인
        User finalUser = userService.getCurrentUser(updatedUser.getId());
        
        // Taking-pill에서 모든 약 정보 가져오기 (자동으로 가져옴)
        List<Map<String, Object>> takingPillMedications = takingPillService.getTakingPillsByUser(finalUser).stream()
                .map(takingPill -> {
                    Map<String, Object> medication = new HashMap<>();
                    medication.put("medicationId", takingPill.getMedicationId());
                    medication.put("medicationName", takingPill.getMedicationName());
                    medication.put("submitted", true);
                    return medication;
                })
                .collect(Collectors.toList());
        
        // 문진표 조회
        PatientQuestionnaire q = getCurrentUserQuestionnaire(finalUser);
        
        q.setQuestionnaireName(request.getRealName() + "님의 문진표");
        q.setNotes("");
        q.setAllergyInfo(objectMapper.writeValueAsString(toKeyedList(request.getAllergyInfo(), "allergyName")));
        q.setChronicDiseaseInfo(objectMapper.writeValueAsString(toKeyedList(request.getChronicDiseaseInfo(), "chronicDiseaseName")));
        q.setSurgeryHistoryInfo(objectMapper.writeValueAsString(toKeyedList(request.getSurgeryHistoryInfo(), "surgeryHistoryName")));
        q.setMedicationInfo(objectMapper.writeValueAsString(takingPillMedications));
        q.setLastModifiedDate(LocalDate.now());
        
        return q;
    }

    // 소유자 검증 없이 문진표 조회
    public PatientQuestionnaire getQuestionnaireByIdPublic(Long id) {
        return questionnaireRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("문진표를 찾을 수 없습니다."));
    }

    // === 민감정보에서 문진표 동기화 ===
    @Transactional
    public void syncFromSensitiveInfo(User user, com.oauth2.User.UserInfo.Dto.UserSensitiveInfoDto sensitiveInfo) {
        // 최신 문진표 조회
        PatientQuestionnaire questionnaire = getLatestQuestionnaireByUser(user);
        try {
            // 민감정보가 null이면 모두 빈 리스트로 처리
            java.util.List<PatientQuestionnaireRequest.InfoItem> allergyList = java.util.Collections.emptyList();
            java.util.List<PatientQuestionnaireRequest.InfoItem> chronicList = java.util.Collections.emptyList();
            java.util.List<PatientQuestionnaireRequest.InfoItem> surgeryList = java.util.Collections.emptyList();

            if (sensitiveInfo != null) {
                allergyList = toInfoItemList(sensitiveInfo.getAllergyInfo(), "allergyName");
                chronicList = toInfoItemList(sensitiveInfo.getChronicDiseaseInfo(), "chronicDiseaseName");
                surgeryList = toInfoItemList(sensitiveInfo.getSurgeryHistoryInfo(), "surgeryHistoryName");
            }

            if (questionnaire == null) {
                // 문진표가 없으면 새로 생성
                PatientQuestionnaireRequest req = PatientQuestionnaireRequest.builder()
                    .realName(user.getRealName())
                    .address(user.getAddress())
                    .phoneNumber(user.getUserProfile() != null ? user.getUserProfile().getPhone() : null)
                    .allergyInfo(allergyList)
                    .chronicDiseaseInfo(chronicList)
                    .surgeryHistoryInfo(surgeryList)
                    .build();
                createQuestionnaire(user, req);
            } else {
                // 있으면 값만 갱신
                questionnaire.setAllergyInfo(objectMapper.writeValueAsString(toKeyedList(allergyList, "allergyName")));
                questionnaire.setChronicDiseaseInfo(objectMapper.writeValueAsString(toKeyedList(chronicList, "chronicDiseaseName")));
                questionnaire.setSurgeryHistoryInfo(objectMapper.writeValueAsString(toKeyedList(surgeryList, "surgeryHistoryName")));
                questionnaire.setLastModifiedDate(java.time.LocalDate.now());
            }
        } catch (Exception e) {
            throw new RuntimeException("문진표 동기화 실패: " + e.getMessage(), e);
        }
    }

    // 민감정보 List<String> -> InfoItem 리스트 변환
    private java.util.List<PatientQuestionnaireRequest.InfoItem> toInfoItemList(java.util.List<String> list, String key) {
        if (list == null) return java.util.Collections.emptyList();
        return list.stream()
            .map(value -> PatientQuestionnaireRequest.InfoItem.builder()
                .allergyName("allergyName".equals(key) ? value : null)
                .chronicDiseaseName("chronicDiseaseName".equals(key) ? value : null)
                .surgeryHistoryName("surgeryHistoryName".equals(key) ? value : null)
                .submitted(true)
                .build())
            .collect(java.util.stream.Collectors.toList());
    }
    
    // /**
    //  * 문진표에서 민감정보 동기화
    //  */
    // private void syncSensitiveInfoFromQuestionnaire(User user, PatientQuestionnaireRequest request) {
    //     try {
    //         // 문진표의 정보를 민감정보 형식으로 변환
    //         String allergyInfo = convertInfoItemsToString(request.getAllergyInfo());
    //         String chronicDiseaseInfo = convertInfoItemsToString(request.getChronicDiseaseInfo());
    //         String surgeryHistoryInfo = convertInfoItemsToString(request.getSurgeryHistoryInfo());
            
    //         // 민감정보 동기화
    //         userSensitiveInfoService.syncFromQuestionnaire(user, allergyInfo, chronicDiseaseInfo, surgeryHistoryInfo);
            
    //         logger.info("Successfully synced sensitive info from questionnaire for user: {}", user.getId());
    //     } catch (Exception e) {
    //         logger.error("Failed to sync sensitive info from questionnaire for user {}: {}", user.getId(), e.getMessage(), e);
    //     }
    // }
    
    // /**
    //  * InfoItem 리스트를 쉼표로 구분된 문자열로 변환
    //  */
    // private String convertInfoItemsToString(List<PatientQuestionnaireRequest.InfoItem> items) {
    //     if (items == null || items.isEmpty()) {
    //         return "";
    //     }
        
    //     return items.stream()
    //             .filter(item -> item != null && isItemValid(item))
    //             .map(this::extractItemValue)
    //             .filter(value -> value != null && !value.trim().isEmpty())
    //             .collect(Collectors.joining(","));
    // }
    
    // /**
    //  * InfoItem이 유효한지 확인
    //  */
    // private boolean isItemValid(PatientQuestionnaireRequest.InfoItem item) {
    //     return item.getAllergyName() != null || item.getChronicDiseaseName() != null || item.getSurgeryHistoryName() != null;
    // }
    
    // /**
    //  * InfoItem에서 값을 추출
    //  */
    // private String extractItemValue(PatientQuestionnaireRequest.InfoItem item) {
    //     if (item.getAllergyName() != null && !item.getAllergyName().trim().isEmpty()) {
    //         return item.getAllergyName();
    //     }
    //     if (item.getChronicDiseaseName() != null && !item.getChronicDiseaseName().trim().isEmpty()) {
    //         return item.getChronicDiseaseName();
    //     }
    //     if (item.getSurgeryHistoryName() != null && !item.getSurgeryHistoryName().trim().isEmpty()) {
    //         return item.getSurgeryHistoryName();
    //     }
    //     return null;
    // }

    // /**
    //  * 문진표에 등록된 복약기록을 takingPill에 자동 등록하는 메서드
    //  */
    // @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    // private void syncMedicationToTakingPill(User user, List<PatientQuestionnaireRequest.InfoItem> medicationInfo) {
    //     if (medicationInfo == null || medicationInfo.isEmpty()) {
    //         return;
    //     }

    //     // submit 값과 관계없이 모든 약물 처리
    //     List<PatientQuestionnaireRequest.InfoItem> validMedications = medicationInfo.stream()
    //             .filter(item -> item.getMedicationId() != null)
    //             .collect(Collectors.toList());

    //     for (PatientQuestionnaireRequest.InfoItem medication : validMedications) {
    //         try {
    //             Long medicationId = Long.parseLong(medication.getMedicationId());
    //             String medicationName = medication.getMedicationName() != null ? medication.getMedicationName() : "문진표 등록 약물";
                
    //             // 이미 takingPill에 등록되어 있는지 확인 (name과 id가 동일한 값이 있는지)
    //             boolean alreadyExists = false;
    //             try {
    //                 takingPillService.getTakingPillDetailById(user, medicationId);
    //                 // ID로 조회 성공하면 이미 존재
    //                 alreadyExists = true;
    //                 logger.info("Medication with ID {} is already registered in takingPill for user {}", medicationId, user.getId());
    //             } catch (Exception e) {
    //                 // ID로 조회 실패하면 존재하지 않음
    //                 alreadyExists = false;
    //             }
                
    //             if (alreadyExists) {
    //                 continue; // 이미 등록되어 있으면 건너뛰기
    //             }
                
    //             // 등록되어 있지 않으면 새로 등록
    //             logger.info("Medication {} ({}) is not registered in takingPill, creating new entry for user {}", medicationId, medicationName, user.getId());
                
    //             // 기본 복용 정보로 TakingPillRequest 생성
    //             TakingPillRequest takingPillRequest = new TakingPillRequest();
    //             takingPillRequest.setMedicationId(medicationId);
    //             takingPillRequest.setMedicationName(medicationName);
    //             takingPillRequest.setStartDate(LocalDate.now()); // 오늘부터 시작
    //             takingPillRequest.setEndDate(LocalDate.now().plusYears(1)); // 1년 후까지
    //             takingPillRequest.setAlarmName("문진표 등록 약물");
    //             takingPillRequest.setDosageAmount(1.0); // 기본 복용량 1
    //             takingPillRequest.setDaysOfWeek(List.of("EVERYDAY")); // 매일 복용
                
    //             // TakingPill에 등록
    //             takingPillService.addTakingPill(user, takingPillRequest);
    //             logger.info("Successfully registered medication {} ({}) to takingPill for user {}", medicationId, medicationName, user.getId());
                
    //         } catch (NumberFormatException e) {
    //             logger.warn("Invalid medication ID format: {} for user {}", medication.getMedicationId(), user.getId());
    //         } catch (Exception e) {
    //             logger.error("Failed to register medication {} ({}) to takingPill for user {}: {}", 
    //                 medication.getMedicationId(), medication.getMedicationName(), user.getId(), e.getMessage());
    //             // 개별 약물 등록 실패가 전체 프로세스를 중단하지 않도록 예외를 다시 던지지 않음
    //         }
    //     }
    // }
} 