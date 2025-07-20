package com.oauth2.User.UserInfo.Dto;


public record ChildProfileRequest(
        String nickname,        // UserProfile 엔티티의 필드와 매핑
        String gender,
        Integer age,
        String birthDate,  // YYYY-MM-DD
        Integer height,
        Integer weight
) {}
