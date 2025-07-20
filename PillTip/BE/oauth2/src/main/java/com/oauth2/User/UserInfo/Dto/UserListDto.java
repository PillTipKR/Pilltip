package com.oauth2.User.UserInfo.Dto;

public record UserListDto(
        Long userId,
        String nickname,
        boolean isMain,
        boolean isSelected
)
{}
