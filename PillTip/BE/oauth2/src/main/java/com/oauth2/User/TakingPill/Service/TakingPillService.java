package com.oauth2.User.TakingPill.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.User.TakingPill.Dto.TakingPillRequest;
import com.oauth2.User.TakingPill.Dto.TakingPillSummaryResponse;
import com.oauth2.User.TakingPill.Dto.TakingPillDetailResponse;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.TakingPill.Entity.*;
import com.oauth2.User.TakingPill.Repositoty.DosageLogRepository;
import com.oauth2.User.TakingPill.Repositoty.TakingPillCounterRepository;
import com.oauth2.User.TakingPill.Repositoty.TakingPillRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.oauth2.User.TakingPill.Entity.PillStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class TakingPillService {
    private static final Logger logger = LoggerFactory.getLogger(TakingPillService.class);
    private final TakingPillRepository takingPillRepository;
    private final DosageLogRepository dosageLogRepository;
    private final TakingPillCounterRepository takingPillCounterRepository;
    private final DrugRepository drugRepository;
    private final ObjectMapper objectMapper;

    /**
     * 복용 중인 약을 추가합니다.
     */
    public TakingPill addTakingPill(User user, TakingPillRequest request) {
        // 요청 데이터 검증
        validateTakingPillRequest(request);        
        // TakingPill 엔티티 생성
        TakingPill takingPill = buildTakingPill(request,
                TakingPill.builder()
                        .user(user)
                        .build());

        // DosageSchedule 엔티티들 생성 및 저장
        if (request.getDosageSchedules() != null){
            for (TakingPillRequest.DosageSchedule scheduleRequest : request.getDosageSchedules()) {
                DosageSchedule dosageSchedule = getDosageSchedule(scheduleRequest,takingPill);

                // 양방향 연관관계 유지
                takingPill.getDosageSchedules().add(dosageSchedule);
            }
        }
        TakingPill savedTakingPill = takingPillRepository.save(takingPill);

        List<DosageSchedule> schedules = savedTakingPill.getDosageSchedules();
        List<DosageLog> dosageLogs = savedTakingPill.getDosageLogs();
        if (schedules != null && !schedules.isEmpty()
                && request.getStartDate() != null && request.getEndDate() != null) {
            LocalDate date = request.getStartDate();
            while (!date.isAfter(request.getEndDate())) {
                if (matchesToday(savedTakingPill, date)) {
                    for (DosageSchedule schedule : schedules) {
                        DosageLog dosageLog = DosageLog.builder()
                                .takingPill(savedTakingPill)
                                .scheduledTime(LocalDateTime.of(date,
                                        LocalTime.of(to24Hour(schedule.getHour(),schedule.getPeriod()), schedule.getMinute())))
                                .user(user)
                                .alarmName(request.getAlarmName())
                                .medicationName(request.getMedicationName())
                                .visible(schedule.getAlarmOnOff())
                                .build();
                        dosageLogs.add(dosageLog);
                    }
                }
                date = date.plusDays(1);
            }

            dosageLogRepository.saveAll(dosageLogs);
        }

        TakingPillCounter takingPillCounter = takingPillCounterRepository.findByDrugId(request.getMedicationId()).orElse(null);
        if (takingPillCounter == null) {
            takingPillCounter = new TakingPillCounter();
            takingPillCounter.setDrug(drugRepository.findById(request.getMedicationId()).orElse(null));
            takingPillCounter.setCount(1);
        }else takingPillCounter.setCount(takingPillCounter.getCount()+1);

        takingPillCounterRepository.save(takingPillCounter);

        return savedTakingPill;
    }

    /**
     * 복용 중인 약을 삭제합니다.
     */
    public void deleteTakingPill(User user, String medicationId) {
        Long medId = Long.parseLong(medicationId);
        List<TakingPill> takingPills = takingPillRepository.findByUserAndMedicationId(user, medId);

        if (takingPills.isEmpty()) {
            throw new RuntimeException("해당 약품을 찾을 수 없습니다.");
        }

        TakingPill takingPill = takingPills.get(0);

        LocalDateTime now = LocalDateTime.now();

        // 복약 기간 계산
        LocalDate endDate = createSafeLocalDate(
                takingPill.getEndYear(), takingPill.getEndMonth(), takingPill.getEndDay());

        // 복약 상태 판단
        PillStatus status = PillStatus.calculateStatus(endDate, takingPill.getCreatedAt());

        if (status == COMPLETED) {
            throw new IllegalStateException("이미 종료된 복약 기록은 수정할 수 없습니다.");
        }

        // 연관된 DosageLog 삭제 정책에 따라 처리
        List<DosageLog> logs = dosageLogRepository.findByUserAndTakingPill(user, takingPill);

        if (status == NEW) {
            // 전체 삭제
            dosageLogRepository.deleteAll(logs);
        } else if (status == ACTIVE) {
            List<DosageLog> futureLogs = logs.stream()
                    .filter(log -> !log.getScheduledTime().isBefore(now)) // now 포함 이후
                    .collect(Collectors.toList());

            dosageLogRepository.deleteAll(futureLogs);
        }

        // TakingPill 삭제 (cascade로 DosageSchedule도 삭제됨)
        takingPillRepository.delete(takingPill);
    }

    public TakingPill updateTakingPill(User user, TakingPillRequest request) {
        // 요청 데이터 검증
        validateTakingPillRequest(request);
        
        // 기존 TakingPill 찾기 (트랜잭션 격리 문제 해결을 위해 직접 조회)
        List<TakingPill> existingPills = takingPillRepository.findByUserAndMedicationId(user, request.getMedicationId());
        
        if (existingPills.isEmpty()) {
            throw new RuntimeException("수정할 약품을 찾을 수 없습니다.");
        }
        
        TakingPill takingPill = existingPills.get(0);

        // === 복약 로그 동기화 ===
        LocalDate oldStartDate = createSafeLocalDate(takingPill.getStartYear(), takingPill.getStartMonth(), takingPill.getStartDay());
        LocalDate oldEndDate = createSafeLocalDate(takingPill.getEndYear(), takingPill.getEndMonth(), takingPill.getEndDay());

        // 기존 로그 조회
        List<DosageLog> existingLogs = dosageLogRepository.findByUserAndTakingPill(user, takingPill);
        // 깊은 복사
        List<DosageSchedule> oldSchedules = takingPill.getDosageSchedules()
                .stream()
                .map(schedule -> DosageSchedule.builder()
                        .hour(schedule.getHour())
                        .minute(schedule.getMinute())
                        .period(schedule.getPeriod())
                        .alarmOnOff(schedule.getAlarmOnOff())
                        .dosageUnit(schedule.getDosageUnit())
                        .build()
                ).toList();
        List<String> oldDaysOfWeek = parseDaysOfWeekFromJson(takingPill.getDaysOfWeek());

        // TakingPill 정보 업데이트
        TakingPill updatedTakingPill = buildTakingPill(request,takingPill);
        
        // 기존 DosageSchedule 리스트를 클리어하고 새로운 스케줄로 교체
        updatedTakingPill.getDosageSchedules().clear();
        
        // 새로운 DosageSchedule 생성 및 추가
        if (request.getDosageSchedules() != null) {
            for (TakingPillRequest.DosageSchedule scheduleRequest : request.getDosageSchedules()) {
                DosageSchedule dosageSchedule = getDosageSchedule(scheduleRequest,takingPill);
                
                takingPill.getDosageSchedules().add(dosageSchedule);
            }
        }
        // TakingPill과 연관된 DosageSchedule들을 함께 저장
        updatedTakingPill = takingPillRepository.save(takingPill);
        PillStatus pillStatus = PillStatus.calculateStatus(oldEndDate,takingPill.getCreatedAt());

        for(DosageLog dosageLog : existingLogs){
            if (!dosageLog.getScheduledTime().isAfter(LocalDateTime.now()) && (pillStatus != NEW)) continue; // 과거 로그는 스킵
            LocalTime logTime = dosageLog.getScheduledTime().toLocalTime();

            // logTime이 어떤 DosageSchedule에 해당하는지 확인
            for (DosageSchedule schedule : updatedTakingPill.getDosageSchedules()) {
                LocalTime scheduleTime = LocalTime.of(
                        to24Hour(schedule.getHour(), schedule.getPeriod()),
                        schedule.getMinute()
                );

                if (logTime.equals(scheduleTime)) {
                    System.out.println("알람 수정" + dosageLog.getScheduledTime() + " "+ schedule.getAlarmOnOff());
                    dosageLog.setVisible(schedule.getAlarmOnOff());
                    break;
                }
            }
        }
        dosageLogRepository.saveAll(existingLogs);

        LocalDate newStartDate = request.getStartDate();
        LocalDate newEndDate = request.getEndDate();

        // 복약 상태 판단
        takingPill.setCreatedAt(takingPill.getCreatedAt() != null ? takingPill.getCreatedAt() : LocalDateTime.now());

        // 날짜 및 스케줄 변화 감지
        boolean isStartDateEarlier = newStartDate.isBefore(oldStartDate);
        boolean isStartDatePushed = newStartDate.isAfter(oldStartDate);
        boolean isEndDateExtended = newEndDate.isAfter(oldEndDate);
        boolean isEndDateShortened = newEndDate.isBefore(oldEndDate);
        boolean isScheduleChanged = !isScheduleEqual(oldSchedules, request.getDosageSchedules(),oldDaysOfWeek,request.getDaysOfWeek());

        // 상태 기반 처리
        handleDosageLogsOnUpdate(
                user,
                updatedTakingPill,
                existingLogs,
                pillStatus,
                oldStartDate,
                oldEndDate,
                isStartDateEarlier,
                isStartDatePushed,
                isEndDateExtended,
                isEndDateShortened,
                isScheduleChanged
        );

        return updatedTakingPill;
    }

    private TakingPill buildTakingPill(TakingPillRequest request, TakingPill takingPill) {
        takingPill.setMedicationId(request.getMedicationId());
        takingPill.setMedicationName(request.getMedicationName());
        takingPill.setStartYear(request.getStartDate() != null ? request.getStartDate().getYear() : null);
        takingPill.setStartMonth(request.getStartDate() != null ? request.getStartDate().getMonthValue() : null);
        takingPill.setStartDay(request.getStartDate() != null ? request.getStartDate().getDayOfMonth() : null);
        takingPill.setEndYear(request.getEndDate() != null ? request.getEndDate().getYear() : null);
        takingPill.setEndMonth(request.getEndDate() != null ? request.getEndDate().getMonthValue() : null);
        takingPill.setEndDay(request.getEndDate() != null ? request.getEndDate().getDayOfMonth() : null);
        takingPill.setAlarmName(request.getAlarmName());
        takingPill.setDaysOfWeek(convertDaysOfWeekToJson(request.getDaysOfWeek()));
        takingPill.setDosageAmount(request.getDosageAmount());

        return takingPill;

    }

    private DosageSchedule getDosageSchedule(TakingPillRequest.DosageSchedule scheduleRequest, TakingPill savedTakingPill) {
        return DosageSchedule.builder()
                .takingPill(savedTakingPill)
                .hour(scheduleRequest.getHour())
                .minute(scheduleRequest.getMinute())
                .period(scheduleRequest.getPeriod())
                .alarmOnOff(scheduleRequest.isAlarmOnOff())
                .dosageUnit(scheduleRequest.getDosageUnit())
                .build();
    }
    /**
     * 복용 중인 약 정보를 요약 형태로 반환합니다.
     */
    public TakingPillSummaryResponse getTakingPillSummary(User user) {
        List<TakingPill> takingPills = takingPillRepository.findByUser(user);
        
        List<TakingPillSummaryResponse.TakingPillSummary> summaries = takingPills.stream()
                .map(pill -> TakingPillSummaryResponse.TakingPillSummary.builder()
                        .medicationId(pill.getMedicationId())
                        .medicationName(getDecryptedMedicationName(pill))
                        .alarmName(getDecryptedAlarmName(pill))
                        .startDate(createSafeLocalDate(pill.getStartYear(), pill.getStartMonth(), pill.getStartDay()))
                        .endDate(createSafeLocalDate(pill.getEndYear(), pill.getEndMonth(), pill.getEndDay()))
                        .dosageAmount(pill.getDosageAmount())
                        .build())
                .collect(Collectors.toList());
        
        return TakingPillSummaryResponse.builder()
                .takingPills(summaries)
                .build();
    }

    /**
     * 복용 중인 약 정보를 상세 형태로 반환합니다.
     */
    public TakingPillDetailResponse getTakingPillDetail(User user) {
        List<TakingPill> takingPills = takingPillRepository.findByUserWithDosageSchedules(user);
        
        List<TakingPillDetailResponse.TakingPillDetail> details = takingPills.stream()
                .map(this::convertToDetailResponse)
                .collect(Collectors.toList());
        
        return TakingPillDetailResponse.builder()
                .takingPills(details)
                .build();
    }

    /**
     * 특정 약의 상세 정보를 조회합니다.
     */
    public TakingPillDetailResponse.TakingPillDetail getTakingPillDetailById(User user, Long medicationId) {
        List<TakingPill> takingPills = takingPillRepository.findByUserAndMedicationId(user, medicationId);
        
        if (takingPills.isEmpty()) {
            throw new RuntimeException("Medication not found with ID: " + medicationId);
        }
        
        TakingPill takingPill = takingPills.get(0);
        return convertToDetailResponse(takingPill);
    }

    /**
     * 특정 사용자의 모든 복용 중인 약을 조회합니다.
     */
    public List<TakingPill> getTakingPillsByUser(User user) {
        return takingPillRepository.findByUserWithDosageSchedules(user);
    }

    /**
     * TakingPill 엔티티를 DetailResponse로 변환합니다.
     */
    private TakingPillDetailResponse.TakingPillDetail convertToDetailResponse(TakingPill takingPill) {
        return TakingPillDetailResponse.TakingPillDetail.builder()
                .medicationId(takingPill.getMedicationId())
                .medicationName(getDecryptedMedicationName(takingPill))
                .startDate(createSafeLocalDate(takingPill.getStartYear(), takingPill.getStartMonth(), takingPill.getStartDay()))
                .endDate(createSafeLocalDate(takingPill.getEndYear(), takingPill.getEndMonth(), takingPill.getEndDay()))
                .alarmName(getDecryptedAlarmName(takingPill))
                .daysOfWeek(parseDaysOfWeekFromJson(getDecryptedDaysOfWeek(takingPill)))
                .dosageAmount(takingPill.getDosageAmount())
                .dosageSchedules(takingPill.getDosageSchedules().stream()
                        .map(schedule -> TakingPillDetailResponse.DosageScheduleDetail.builder()
                                .hour(schedule.getHour())
                                .minute(schedule.getMinute())
                                .period(schedule.getPeriod())
                                .dosageUnit(schedule.getDosageUnit())
                                .alarmOnOff(schedule.getAlarmOnOff())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * 약물명을 가져옵니다 (EncryptionConverter가 자동으로 복호화).
     */
    private String getDecryptedMedicationName(TakingPill takingPill) {
        return takingPill.getMedicationName();
    }

    /**
     * 알림명을 가져옵니다 (EncryptionConverter가 자동으로 복호화).
     */
    private String getDecryptedAlarmName(TakingPill takingPill) {
        return takingPill.getAlarmName();
    }

    /**
     * 요일 정보를 가져옵니다 (EncryptionConverter가 자동으로 복호화).
     */
    private String getDecryptedDaysOfWeek(TakingPill takingPill) {
        return takingPill.getDaysOfWeek();
    }

    /**
     * 요일 리스트를 JSON 문자열로 변환합니다.
     */
    private String convertDaysOfWeekToJson(List<String> daysOfWeek) {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(daysOfWeek);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert days of week to JSON", e);
        }
    }

    /**
     * JSON 문자열을 요일 리스트로 변환합니다.
     */
    private List<String> parseDaysOfWeekFromJson(String daysOfWeekJson) {
        if (daysOfWeekJson == null || daysOfWeekJson.isEmpty()) {
            return List.of();
        }
        
        try {
            return objectMapper.readValue(daysOfWeekJson, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse days of week from JSON", e);
        }
    }

    /**
     * 안전한 LocalDate 생성 메서드
     */
    private LocalDate createSafeLocalDate(Integer year, Integer month, Integer day) {
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

    /**
     * TakingPill이 오늘 복용해야 하는지 확인
     */
    public boolean matchesToday(TakingPill takingPill, LocalDate today) {
        // 복용 기간 확인 - 년, 월, 일로 분리된 필드에서 LocalDate 생성
        LocalDate startDate = createSafeLocalDate(takingPill.getStartYear(), takingPill.getStartMonth(), takingPill.getStartDay());
        LocalDate endDate = createSafeLocalDate(takingPill.getEndYear(), takingPill.getEndMonth(), takingPill.getEndDay());
        //System.out.println(startDate + " " + endDate + " "+ today);
        //System.out.println("daysOfWeek raw: " + takingPill.getDaysOfWeek());

        if (startDate.isAfter(today) || endDate.isBefore(today)) {
            return false;
        }

        // 요일 확인
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> daysOfWeek = mapper.readValue(takingPill.getDaysOfWeek(),
                    new TypeReference<>() {
                    });

            // 매일 복용인 경우
            if (daysOfWeek.contains("EVERYDAY")) {
                return true;
            }

            // 특정 요일 복용인 경우
            String todayOfWeek = today.getDayOfWeek().name().substring(0, 3); // MON, TUE, WED, etc.
            return daysOfWeek.contains(todayOfWeek);

        } catch (JsonProcessingException e) {
            // JSON 파싱 실패 시 false 반환
            System.out.println("파싱실패");
            return false;
        }
    }

    public int to24Hour(Integer hour, String period) {
        if (hour == 12) hour = 0;
        return "PM".equalsIgnoreCase(period) ? hour + 12 : hour;
    }

    /**
     * 요청 데이터 검증 메서드
     */
    private void validateTakingPillRequest(TakingPillRequest request) {
        if (request.getMedicationId() == null) {
            throw new RuntimeException("약품 ID는 필수입니다.");
        }
        
        if (request.getMedicationName() == null || request.getMedicationName().trim().isEmpty()) {
            throw new RuntimeException("약품 이름은 필수입니다.");
        }
        
        // startDate와 endDate가 모두 null이거나 모두 설정되어야 함
        if ((request.getStartDate() == null) != (request.getEndDate() == null)) {
            throw new RuntimeException("시작일과 종료일은 모두 설정되거나 모두 null이어야 합니다.");
        }
        
        // startDate와 endDate가 모두 설정된 경우에만 날짜 검증
        if (request.getStartDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new RuntimeException("시작일은 종료일보다 이전이어야 합니다.");
            }
        }
        
        // alarmName은 null 허용
        
        // daysOfWeek는 null 허용
        
        // dosageSchedules는 null 허용 (빈 배열도 허용)
        
        // 복용 스케줄이 있는 경우에만 검증
        if (request.getDosageSchedules() != null && !request.getDosageSchedules().isEmpty()) {
            for (TakingPillRequest.DosageSchedule schedule : request.getDosageSchedules()) {
                if (!schedule.isValidHour()) {
                    throw new RuntimeException("시간은 0-12 사이의 값이어야 합니다.");
                }
                if (!schedule.isValidMinute()) {
                    throw new RuntimeException("분은 0-59 사이의 값이어야 합니다.");
                }
                if (!schedule.isValidPeriod()) {
                    throw new RuntimeException("기간은 AM 또는 PM이어야 합니다.");
                }
            }
        }
    }

    private void handleDosageLogsOnUpdate(
            User user,
            TakingPill pill,
            List<DosageLog> existingLogs,
            PillStatus status,
            LocalDate oldStart,
            LocalDate oldEnd,
            boolean isStartDateEarlier,
            boolean isStartDatePushed,
            boolean isEndDateExtended,
            boolean isEndDateShortened,
            boolean isScheduleChanged
    ) {
        LocalDateTime now = LocalDateTime.now();
        String medicationName = pill.getMedicationName();
        List<DosageSchedule> schedules = pill.getDosageSchedules();
        List<LocalDateTime> existingTimes = extractScheduledTimes(existingLogs);

        LocalDate startDate = pillStartDate(pill);
        LocalDate endDate = pillEndDate(pill);

        if (status == COMPLETED) return;

        if (isStartDateEarlier && !(isScheduleChanged && status == NEW) && status != ACTIVE) {
            List<DosageLog> backfill = generateDosageLogsBetween(
                    user, medicationName, pill.getAlarmName(),
                    startDate, oldStart.minusDays(1),
                    schedules, pill, existingTimes
            );
            dosageLogRepository.saveAll(backfill);
        }

        if (isStartDatePushed && status == NEW) {
            dosageLogRepository.deleteAll(
                    existingLogs.stream()
                            .filter(log -> log.getScheduledTime().toLocalDate().isBefore(startDate))
                            .collect(Collectors.toList())
            );
        }

        if (isEndDateExtended && !isScheduleChanged) {
            List<DosageLog> futureLogs = generateDosageLogsBetween(
                    user, medicationName, pill.getAlarmName(),
                    oldEnd.plusDays(1), endDate,
                    schedules, pill, existingTimes
            );
            dosageLogRepository.saveAll(futureLogs);
        }

        if (isEndDateShortened) {
            dosageLogRepository.deleteAll(
                    existingLogs.stream()
                            .filter(log -> log.getScheduledTime().toLocalDate().isAfter(endDate))
                            .collect(Collectors.toList())
            );
        }

        if (isScheduleChanged) {
            if (status == NEW) {
                dosageLogRepository.deleteAll(existingLogs);

                List<DosageLog> regenerated = generateDosageLogsBetween(
                        user, medicationName, pill.getAlarmName(),
                        startDate, endDate,
                        schedules, pill, List.of()  // NEW는 전체 삭제니까 기존 로그 없음
                );
                dosageLogRepository.saveAll(regenerated);
            } else if (status == ACTIVE) {
                List<DosageLog> futureLogsToRemove = existingLogs.stream()
                        .filter(log -> !log.getIsTaken() && !log.getScheduledTime().isBefore(now))
                        .collect(Collectors.toList());

                dosageLogRepository.deleteAll(futureLogsToRemove);

                List<LocalDateTime> futureLogTimes = extractScheduledTimes(existingLogs);

                List<DosageLog> regenerated = generateDosageLogsBetween(
                        user, medicationName, pill.getAlarmName(),
                        now.toLocalDate(), endDate,
                        schedules, pill, futureLogTimes
                );
                dosageLogRepository.saveAll(regenerated);
            }
        }
    }


    private List<LocalDateTime> extractScheduledTimes(List<DosageLog> logs) {
        return logs.stream()
                .map(DosageLog::getScheduledTime)
                .collect(Collectors.toList());
    }

    private List<DosageLog> generateDosageLogsBetween(
            User user,
            String medicationName,
            String alarmName,
            LocalDate from,
            LocalDate to,
            List<DosageSchedule> schedules,
            TakingPill pill,
            List<LocalDateTime> existingTimes
    ) {
        List<DosageLog> logs = new ArrayList<>();

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            if (!matchesToday(pill, date)) continue;

            for (DosageSchedule schedule : schedules) {
                int hour = to24Hour(schedule.getHour(), schedule.getPeriod());
                LocalDateTime scheduledTime = LocalDateTime.of(date, LocalTime.of(hour, schedule.getMinute()));

                if (existingTimes.contains(scheduledTime)) continue;

                DosageLog log = DosageLog.builder()
                        .user(user)
                        .takingPill(pill)
                        .medicationName(medicationName)
                        .alarmName(alarmName)
                        .scheduledTime(scheduledTime)
                        .visible(schedule.getAlarmOnOff())
                        .build();

                logs.add(log);
            }
        }

        return logs;
    }



    private LocalDate pillStartDate(TakingPill pill) {
        return createSafeLocalDate(pill.getStartYear(), pill.getStartMonth(), pill.getStartDay());
    }

    private LocalDate pillEndDate(TakingPill pill) {
        return createSafeLocalDate(pill.getEndYear(), pill.getEndMonth(), pill.getEndDay());
    }

    private boolean isScheduleEqual(List<DosageSchedule> existing, List<TakingPillRequest.DosageSchedule> updated,
            List<String> oldDaysofWeek, List<String> newDaysofWeek) {
        if (existing.size() != updated.size()) return false;
        for (int i = 0; i < existing.size(); i++) {
            DosageSchedule e = existing.get(i);
            TakingPillRequest.DosageSchedule u = updated.get(i);

            if (!e.getHour().equals(u.getHour())) return false;
            if (!e.getMinute().equals(u.getMinute())) return false;
            if (!e.getPeriod().equalsIgnoreCase(u.getPeriod())) return false;
            if (!e.getDosageUnit().equalsIgnoreCase(u.getDosageUnit())) return false;
        }
        if(oldDaysofWeek.size() != newDaysofWeek.size()) return false;
        else{
            for(String day : oldDaysofWeek)
                if(!newDaysofWeek.contains(day)) return false;
        }
        return true;
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void removeExpiredTakingPills() {
        LocalDate today = LocalDate.now();
        takingPillRepository.deleteExpiredPills(today.getYear(), today.getMonthValue(), today.getDayOfMonth());
        logger.info("Removed expired taking pills at {}", LocalDateTime.now());
    }

} 