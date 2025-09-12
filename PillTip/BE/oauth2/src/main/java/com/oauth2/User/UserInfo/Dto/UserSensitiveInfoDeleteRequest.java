// author : mireutale
// description : 사용자 민감정보 삭제 요청 DTO
package com.oauth2.User.UserInfo.Dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSensitiveInfoDeleteRequest {
    private boolean keepMedicationInfo;
    private boolean keepAllergyInfo;
    private boolean keepChronicDiseaseInfo;
    private boolean keepSurgeryHistoryInfo;
} 