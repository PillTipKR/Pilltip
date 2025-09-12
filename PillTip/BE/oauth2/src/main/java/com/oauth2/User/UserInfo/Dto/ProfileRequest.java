package com.oauth2.User.UserInfo.Dto;

public record ProfileRequest(
        String nickname,        // UserProfile 엔티티의 필드와 매핑
        String gender,
        String birthDate,  // YYYY-MM-DD
        Integer age,
        Integer height,
        Integer weight,
        String phone,
        // Interests 엔티티의 필드와 매핑
        String interest   // 콤마로 구분된 관심사 문자열
) {}
