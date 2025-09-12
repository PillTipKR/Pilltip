package com.oauth2.User.TakingPill.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.User.TakingPill.Entity.DosageSchedule;
import com.oauth2.User.TakingPill.Entity.TakingPill;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class TakingPillResponse {
    private static final Logger logger = LoggerFactory.getLogger(TakingPillResponse.class);
    
    @JsonProperty("id")
    private Long id;

    @JsonProperty("medication_id")
    private Long medicationId;

    @JsonProperty("medication_name")
    private String medicationName;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("alert_name")
    private String alertName;

    @JsonProperty("days_of_week")
    private List<String> daysOfWeek;

    @JsonProperty("dosage_schedules")
    private List<DosageScheduleResponse> dosageSchedules;

    public TakingPillResponse(TakingPill takingPill) {
        this.id = takingPill.getId();
        this.medicationId = takingPill.getMedicationId();
        this.medicationName = takingPill.getMedicationName();
        this.startDate = createSafeLocalDate(takingPill.getStartYear(), takingPill.getStartMonth(), takingPill.getStartDay());
        this.endDate = createSafeLocalDate(takingPill.getEndYear(), takingPill.getEndMonth(), takingPill.getEndDay());
        this.alertName = takingPill.getAlarmName();
        
        // JSON 문자열을 리스트로 변환
        this.daysOfWeek = parseDaysOfWeek(takingPill.getDaysOfWeek());
        
        // dosageSchedules가 null일 경우 빈 리스트로 초기화
        if (takingPill.getDosageSchedules() != null) {
            this.dosageSchedules = takingPill.getDosageSchedules().stream()
                    .map(DosageScheduleResponse::new)
                    .collect(Collectors.toList());
        } else {
            this.dosageSchedules = new ArrayList<>();
        }
    }

    /**
     * TakingPillResponse를 생성하는 정적 팩토리 메서드 (EncryptionConverter가 자동으로 복호화)
     */
    public static TakingPillResponse fromDecrypted(TakingPill takingPill) {
        TakingPillResponse response = new TakingPillResponse();
        response.id = takingPill.getId();
        response.medicationId = takingPill.getMedicationId();
        response.medicationName = takingPill.getMedicationName(); // EncryptionConverter가 자동으로 복호화
        response.startDate = createSafeLocalDate(takingPill.getStartYear(), takingPill.getStartMonth(), takingPill.getStartDay());
        response.endDate = createSafeLocalDate(takingPill.getEndYear(), takingPill.getEndMonth(), takingPill.getEndDay());
        response.alertName = takingPill.getAlarmName(); // EncryptionConverter가 자동으로 복호화
        
        // JSON 문자열을 리스트로 변환 (EncryptionConverter가 자동으로 복호화)
        response.daysOfWeek = parseDaysOfWeek(takingPill.getDaysOfWeek());
        
        // dosageSchedules가 null일 경우 빈 리스트로 초기화
        if (takingPill.getDosageSchedules() != null) {
            response.dosageSchedules = takingPill.getDosageSchedules().stream()
                    .map(DosageScheduleResponse::new)
                    .collect(Collectors.toList());
        } else {
            response.dosageSchedules = new ArrayList<>();
        }
        
        return response;
    }

    private TakingPillResponse() {
        // 기본 생성자
    }



    private static List<String> parseDaysOfWeek(String daysOfWeekJson) {
        if (daysOfWeekJson == null || daysOfWeekJson.isEmpty()) {
            return List.of();
        }
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(daysOfWeekJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // 파싱 실패 시 빈 리스트 반환
            return List.of();
        }
    }

    /**
     * 안전한 LocalDate 생성 메서드
     */
    private static LocalDate createSafeLocalDate(Integer year, Integer month, Integer day) {
        if (year == null || month == null || day == null) {
            return LocalDate.now(); // 기본값으로 오늘 날짜 반환
        }
        
        // 월이 0이거나 12보다 큰 경우 기본값으로 1월 사용
        int safeMonth = (month <= 0 || month > 12) ? 1 : month;
        
        // 일이 0이거나 31보다 큰 경우 기본값으로 1일 사용
        int safeDay = (day <= 0 || day > 31) ? 1 : day;
        
        try {
            return LocalDate.of(year, safeMonth, safeDay);
        } catch (Exception e) {
            // 날짜 생성 실패 시 오늘 날짜 반환
            return LocalDate.now();
        }
    }

    @Getter
    @Setter
    public static class DosageScheduleResponse {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("hour")
        private Integer hour;

        @JsonProperty("minute")
        private Integer minute;

        @JsonProperty("period")
        private String period;

        @JsonProperty("dosage_amount")
        private Double dosageAmount;

        @JsonProperty("dosage_unit")
        private String dosageUnit;

        public DosageScheduleResponse(DosageSchedule dosageSchedule) {
            this.id = dosageSchedule.getId();
            this.hour = dosageSchedule.getHour();
            this.minute = dosageSchedule.getMinute();
            this.period = dosageSchedule.getPeriod();
            this.dosageUnit = dosageSchedule.getDosageUnit();
        }
    }
} 