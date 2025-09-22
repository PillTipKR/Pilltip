package com.oauth2.User.TakingSupplement.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.HealthSupplement.SupplementInfo.Repository.HealthSupplementRepository;
import com.oauth2.User.TakingPill.Entity.*;
import com.oauth2.User.TakingSupplement.Dto.TakingSupplementDetailResponse;
import com.oauth2.User.TakingSupplement.Dto.TakingSupplementRequest;
import com.oauth2.User.TakingSupplement.Dto.TakingSupplementSummaryResponse;
import com.oauth2.User.TakingSupplement.Entity.SupplementLog;
import com.oauth2.User.TakingSupplement.Entity.SupplementSchedule;
import com.oauth2.User.TakingSupplement.Entity.TakingSupplement;
import com.oauth2.User.TakingSupplement.Entity.TakingSupplementCounter;
import com.oauth2.User.TakingSupplement.Repository.SupplementLogRepository;
import com.oauth2.User.TakingSupplement.Repository.TakingSupplementCounterRepository;
import com.oauth2.User.TakingSupplement.Repository.TakingSupplementRepository;
import com.oauth2.User.UserInfo.Entity.User;
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
public class TakingSupplementService {
    private static final Logger logger = LoggerFactory.getLogger(TakingSupplementService.class);
    private final TakingSupplementRepository takingSupplementRepository;
    private final SupplementLogRepository supplementLogRepository;
    private final HealthSupplementRepository healthSupplementRepository;
    private final TakingSupplementCounterRepository takingSupplementCounterRepository;
    private final ObjectMapper objectMapper;

    /**
     * 복용 중인 약을 추가합니다.
     */
    public TakingSupplement addTakingSupplement(User user, TakingSupplementRequest request) {
        // 요청 데이터 검증
        validateTakingSupplementRequest(request);
        // TakingPill 엔티티 생성
        TakingSupplement takingSupplement = buildTakingSupplement(request,
                TakingSupplement.builder()
                        .user(user)
                        .build());

        // DosageSchedule 엔티티들 생성 및 저장
        if (request.getSupplementSchedules() != null){
            for (TakingSupplementRequest.SupplementSchedule scheduleRequest : request.getSupplementSchedules()) {
                SupplementSchedule dosageSchedule = getDosageSchedule(scheduleRequest,takingSupplement);

                // 양방향 연관관계 유지
                takingSupplement.getSupplementSchedules().add(dosageSchedule);
            }
        }
        TakingSupplement savedTakingSupplement = takingSupplementRepository.save(takingSupplement);

        List<SupplementSchedule> schedules = savedTakingSupplement.getSupplementSchedules();
        List<SupplementLog> supplementLogs = savedTakingSupplement.getSupplementLogs();
        if (schedules != null && !schedules.isEmpty()
                && request.getStartDate() != null && request.getEndDate() != null) {
            LocalDate date = request.getStartDate();
            while (!date.isAfter(request.getEndDate())) {
                if (matchesToday(savedTakingSupplement, date)) {
                    for (SupplementSchedule schedule : schedules) {
                        SupplementLog supplementLog = SupplementLog.builder()
                                .takingSupplement(savedTakingSupplement)
                                .scheduledTime(LocalDateTime.of(date,
                                        LocalTime.of(to24Hour(schedule.getHour(),schedule.getPeriod()), schedule.getMinute())))
                                .user(user)
                                .alarmName(request.getAlarmName())
                                .supplementName(request.getSupplementName())
                                .visible(schedule.getAlarmOnOff())
                                .build();
                        supplementLogs.add(supplementLog);
                    }
                }
                date = date.plusDays(1);
            }

            supplementLogRepository.saveAll(supplementLogs);
        }

        TakingSupplementCounter takingSupplementCounter = takingSupplementCounterRepository.findBySupplementId(request.getSupplementId()).orElse(null);
        if (takingSupplementCounter == null) {
            takingSupplementCounter = new TakingSupplementCounter();
            takingSupplementCounter.setSupplement(healthSupplementRepository.findById(request.getSupplementId()).orElse(null));
            takingSupplementCounter.setCount(1);
        }else takingSupplementCounter.setCount(takingSupplementCounter.getCount()+1);

        takingSupplementCounterRepository.save(takingSupplementCounter);

        return savedTakingSupplement;
    }

