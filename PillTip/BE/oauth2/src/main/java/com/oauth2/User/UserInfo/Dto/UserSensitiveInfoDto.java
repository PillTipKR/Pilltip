// author : mireutale
// description : 사용자 민감정보 DTO
package com.oauth2.User.UserInfo.Dto;

import com.oauth2.User.UserInfo.Entity.UserSensitiveInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSensitiveInfoDto {
    private List<String> allergyInfo;
    private List<String> chronicDiseaseInfo;
    private List<String> surgeryHistoryInfo;

    public static UserSensitiveInfoDto from(UserSensitiveInfo entity) {
        try {
            return UserSensitiveInfoDto.builder()
                    .allergyInfo(parseCommaSeparatedToList(entity.getAllergyInfo()))
                    .chronicDiseaseInfo(parseCommaSeparatedToList(entity.getChronicDiseaseInfo()))
                    .surgeryHistoryInfo(parseCommaSeparatedToList(entity.getSurgeryHistoryInfo()))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert UserSensitiveInfo to DTO", e);
        }
    }
    
    private static List<String> parseCommaSeparatedToList(String commaSeparated) {
        if (commaSeparated == null || commaSeparated.trim().isEmpty()) {
            return List.of();
        }
        
        String[] items = commaSeparated.split(",");
        List<String> result = new java.util.ArrayList<>();
        for (String item : items) {
            String trimmedItem = item.trim();
            if (!trimmedItem.isEmpty()) {
                result.add(trimmedItem);
            }
        }
        return result;
    }
} 