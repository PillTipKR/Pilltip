package com.oauth2.User.TakingPill.Dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import java.util.Arrays;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TakingPillRequest {
    // 요일 상수 정의
    public static final List<String> VALID_DAYS = Arrays.asList("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");
    public static final String EVERYDAY = "EVERYDAY"; // 매일 복용
    
    @JsonProperty("medicationId")
    private Long medicationId;
    
    @JsonProperty("medicationName")
    private String medicationName;
    
    @JsonProperty("startDate")
    private LocalDate startDate;
    
    @JsonProperty("endDate")
    private LocalDate endDate;
    
    @JsonProperty("alarmName")
    private String alarmName; // 알림명 (아침약, 꼭 먹기! 등)
    
    @JsonProperty("dosageAmount")
    private Double dosageAmount; // 복용량 (0.25부터 가능)

    // 복용량 검증 메서드
    @JsonIgnore
    public boolean isValidDosageAmount() {
        return dosageAmount != null && dosageAmount >= 0.25;
    }
    
    @JsonProperty("daysOfWeek")
    private List<String> daysOfWeek; // 요일 리스트 (MON, TUE, WED, THU, FRI, SAT, SUN) 또는 ["EVERYDAY"]
    
    @JsonProperty("dosageSchedules")
    private List<DosageSchedule> dosageSchedules; // 복용 스케줄 리스트
    
    // 요일 검증 메서드
    @JsonIgnore
    public boolean isValidDaysOfWeek() {
        // 요일 리스트가 비어있는 경우
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            return false;
        }
        
        // 매일 복용인 경우, 리스트의 입력이 1개이고, 그 값이 EVERYDAY인 경우
        if (daysOfWeek.size() == 1 && EVERYDAY.equals(daysOfWeek.get(0))) {
            return true;
        }
        
        // 개별 요일 선택인 경우
        return daysOfWeek.stream().allMatch(VALID_DAYS::contains);
    }
    
    // 복용 스케줄 내부 클래스
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DosageSchedule {
        // 시간대
        @JsonProperty("hour")
        private Integer hour; // 0-12
        
        @JsonProperty("minute")
        private Integer minute; // 0-59
        
        @JsonProperty("period")
        private String period; // AM/PM
        
        @JsonProperty("alarmOnOff")
        private boolean alarmOnOff; // 알림 여부
        
        @JsonProperty("dosageUnit")
        private String dosageUnit; // 복용 단위

        // 시간 검증 메서드
        @JsonIgnore
        public boolean isValidHour() {
            return hour != null && hour >= 0 && hour <= 12;
        }
        
        // 분 검증 메서드
        @JsonIgnore
        public boolean isValidMinute() {
            return minute != null && minute >= 0 && minute <= 59;
        }
        
        // 기간 검증 메서드
        @JsonIgnore
        public boolean isValidPeriod() {
            return "AM".equals(period) || "PM".equals(period);
        }
    }

    // 내부 구현
    public boolean matchesToday(LocalDate now) {
        if (startDate.isAfter(now) || endDate.isBefore(now)) return false;
        if (daysOfWeek.contains(EVERYDAY)) return true;
        String today = now.getDayOfWeek().name().substring(0, 3);
        return daysOfWeek.contains(today);
    }
}