    /**
     * 복용 중인 약을 삭제합니다.
     */
    public void deleteTakingSupplement(User user, String supplementId) {
        Long medId = Long.parseLong(supplementId);
        List<TakingSupplement> takingSupplements = takingSupplementRepository.findByUserAndSupplementId(user, medId);

        if (takingSupplements.isEmpty()) {
            throw new RuntimeException("해당 약품을 찾을 수 없습니다.");
        }

        TakingSupplement takingSupplement = takingSupplements.get(0);

        LocalDateTime now = LocalDateTime.now();

        // 복약 기간 계산
        LocalDate endDate = createSafeLocalDate(
                takingSupplement.getEndYear(), takingSupplement.getEndMonth(), takingSupplement.getEndDay());

        // 복약 상태 판단
        PillStatus status = PillStatus.calculateStatus(endDate, takingSupplement.getCreatedAt());

        if (status == COMPLETED) {
            throw new IllegalStateException("이미 종료된 복약 기록은 수정할 수 없습니다.");
        }

        // 연관된 DosageLog 삭제 정책에 따라 처리
        List<SupplementLog> logs = supplementLogRepository.findByUserAndTakingSupplement(user, takingSupplement);

        if (status == NEW) {
            // 전체 삭제
            supplementLogRepository.deleteAll(logs);
        } else if (status == ACTIVE) {
            List<SupplementLog> futureLogs = logs.stream()
                    .filter(log -> !log.getScheduledTime().isBefore(now)) // now 포함 이후
                    .collect(Collectors.toList());

            supplementLogRepository.deleteAll(futureLogs);
        }

        // TakingPill 삭제 (cascade로 DosageSchedule도 삭제됨)
        takingSupplementRepository.delete(takingSupplement);
    }

