package com.oauth2.User.UserInfo.Controller;

import com.oauth2.Account.Service.AccountService;
import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Dto.*;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.UserInfo.Dto.*;
import com.oauth2.User.UserInfo.Service.UserService;
// import com.oauth2.User.UserInfo.Dto.ProfilePhotoRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final AccountService accountService;
    private final UserService userService;

    // 현재 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AllUserResponse>> getCurrentUser(
            @AuthenticationPrincipal Account account,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        try {
            if(profileId==0) profileId=user.getId();
            User currentUser = userService.getCurrentUser(user.getId());
            List<UserListDto> userListDtos = accountService.getUserList(account.getId(),profileId);

            return ResponseEntity.status(200)
                    .body(ApiResponse.success(
                            new AllUserResponse(currentUser, userListDtos)));
        } catch (Exception e) {
            logger.error("Get current user failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(UserInfoMessageConstants.GET_CURRENT_USER_FAILED, null));
        }
    }

    // 실명과 주소 업데이트
    @PutMapping("/personal-info")
    public ResponseEntity<ApiResponse<UserResponse>> updatePersonalInfo(
            @AuthenticationPrincipal Account account,
            @RequestBody PersonalInfoRequest request,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        try {
            User updatedUser = userService.updatePersonalInfo(user, request.getRealName(), request.getAddress());
            return ResponseEntity.status(200)
                    .body(ApiResponse.success(UserInfoMessageConstants.PERSONAL_INFO_UPDATE_SUCCESS, new UserResponse(updatedUser)));
        } catch (Exception e) {
            logger.error("Personal info update failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(UserInfoMessageConstants.PERSONAL_INFO_UPDATE_FAILED, null));
        }
    }

    // 닉네임 업데이트
    @PutMapping("/nickname")
    public ResponseEntity<ApiResponse<UserResponse>> updateNickname(
            @AuthenticationPrincipal Account account,
            @RequestParam String nickname,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());


        try {
            String cleanedNickname = nickname.trim();
            
            if (cleanedNickname.isEmpty()) {
                return ResponseEntity.status(400)
                        .body(ApiResponse.error(UserInfoMessageConstants.NICKNAME_EMPTY, null));
            }

            User updatedUser = userService.updateNickname(user, cleanedNickname);
            return ResponseEntity.status(200)
                    .body(ApiResponse.success(UserInfoMessageConstants.NICKNAME_UPDATE_SUCCESS, new UserResponse(updatedUser)));
        } catch (Exception e) {
            logger.error("Nickname update failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(UserInfoMessageConstants.NICKNAME_UPDATE_FAILED, null));
        }
    }

    @PutMapping("/profile-photo")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfilePhoto(
            @AuthenticationPrincipal Account account,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());


        try {
            String uploadDir = System.getProperty("user.dir") + "/upload/profile/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    throw new RuntimeException(UserInfoMessageConstants.PROFILE_PHOTO_DIR_CREATE_FAILED);
                }
            }

            // 기존 프로필 사진 삭제
            if (user.getProfilePhoto() != null && !user.getProfilePhoto().isEmpty()) {
                try {
                    String oldFileName = user.getProfilePhoto().substring(user.getProfilePhoto().lastIndexOf("/") + 1);
                    File oldFile = new File(uploadDir, oldFileName);
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                } catch (Exception e) {
                    logger.warn("Could not delete old profile photo: {}", e.getMessage());
                }
            }

            String fileName = "profile_" + user.getId() + "_" + System.currentTimeMillis() + ".jpg";
            File dest = new File(dir, fileName);
            file.transferTo(dest);

            String fileUrl = "/profile/" + fileName;
            User updatedUser = userService.updateProfilePhoto(user, fileUrl);

            return ResponseEntity.status(200)
                    .body(ApiResponse.success(UserInfoMessageConstants.PROFILE_PHOTO_UPDATE_SUCCESS, new UserResponse(updatedUser)));
        } catch (IOException e) {
            logger.error("Profile photo update failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(UserInfoMessageConstants.PROFILE_PHOTO_UPDATE_FAILED, null));
        }
    }

    // 회원 탈퇴
    @DeleteMapping("/delete-account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal Account account){

        try {
            userService.deleteAccount(account.getId());
            return ResponseEntity.status(200)
                    .body(ApiResponse.success(UserInfoMessageConstants.ACCOUNT_DELETE_SUCCESS, null));
        } catch (Exception e) {
            logger.error("Account delete failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(UserInfoMessageConstants.ACCOUNT_DELETE_FAILED, null));
        }
    }
}
