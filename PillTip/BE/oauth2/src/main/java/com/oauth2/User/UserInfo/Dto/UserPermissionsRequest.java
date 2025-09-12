// author : mireutale
// description : 유저 동의 수정 요청 DTO
package com.oauth2.User.UserInfo.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionsRequest {
    private boolean sensitiveInfoPermission; // 민감정보 수집 동의
    private boolean medicalInfoPermission;   // 의약품 관련 동의
} 