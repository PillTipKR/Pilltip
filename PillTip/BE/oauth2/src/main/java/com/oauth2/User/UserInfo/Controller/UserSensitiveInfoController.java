// author : mireutale
// description : 사용자 민감정보 관리 컨트롤러
package com.oauth2.User.UserInfo.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oauth2.Account.Service.AccountService;
import com.oauth2.Account.Entity.Account;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.UserInfo.Dto.UserSensitiveInfoDeleteRequest;
import com.oauth2.User.UserInfo.Dto.UserSensitiveInfoDto;
import com.oauth2.User.UserInfo.Dto.UserProfileUpdateRequest;
import com.oauth2.User.UserInfo.Dto.UserProfileUpdateResponse;
import com.oauth2.User.UserInfo.Service.UserSensitiveInfoService;
import com.oauth2.User.UserInfo.Service.UserService;
import com.oauth2.Account.Dto.ApiResponse;
import com.oauth2.User.UserInfo.Dto.UserInfoMessageConstants;
import com.oauth2.User.PatientQuestionnaire.Service.PatientQuestionnaireService;
import com.oauth2.User.PatientQuestionnaire.Dto.PatientQuestionnaireRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sensitive-info")
@RequiredArgsConstructor
public class UserSensitiveInfoController {

    private static final Logger logger = LoggerFactory.getLogger(UserSensitiveInfoController.class);
    private final UserSensitiveInfoService userSensitiveInfoService;
    private final AccountService accountService;
    private final UserService userService;
    private final PatientQuestionnaireService patientQuestionnaireService;

    /**
     * 사용자 프로필 정보 전체 업데이트 (realName, address, phoneNumber, sensitiveInfo)
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileUpdateResponse>> updateUserProfile(
            @AuthenticationPrincipal Account account,
            @RequestBody UserProfileUpdateRequest request,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        logger.info("Received updateUserProfile request for user: {}", user.getId());
        try {
            // 1. 사용자 기본 정보 업데이트
            userService.updatePersonalInfo(user, request.getRealName(), request.getAddress());
            userService.updatePhoneNumber(user, request.getPhoneNumber());
            
            // 2. 민감정보 업데이트
            UserSensitiveInfoDto sensitiveInfo = userSensitiveInfoService.saveOrUpdateSensitiveInfo(
                    user, request.getAllergyInfo(), request.getChronicDiseaseInfo(), request.getSurgeryHistoryInfo());

            // 3. DB에서 민감정보(문진표용) 조회 (없으면 빈 리스트)
            UserSensitiveInfoDto infoForQuestionnaire = userSensitiveInfoService.getSensitiveInfo(user);
            List<PatientQuestionnaireRequest.InfoItem> allergyInfo = toInfoItemList(
                infoForQuestionnaire != null ? infoForQuestionnaire.getAllergyInfo() : null, "allergyName");
            List<PatientQuestionnaireRequest.InfoItem> chronicDiseaseInfo = toInfoItemList(
                infoForQuestionnaire != null ? infoForQuestionnaire.getChronicDiseaseInfo() : null, "chronicDiseaseName");
            List<PatientQuestionnaireRequest.InfoItem> surgeryHistoryInfo = toInfoItemList(
                infoForQuestionnaire != null ? infoForQuestionnaire.getSurgeryHistoryInfo() : null, "surgeryHistoryName");

            // 4. 문진표 자동 생성 (이미 있으면 예외 무시)
            // medicationInfo는 PatientQuestionnaireService.createQuestionnaire()에서 taking-pill에서 자동으로 가져옴
            try {
                com.oauth2.User.PatientQuestionnaire.Dto.PatientQuestionnaireRequest questionnaireRequest =
                    com.oauth2.User.PatientQuestionnaire.Dto.PatientQuestionnaireRequest.builder()
                        .realName(request.getRealName())
                        .address(request.getAddress())
                        .phoneNumber(request.getPhoneNumber())
                        // .questionnaireName("자동 생성 문진표")
                        // .notes("")
                        .allergyInfo(allergyInfo)
                        .chronicDiseaseInfo(chronicDiseaseInfo)
                        .surgeryHistoryInfo(surgeryHistoryInfo)
                        .build();
                if(patientQuestionnaireService.getLatestQuestionnaireByUser(user) == null) {
                    patientQuestionnaireService.createQuestionnaire(user, questionnaireRequest);
                } else {
                    patientQuestionnaireService.updateCurrentUserQuestionnaire(user, questionnaireRequest);
                }
                logger.info("문진표 자동 생성 성공 (taking-pill 정보 포함) for user: {}", user.getId());
            } catch (Exception e) {
                logger.warn("문진표 자동 생성 실패(이미 존재할 수 있음): {}", e.getMessage());
            }

            // 5. 응답 생성
            UserProfileUpdateResponse response = UserProfileUpdateResponse.builder()
                    .realName(request.getRealName())
                    .address(request.getAddress())
                    .phoneNumber(request.getPhoneNumber())
                    .sensitiveInfo(sensitiveInfo)
                    .build();
            logger.info("Successfully updated user profile for user: {}", user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success(UserInfoMessageConstants.PROFILE_UPDATE_SUCCESS, response));
        } catch (Exception e) {
            logger.error("Error updating user profile for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(UserInfoMessageConstants.PROFILE_UPDATE_FAILED + ": " + e.getMessage(), null));
        }
    }

    // Helper method to convert List<String> to List<InfoItem>
    private List<PatientQuestionnaireRequest.InfoItem> toInfoItemList(List<String> list, String key) {
        if (list == null) return java.util.Collections.emptyList();
        return list.stream()
            .map(value -> PatientQuestionnaireRequest.InfoItem.builder()
                .allergyName("allergyName".equals(key) ? value : null)
                .chronicDiseaseName("chronicDiseaseName".equals(key) ? value : null)
                .surgeryHistoryName("surgeryHistoryName".equals(key) ? value : null)
                .submitted(true)
                .build())
            .collect(Collectors.toList());
    }

    /**
     * 사용자 민감정보 조회
     */
    @GetMapping("")
    public ResponseEntity<ApiResponse<UserSensitiveInfoDto>> getSensitiveInfo(
            @AuthenticationPrincipal Account account,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        logger.info("Received getSensitiveInfo request for user: {}", user.getId());
        
        try {
            UserSensitiveInfoDto sensitiveInfo = userSensitiveInfoService.getSensitiveInfo(user);
            if (sensitiveInfo == null) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error(UserInfoMessageConstants.SENSITIVE_INFO_NOT_FOUND, null));
            }
            
