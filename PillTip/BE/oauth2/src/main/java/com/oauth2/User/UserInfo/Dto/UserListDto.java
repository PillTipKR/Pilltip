package com.oauth2.User.UserInfo.Dto;

public record UserListDto(
        Long userId,
        String nickname,
        Integer age,
        String gender,
        String birthDate,
        boolean isMain,
        boolean isSelected
)
{}
