// author : mireutale
// description : 복용 중인 약 상세 정보 응답 DTO
package com.oauth2.User.TakingSupplement.Dto;

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
public class TakingSupplementDetailResponse {
    private List<TakingSupplementDetail> supplementDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TakingSupplementDetail {
        private Long supplementId;                    // 약 ID
        private String supplementName;                // 약 이름
        private LocalDate startDate;                  // 복약 시작날짜
        private LocalDate endDate;                    // 복약 종료날짜
        private String alarmName;                     // 알림명
        private List<String> daysOfWeek;              // 복용 요일
        private Double dosageAmount;                  // 복용량
        private List<SupplementScheduleDetail> supplementSchedules; // 복용 스케줄
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplementScheduleDetail {
        private Integer hour;           // 시간 (0-12)
        private Integer minute;         // 분 (0-59)
        private String period;          // 기간 (AM/PM)
        private String dosageUnit;      // 복용 단위
        private boolean alarmOnOff;     // 알림 여부
    }
} 