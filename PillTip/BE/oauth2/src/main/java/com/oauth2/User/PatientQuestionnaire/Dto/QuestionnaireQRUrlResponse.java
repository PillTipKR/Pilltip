package com.oauth2.User.PatientQuestionnaire.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireQRUrlResponse {
    private String qrUrl;
    private LocalDateTime createDate;
    private Long expirationDate;
    private String address;
    private String gender;
    private String birthDate;
    private String realName;
    private String phoneNumber;
    private Boolean isMedication;
    private Boolean isAllergy;
    private Boolean isChronicDisease;
    private Boolean isSurgeryHistory;
    private String hospitalName;
} 