            logger.info("Successfully retrieved sensitive info for user: {}", user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success(UserInfoMessageConstants.SENSITIVE_INFO_RETRIEVE_SUCCESS, sensitiveInfo));
        } catch (Exception e) {
            logger.error("Error retrieving sensitive info for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(UserInfoMessageConstants.SENSITIVE_INFO_RETRIEVE_FAILED + ": " + e.getMessage(), null));
        }
    }

    /**
     * 사용자 민감정보 생성/업데이트
     */
    @PutMapping("")
    public ResponseEntity<ApiResponse<UserSensitiveInfoDto>> updateSensitiveInfo(
            @AuthenticationPrincipal Account account,
            @RequestBody UserSensitiveInfoDto request,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        logger.info("Received updateSensitiveInfo request for user: {}", user.getId());
        
        try {
            List<String> allergyInfo = request.getAllergyInfo();
            List<String> chronicDiseaseInfo = request.getChronicDiseaseInfo();
            List<String> surgeryHistoryInfo = request.getSurgeryHistoryInfo();
            
            UserSensitiveInfoDto sensitiveInfo = userSensitiveInfoService.saveOrUpdateSensitiveInfo(
                    user, allergyInfo, chronicDiseaseInfo, surgeryHistoryInfo);
            
            logger.info("Successfully updated sensitive info for user: {}", user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success(UserInfoMessageConstants.SENSITIVE_INFO_UPDATE_SUCCESS, sensitiveInfo));
        } catch (Exception e) {
            logger.error("Error updating sensitive info for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(UserInfoMessageConstants.SENSITIVE_INFO_UPDATE_FAILED + ": " + e.getMessage(), null));
        }
    }

    /**
     * 특정 카테고리의 민감정보만 업데이트
     */
    @PutMapping("/{category}")
    public ResponseEntity<ApiResponse<UserSensitiveInfoDto>> updateSensitiveInfoCategory(
            @AuthenticationPrincipal Account account,
            @PathVariable String category,
            @RequestBody List<String> data,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        logger.info("Received updateSensitiveInfoCategory request for user: {} - Category: {}", user.getId(), category);
        
        try {
            UserSensitiveInfoDto sensitiveInfo = userSensitiveInfoService.updateSensitiveInfoCategory(user, category, data);
            
            logger.info("Successfully updated sensitive info category for user: {} - Category: {}", user.getId(), category);
            return ResponseEntity.status(200)
                .body(ApiResponse.success(UserInfoMessageConstants.SENSITIVE_INFO_CATEGORY_UPDATE_SUCCESS, sensitiveInfo));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid category for user: {} - Category: {}", user.getId(), category);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(UserInfoMessageConstants.INVALID_CATEGORY + ": " + e.getMessage(), null));
        } catch (JsonProcessingException e) {
            logger.error("Error serializing sensitive info category for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(UserInfoMessageConstants.SENSITIVE_INFO_SERIALIZE_FAILED + ": " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error updating sensitive info category for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(UserInfoMessageConstants.SENSITIVE_INFO_CATEGORY_UPDATE_FAILED + ": " + e.getMessage(), null));
        }
    }

    /**
     * 사용자 민감정보 선택적 삭제 (boolean으로 지정된 카테고리만 유지)
     */
    @DeleteMapping("")
    public ResponseEntity<ApiResponse<UserSensitiveInfoDto>> deleteSensitiveInfoCategories(
            @AuthenticationPrincipal Account account,
            @RequestBody UserSensitiveInfoDeleteRequest request,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        logger.info("Received deleteSensitiveInfoCategories request for user: {}", user.getId());
        
        try {
            UserSensitiveInfoDto sensitiveInfo = userSensitiveInfoService.deleteSensitiveInfoCategories(user, request);
            
            // 문진표 동기화 추가
            patientQuestionnaireService.syncFromSensitiveInfo(user, sensitiveInfo);
            
            if (sensitiveInfo == null) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error(UserInfoMessageConstants.SENSITIVE_INFO_NOT_FOUND, null));
            }
            
            logger.info("Successfully deleted sensitive info categories for user: {}", user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success(UserInfoMessageConstants.SENSITIVE_INFO_DELETE_SUCCESS, sensitiveInfo));
        } catch (JsonProcessingException e) {
            logger.error("Error serializing sensitive info for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(UserInfoMessageConstants.SENSITIVE_INFO_SERIALIZE_FAILED + ": " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error deleting sensitive info categories for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(UserInfoMessageConstants.SENSITIVE_INFO_DELETE_FAILED + ": " + e.getMessage(), null));
        }
    }

    /**
     * 사용자 민감정보 전체 삭제
     */
    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse<String>> deleteAllSensitiveInfo(
            @AuthenticationPrincipal Account account,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        logger.info("Received deleteAllSensitiveInfo request for user: {}", user.getId());
        
        try {
            userSensitiveInfoService.deleteAllSensitiveInfoByUser(user);
            UserSensitiveInfoDto emptyInfo = userSensitiveInfoService.saveOrUpdateSensitiveInfo(user, null, null, null);
            // 문진표 동기화 추가
            patientQuestionnaireService.syncFromSensitiveInfo(user, emptyInfo);
            logger.info("Successfully deleted all sensitive info for user: {}", user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success(UserInfoMessageConstants.SENSITIVE_INFO_ALL_DELETE_SUCCESS, "민감정보가 성공적으로 삭제되었습니다."));
        } catch (Exception e) {
            logger.error("Error deleting all sensitive info for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(UserInfoMessageConstants.SENSITIVE_INFO_ALL_DELETE_FAILED + ": " + e.getMessage(), null));
        }
    }
} 