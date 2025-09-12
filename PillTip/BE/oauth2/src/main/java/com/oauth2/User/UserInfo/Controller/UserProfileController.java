package com.oauth2.User.UserInfo.Controller;

import com.oauth2.Account.Dto.ApiResponse;
import com.oauth2.Account.Service.AccountService;
import com.oauth2.User.UserInfo.Dto.*;
import com.oauth2.Account.Entity.Account;
import com.oauth2.User.UserInfo.Entity.Gender;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.UserInfo.Entity.UserProfile;
import com.oauth2.User.UserInfo.Service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/user-profile")
@RequiredArgsConstructor
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);
    private final UserProfileService userProfileService;
    private final AccountService accountService;

    // 임신 여부 업데이트
    @PutMapping("/pregnant")
    public ResponseEntity<ApiResponse<UserProfilePregnantResponse>> updatePregnant(
            @AuthenticationPrincipal Account account,
            @RequestBody PregnantRequest request,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());
        logger.info("=== UPDATE PREGNANT START ===");
        logger.info("Received updatePregnant request for user: {}", user.getId());
        logger.info("Pregnant value: {}", request.isPregnant());

        try {
            // 현재 UserProfile 조회
            UserProfile currentProfile = userProfileService.getUserProfile(user);
            logger.info("Current user profile - Age: {}, Gender: {}, Pregnant: {}",
                    currentProfile.getAge(), currentProfile.getGender(), currentProfile.isPregnant());

            // 성별 검증
            logger.info("Checking gender validation - pregnantValue: {}, gender: {}",
                    request.isPregnant(), currentProfile.getGender());
            logger.info("Is male? {}", currentProfile.getGender() == Gender.MALE);
            logger.info("Should block? {}", request.isPregnant() && currentProfile.getGender() == Gender.MALE);

            if (request.isPregnant() && currentProfile.getGender() == Gender.MALE) {
                logger.warn("=== BLOCKED: Attempted to set pregnant=true for male user: {} (Gender: {}) ===",
                        user.getId(), currentProfile.getGender());
                return ResponseEntity.status(400)
                        .body(ApiResponse.error("남성은 임신 상태를 true로 설정할 수 없습니다.", null));
            }

            logger.info("Gender validation passed, proceeding with update...");

            UserProfile updatedProfile = userProfileService.updatePregnant(user, request.isPregnant());
            logger.info("Successfully updated pregnant status for user: {} - New pregnant value: {}",
                    user.getId(), updatedProfile.isPregnant());

            // 응답 DTO 생성
            UserProfilePregnantResponse response = UserProfilePregnantResponse.builder()
                    .age(updatedProfile.getAge())
                    .gender(updatedProfile.getGender())
                    .pregnant(updatedProfile.isPregnant())
                    .build();

            logger.info("Response DTO - Age: {}, Gender: {}, Pregnant: {}",
                    response.getAge(), response.getGender(), response.isPregnant());
            logger.info("=== UPDATE PREGNANT END ===");

            return ResponseEntity.status(200)
                    .body(ApiResponse.success("Pregnant status updated successfully", response));
        } catch (Exception e) {
            logger.error("Error updating pregnant status for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                    .body(ApiResponse.error("Failed to update pregnant status: " + e.getMessage(), null));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<UserResponse>> createProfile(
            @AuthenticationPrincipal Account account,
            @RequestBody ChildProfileRequest profileRequest) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success(userProfileService.createProfile(profileRequest, account.getId())));
    }

    @DeleteMapping("")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(
            @AuthenticationPrincipal Account account,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        userProfileService.deleteProfile(user.getId());
        return ResponseEntity.ok()
                .body(ApiResponse.success("프로필 삭제 완료",null));
    }
}
