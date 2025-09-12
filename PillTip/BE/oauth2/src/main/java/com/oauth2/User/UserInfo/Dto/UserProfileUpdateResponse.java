package com.oauth2.User.UserInfo.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateResponse {
    private String realName;
    private String address;
    private String phoneNumber;
    private UserSensitiveInfoDto sensitiveInfo;
} 