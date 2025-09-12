package com.oauth2.User.UserInfo.Dto;

import com.oauth2.User.UserInfo.Entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfilePregnantResponse {
    private Integer age;
    private Gender gender;
    private boolean pregnant;
} 