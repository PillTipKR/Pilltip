package com.oauth2.User.UserInfo.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {
    private String realName;
    private String address;
    private String phoneNumber;
    private List<String> allergyInfo;
    private List<String> chronicDiseaseInfo;
    private List<String> surgeryHistoryInfo;
} 