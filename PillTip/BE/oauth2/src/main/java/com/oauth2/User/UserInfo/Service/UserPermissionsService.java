// author : mireutale
// description : 유저 동의 서비스
package com.oauth2.User.UserInfo.Service;

import com.oauth2.User.UserInfo.Dto.UserPermissionsRequest;
import com.oauth2.User.UserInfo.Dto.UserPermissionsResponse;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.UserInfo.Entity.UserPermissions;
import com.oauth2.User.UserInfo.Repository.UserPermissionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserPermissionsService {
    private final UserPermissionsRepository userPermissionsRepository;

    /**
     * 현재 사용자의 동의 정보를 조회합니다.
     * @param user 현재 로그인한 사용자
     * @return 사용자의 동의 정보
     */
    public UserPermissionsResponse getUserPermissions(User user) {
        UserPermissions userPermissions = userPermissionsRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("User permissions not found"));
        
        return UserPermissionsResponse.from(userPermissions);
    }

    /**
     * 문진표 관련 동의를 수정합니다.
     * @param user 현재 로그인한 사용자
     * @param request 동의 수정 요청
     * @return 수정된 동의 정보
     */
    public UserPermissionsResponse updateMedicalPermissions(User user, UserPermissionsRequest request) {
        UserPermissions userPermissions = userPermissionsRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("User permissions not found"));
        
        // 문진표 관련 동의만 수정
        userPermissions.setSensitiveInfoPermission(request.isSensitiveInfoPermission());
        userPermissions.setMedicalInfoPermission(request.isMedicalInfoPermission());
        
        userPermissions = userPermissionsRepository.save(userPermissions);
        return UserPermissionsResponse.from(userPermissions);
    }

    /**
     * 특정 동의 항목을 수정합니다.
     * @param user 현재 로그인한 사용자
     * @param permissionType 동의 항목 타입
     * @param granted 동의 여부
     * @return 수정된 동의 정보
     */
    public UserPermissionsResponse updatePermission(User user, String permissionType, boolean granted) {
        UserPermissions userPermissions = userPermissionsRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("User permissions not found"));
        
        // 동의 항목에 따라 수정
        switch (permissionType.toLowerCase()) {
            case "location":
                userPermissions.setLocationPermission(granted);
                break;
            case "camera":
                userPermissions.setCameraPermission(granted);
                break;
            case "gallery":
                userPermissions.setGalleryPermission(granted);
                break;
            case "phone":
                userPermissions.setPhonePermission(granted);
                break;
            case "sms":
                userPermissions.setSmsPermission(granted);
                break;
            case "file":
                userPermissions.setFilePermission(granted);
                break;
            case "sensitive_info":
                userPermissions.setSensitiveInfoPermission(granted);
                break;
            case "medical_info":
                userPermissions.setMedicalInfoPermission(granted);
                break;
            case "friend":
                userPermissions.setFriendPermission(granted);
                break;
            default:
                throw new IllegalArgumentException("Invalid permission type: " + permissionType);
        }
        
        userPermissions = userPermissionsRepository.save(userPermissions);
        return UserPermissionsResponse.from(userPermissions);
    }
} 