    public TakingSupplement updateTakingSupplement(User user, TakingSupplementRequest request) {
        // 요청 데이터 검증
        validateTakingSupplementRequest(request);
        
        // 기존 TakingPill 찾기 (트랜잭션 격리 문제 해결을 위해 직접 조회)
        List<TakingSupplement> existingSupplements = takingSupplementRepository.findByUserAndSupplementId(user, request.getSupplementId());
        
        if (existingSupplements.isEmpty()) {
            throw new RuntimeException("수정할 약품을 찾을 수 없습니다.");
        }
        
        TakingSupplement takingSupplement = existingSupplements.get(0);

        // === 복약 로그 동기화 ===
        LocalDate oldStartDate = createSafeLocalDate(takingSupplement.getStartYear(), takingSupplement.getStartMonth(), takingSupplement.getStartDay());
        LocalDate oldEndDate = createSafeLocalDate(takingSupplement.getEndYear(), takingSupplement.getEndMonth(), takingSupplement.getEndDay());

        // 기존 로그 조회
        List<SupplementLog> existingLogs = supplementLogRepository.findByUserAndTakingSupplement(user, takingSupplement);
        // 깊은 복사
        List<SupplementSchedule> oldSchedules = takingSupplement.getSupplementSchedules()
                .stream()
                .map(schedule -> SupplementSchedule.builder()
                        .hour(schedule.getHour())
                        .minute(schedule.getMinute())
                        .period(schedule.getPeriod())
                        .alarmOnOff(schedule.getAlarmOnOff())
                        .dosageUnit(schedule.getDosageUnit())
                        .build()
                ).toList();
        List<String> oldDaysOfWeek = parseDaysOfWeekFromJson(takingSupplement.getDaysOfWeek());

        // TakingPill 정보 업데이트
        TakingSupplement updatedTakingSupplement = buildTakingSupplement(request,takingSupplement);
        
        // 기존 DosageSchedule 리스트를 클리어하고 새로운 스케줄로 교체
        updatedTakingSupplement.getSupplementSchedules().clear();
        
        // 새로운 DosageSchedule 생성 및 추가
        if (request.getSupplementSchedules() != null) {
            for (TakingSupplementRequest.SupplementSchedule scheduleRequest : request.getSupplementSchedules()) {
                SupplementSchedule dosageSchedule = getDosageSchedule(scheduleRequest,takingSupplement);
                
                takingSupplement.getSupplementSchedules().add(dosageSchedule);
            }
        }
        // TakingPill과 연관된 DosageSchedule들을 함께 저장
        updatedTakingSupplement = takingSupplementRepository.save(takingSupplement);
        PillStatus pillStatus = PillStatus.calculateStatus(oldEndDate,takingSupplement.getCreatedAt());

        for(SupplementLog supplementLog : existingLogs){
            if (!supplementLog.getScheduledTime().isAfter(LocalDateTime.now()) && (pillStatus != NEW)) continue; // 과거 로그는 스킵
            LocalTime logTime = supplementLog.getScheduledTime().toLocalTime();

            // logTime이 어떤 DosageSchedule에 해당하는지 확인
            for (SupplementSchedule schedule : updatedTakingSupplement.getSupplementSchedules()) {
                LocalTime scheduleTime = LocalTime.of(
                        to24Hour(schedule.getHour(), schedule.getPeriod()),
                        schedule.getMinute()
                );

                if (logTime.equals(scheduleTime)) {
                    System.out.println("알람 수정" + supplementLog.getScheduledTime() + " "+ schedule.getAlarmOnOff());
                    supplementLog.setVisible(schedule.getAlarmOnOff());
                    break;
                }
            }
        }
        supplementLogRepository.saveAll(existingLogs);

        LocalDate newStartDate = request.getStartDate();
        LocalDate newEndDate = request.getEndDate();

        // 복약 상태 판단
        takingSupplement.setCreatedAt(takingSupplement.getCreatedAt() != null ? takingSupplement.getCreatedAt() : LocalDateTime.now());

        // 날짜 및 스케줄 변화 감지
        boolean isStartDateEarlier = newStartDate.isBefore(oldStartDate);
        boolean isStartDatePushed = newStartDate.isAfter(oldStartDate);
        boolean isEndDateExtended = newEndDate.isAfter(oldEndDate);
        boolean isEndDateShortened = newEndDate.isBefore(oldEndDate);
        boolean isScheduleChanged = !isScheduleEqual(oldSchedules, request.getSupplementSchedules(),oldDaysOfWeek,request.getDaysOfWeek());

        // 상태 기반 처리
        handleDosageLogsOnUpdate(
                user,
                updatedTakingSupplement,
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

        return updatedTakingSupplement;
    }

    private TakingSupplement buildTakingSupplement(TakingSupplementRequest request, TakingSupplement takingSupplement) {
        takingSupplement.setSupplementId(request.getSupplementId());
        takingSupplement.setSupplementName(request.getSupplementName());
        takingSupplement.setStartYear(request.getStartDate() != null ? request.getStartDate().getYear() : null);
        takingSupplement.setStartMonth(request.getStartDate() != null ? request.getStartDate().getMonthValue() : null);
        takingSupplement.setStartDay(request.getStartDate() != null ? request.getStartDate().getDayOfMonth() : null);
        takingSupplement.setEndYear(request.getEndDate() != null ? request.getEndDate().getYear() : null);
        takingSupplement.setEndMonth(request.getEndDate() != null ? request.getEndDate().getMonthValue() : null);
        takingSupplement.setEndDay(request.getEndDate() != null ? request.getEndDate().getDayOfMonth() : null);
        takingSupplement.setAlarmName(request.getAlarmName());
        takingSupplement.setDaysOfWeek(convertDaysOfWeekToJson(request.getDaysOfWeek()));
        takingSupplement.setDosageAmount(request.getDosageAmount());

        return takingSupplement;

    }

    private SupplementSchedule getDosageSchedule(TakingSupplementRequest.SupplementSchedule scheduleRequest, TakingSupplement savingSupplement) {
        return SupplementSchedule.builder()
                .takingSupplement(savingSupplement)
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
    public TakingSupplementSummaryResponse getTakingSupplementSummary(User user) {
        List<TakingSupplement> takingSupplements = takingSupplementRepository.findByUser(user);
        
        List<TakingSupplementSummaryResponse.TakingSupplementSummary> summaries = takingSupplements.stream()
                .map(supplement -> TakingSupplementSummaryResponse.TakingSupplementSummary.builder()
                        .supplementId(supplement.getSupplementId())
                        .supplementName(getDecryptedMedicationName(supplement))
                        .alarmName(getDecryptedAlarmName(supplement))
                        .startDate(createSafeLocalDate(supplement.getStartYear(), supplement.getStartMonth(), supplement.getStartDay()))
                        .endDate(createSafeLocalDate(supplement.getEndYear(), supplement.getEndMonth(), supplement.getEndDay()))
                        .dosageAmount(supplement.getDosageAmount())
                        .build())
                .collect(Collectors.toList());
        
        return TakingSupplementSummaryResponse.builder()
                .takingSupplements(summaries)
                .build();
    }

    /**
     * 복용 중인 약 정보를 상세 형태로 반환합니다.
     */
    public TakingSupplementDetailResponse getTakingSupplementDetail(User user) {
        List<TakingSupplement> takingSupplements = takingSupplementRepository.findByUserWithDosageSchedules(user);
        
        List<TakingSupplementDetailResponse.TakingSupplementDetail> details = takingSupplements.stream()
                .map(this::convertToDetailResponse)
                .collect(Collectors.toList());
        
        return TakingSupplementDetailResponse.builder()
                .supplementDetails(details)
                .build();
    }

    /**
     * 특정 약의 상세 정보를 조회합니다.
     */
    public TakingSupplementDetailResponse.TakingSupplementDetail getTakingSupplementDetailById(User user, Long supplementId) {
        List<TakingSupplement> takingSupplements = takingSupplementRepository.findByUserAndSupplementId(user, supplementId);
        
        if (takingSupplements.isEmpty()) {
            throw new RuntimeException("Medication not found with ID: " + supplementId);
        }
        
        TakingSupplement takingSupplement = takingSupplements.get(0);
        return convertToDetailResponse(takingSupplement);
    }

    /**
     * 특정 사용자의 모든 복용 중인 약을 조회합니다.
     */
    public List<TakingSupplement> getTakingSupplementsByUser(User user) {
        return takingSupplementRepository.findByUserWithDosageSchedules(user);
    }

    /**
     * TakingPill 엔티티를 DetailResponse로 변환합니다.
     */
    private TakingSupplementDetailResponse.TakingSupplementDetail convertToDetailResponse(TakingSupplement takingSupplement) {
        return TakingSupplementDetailResponse.TakingSupplementDetail.builder()
                .supplementId(takingSupplement.getSupplementId())
                .supplementName(getDecryptedMedicationName(takingSupplement))
                .startDate(createSafeLocalDate(takingSupplement.getStartYear(), takingSupplement.getStartMonth(), takingSupplement.getStartDay()))
                .endDate(createSafeLocalDate(takingSupplement.getEndYear(), takingSupplement.getEndMonth(), takingSupplement.getEndDay()))
                .alarmName(getDecryptedAlarmName(takingSupplement))
                .daysOfWeek(parseDaysOfWeekFromJson(getDecryptedDaysOfWeek(takingSupplement)))
                .dosageAmount(takingSupplement.getDosageAmount())
                .supplementSchedules(takingSupplement.getSupplementSchedules().stream()
                        .map(schedule -> TakingSupplementDetailResponse.SupplementScheduleDetail.builder()
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
    private String getDecryptedMedicationName(TakingSupplement takingSupplement) {
        return takingSupplement.getSupplementName();
    }

    /**
     * 알림명을 가져옵니다 (EncryptionConverter가 자동으로 복호화).
     */
    private String getDecryptedAlarmName(TakingSupplement takingSupplement) {
        return takingSupplement.getAlarmName();
    }

    /**
     * 요일 정보를 가져옵니다 (EncryptionConverter가 자동으로 복호화).
     */
    private String getDecryptedDaysOfWeek(TakingSupplement takingSupplement) {
        return takingSupplement.getDaysOfWeek();
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
    public boolean matchesToday(TakingSupplement takingSupplement, LocalDate today) {
        // 복용 기간 확인 - 년, 월, 일로 분리된 필드에서 LocalDate 생성
        LocalDate startDate = createSafeLocalDate(takingSupplement.getStartYear(), takingSupplement.getStartMonth(), takingSupplement.getStartDay());
        LocalDate endDate = createSafeLocalDate(takingSupplement.getEndYear(), takingSupplement.getEndMonth(), takingSupplement.getEndDay());
        //System.out.println(startDate + " " + endDate + " "+ today);
        //System.out.println("daysOfWeek raw: " + takingPill.getDaysOfWeek());

        if (startDate.isAfter(today) || endDate.isBefore(today)) {
            return false;
        }

        // 요일 확인
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> daysOfWeek = mapper.readValue(takingSupplement.getDaysOfWeek(),
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
    private void validateTakingSupplementRequest(TakingSupplementRequest request) {
        if (request.getSupplementId() == null) {
            throw new RuntimeException("약품 ID는 필수입니다.");
        }
        
        if (request.getSupplementName() == null || request.getSupplementName().trim().isEmpty()) {
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
        if (request.getSupplementSchedules() != null && !request.getSupplementSchedules().isEmpty()) {
            for (TakingSupplementRequest.SupplementSchedule schedule : request.getSupplementSchedules()) {
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
            TakingSupplement supplement,
            List<SupplementLog> existingLogs,
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
        String supplementName = supplement.getSupplementName();
        List<SupplementSchedule> schedules = supplement.getSupplementSchedules();
        List<LocalDateTime> existingTimes = extractScheduledTimes(existingLogs);

        LocalDate startDate = pillStartDate(supplement);
        LocalDate endDate = pillEndDate(supplement);

        if (status == COMPLETED) return;

        if (isStartDateEarlier && !(isScheduleChanged && status == NEW) && status != ACTIVE) {
            List<SupplementLog> backfill = generateSupplementLogsBetween(
                    user, supplementName, supplement.getAlarmName(),
                    startDate, oldStart.minusDays(1),
                    schedules, supplement, existingTimes
            );
            supplementLogRepository.saveAll(backfill);
        }

        if (isStartDatePushed && status == NEW) {
            supplementLogRepository.deleteAll(
                    existingLogs.stream()
                            .filter(log -> log.getScheduledTime().toLocalDate().isBefore(startDate))
                            .collect(Collectors.toList())
            );
        }

        if (isEndDateExtended && !isScheduleChanged) {
            List<SupplementLog> futureLogs = generateSupplementLogsBetween(
                    user, supplementName, supplement.getAlarmName(),
                    oldEnd.plusDays(1), endDate,
                    schedules, supplement, existingTimes
            );
            supplementLogRepository.saveAll(futureLogs);
        }

        if (isEndDateShortened) {
            supplementLogRepository.deleteAll(
                    existingLogs.stream()
                            .filter(log -> log.getScheduledTime().toLocalDate().isAfter(endDate))
                            .collect(Collectors.toList())
            );
        }

        if (isScheduleChanged) {
            if (status == NEW) {
                supplementLogRepository.deleteAll(existingLogs);

                List<SupplementLog> regenerated = generateSupplementLogsBetween(
                        user, supplementName, supplement.getAlarmName(),
                        startDate, endDate,
                        schedules, supplement, List.of()  // NEW는 전체 삭제니까 기존 로그 없음
                );
                supplementLogRepository.saveAll(regenerated);
            } else if (status == ACTIVE) {
                List<SupplementLog> futureLogsToRemove = existingLogs.stream()
                        .filter(log -> !log.getIsTaken() && !log.getScheduledTime().isBefore(now))
                        .collect(Collectors.toList());

                supplementLogRepository.deleteAll(futureLogsToRemove);

                List<LocalDateTime> futureLogTimes = extractScheduledTimes(existingLogs);

                List<SupplementLog> regenerated = generateSupplementLogsBetween(
                        user, supplementName, supplement.getAlarmName(),
                        now.toLocalDate(), endDate,
                        schedules, supplement, futureLogTimes
                );
                supplementLogRepository.saveAll(regenerated);
            }
        }
    }


    private List<LocalDateTime> extractScheduledTimes(List<SupplementLog> logs) {
        return logs.stream()
                .map(SupplementLog::getScheduledTime)
                .collect(Collectors.toList());
    }

    private List<SupplementLog> generateSupplementLogsBetween(
            User user,
            String supplementName,
            String alarmName,
            LocalDate from,
            LocalDate to,
            List<SupplementSchedule> schedules,
            TakingSupplement supplement,
            List<LocalDateTime> existingTimes
    ) {
        List<SupplementLog> logs = new ArrayList<>();

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            if (!matchesToday(supplement, date)) continue;

            for (SupplementSchedule schedule : schedules) {
                int hour = to24Hour(schedule.getHour(), schedule.getPeriod());
                LocalDateTime scheduledTime = LocalDateTime.of(date, LocalTime.of(hour, schedule.getMinute()));

                if (existingTimes.contains(scheduledTime)) continue;

                SupplementLog log = SupplementLog.builder()
                        .user(user)
                        .takingSupplement(supplement)
                        .supplementName(supplementName)
                        .alarmName(alarmName)
                        .scheduledTime(scheduledTime)
                        .visible(schedule.getAlarmOnOff())
                        .build();

                logs.add(log);
            }
        }

        return logs;
    }



    private LocalDate pillStartDate(TakingSupplement supplement) {
        return createSafeLocalDate(supplement.getStartYear(), supplement.getStartMonth(), supplement.getStartDay());
    }

    private LocalDate pillEndDate(TakingSupplement supplement) {
        return createSafeLocalDate(supplement.getEndYear(), supplement.getEndMonth(), supplement.getEndDay());
    }

    private boolean isScheduleEqual(List<SupplementSchedule> existing, List<TakingSupplementRequest.SupplementSchedule> updated,
            List<String> oldDaysofWeek, List<String> newDaysofWeek) {
        if (existing.size() != updated.size()) return false;
        for (int i = 0; i < existing.size(); i++) {
            SupplementSchedule e = existing.get(i);
            TakingSupplementRequest.SupplementSchedule u = updated.get(i);

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
        takingSupplementRepository.deleteExpiredSupplements(today.getYear(), today.getMonthValue(), today.getDayOfMonth());
        logger.info("Removed expired taking pills at {}", LocalDateTime.now());
    }

} 