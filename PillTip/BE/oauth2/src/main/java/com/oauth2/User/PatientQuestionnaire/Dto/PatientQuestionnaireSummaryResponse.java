package com.oauth2.User.PatientQuestionnaire.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientQuestionnaireSummaryResponse {
    private Long questionnaireId;
    private String questionnaireName;
    private LocalDate issueDate;
    private LocalDate lastModifiedDate;
    
    public static PatientQuestionnaireSummaryResponse from(Long questionnaireId, String questionnaireName, LocalDate issueDate, LocalDate lastModifiedDate) {
        return PatientQuestionnaireSummaryResponse.builder()
                .questionnaireId(questionnaireId)
                .questionnaireName(questionnaireName)
                .issueDate(issueDate)
                .lastModifiedDate(lastModifiedDate)
                .build();
    }
} 