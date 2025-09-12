// author : mireutale
// description : 복용 중인 약 정보 요약 응답 DTO
package com.oauth2.User.TakingPill.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TakingPillSummaryResponse {
    private List<TakingPillSummary> takingPills;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TakingPillSummary {
        private Long medicationId;        // 약 ID
        private String medicationName;    // 약 이름
        private String alarmName;         // 알림명
        private LocalDate startDate;      // 복약 시작날짜
        private LocalDate endDate;        // 복약 종료날짜
        private Double dosageAmount;      // 복용량
    }
} 