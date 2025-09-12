// author : mireutale
// description : 유저 동의 정보 응답 DTO
package com.oauth2.User.UserInfo.Dto;

import com.oauth2.User.UserInfo.Entity.UserPermissions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionsResponse {
    private boolean locationPermission;      // 위치 동의
    private boolean cameraPermission;        // 카메라 동의
    private boolean galleryPermission;       // 갤러리 동의
    private boolean phonePermission;         // 전화번호 동의
    private boolean smsPermission;           // 문자 동의
    private boolean filePermission;          // 파일 동의
    private boolean sensitiveInfoPermission; // 민감정보 수집 동의
    private boolean medicalInfoPermission;   // 의약품 관련 동의
    private boolean friendPermission;        // 친구 동의

    public static UserPermissionsResponse from(UserPermissions userPermissions) {
        return UserPermissionsResponse.builder()
                .locationPermission(userPermissions.isLocationPermission())
                .cameraPermission(userPermissions.isCameraPermission())
                .galleryPermission(userPermissions.isGalleryPermission())
                .phonePermission(userPermissions.isPhonePermission())
                .smsPermission(userPermissions.isSmsPermission())
                .filePermission(userPermissions.isFilePermission())
                .sensitiveInfoPermission(userPermissions.isSensitiveInfoPermission())
                .medicalInfoPermission(userPermissions.isMedicalInfoPermission())
                .friendPermission(userPermissions.isFriendPermission())
                .build();
    }
} 