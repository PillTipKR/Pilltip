package com.oauth2.User.TakingSupplement.Service;

import com.oauth2.User.TakingPill.Dto.*;
import com.oauth2.User.TakingSupplement.Entity.SupplementLog;
import com.oauth2.User.TakingSupplement.Repository.SupplementLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SupplementLogService {

    private final SupplementLogRepository supplementLogRepository;

    public void updateTaken(Long supplementLogId) {
        // 복약 완료 상태 업데이트
        SupplementLog supplementLog = supplementLogRepository.findById(supplementLogId)
                .orElseThrow(() -> new IllegalArgumentException("복약 기록을 찾을 수 없습니다"));

        if(!supplementLog.getIsTaken()){
            supplementLog.setTakenAt(LocalDateTime.now());  // 복약 완료 시간
            supplementLog.setIsTaken(true);  // 복약 완료 상태
        }else{
            supplementLog.setTakenAt(null);  // 복약 완료 시간
            supplementLog.setIsTaken(false);  // 복약 완료 상태
        }
        supplementLogRepository.save(supplementLog);
    }

    public void alarmTaken(Long supplementLogId) {
        // 복약 완료 상태 업데이트
        SupplementLog supplementLog = supplementLogRepository.findById(supplementLogId)
                .orElseThrow(() -> new IllegalArgumentException("복약 기록을 찾을 수 없습니다"));

        if(!supplementLog.getIsTaken()){
            supplementLog.setTakenAt(LocalDateTime.now());  // 복약 완료 시간
            supplementLog.setIsTaken(true);  // 복약 완료 상태
        }

        supplementLogRepository.save(supplementLog);
    }

    public void markPending(Long supplementLogId) {
        SupplementLog supplementLog = supplementLogRepository.findById(supplementLogId)
                .orElseThrow(() -> new IllegalArgumentException("복약 기록을 찾을 수 없습니다."));
        supplementLog.setRescheduledTime(LocalDateTime.now().plusMinutes(5));
        supplementLogRepository.save(supplementLog);
    }

    public AllDosageLogResponse getDateLog(Long userId, LocalDate date) {
        List<SupplementLog> logs = supplementLogRepository.findByUserAndDate(userId, date);
        Map<String, List<SupplementLog>> grouped = logs.stream()
                .collect(Collectors.groupingBy(SupplementLog::getSupplementName));

        return convertToDosageLogResponses(grouped);
    }

    private AllDosageLogResponse convertToDosageLogResponses(Map<String, List<SupplementLog>> groupedLogs) {
        List<DosageLogResponse> responses = new ArrayList<>();
        int total = 0;
        int takenCount = 0;
        int totalPercent = 0;
        List<SupplementLog> allLogs = groupedLogs.values().stream()
                .flatMap(List::stream)
                .toList();
        for (Map.Entry<String, List<SupplementLog>> entry : groupedLogs.entrySet()) {
            String name = entry.getKey();
            List<SupplementLog> logs = entry.getValue();

            List<DosageScheduleDto> scheduleDtos = logs.stream()
                    .map(log -> new DosageScheduleDto(
                            log.getId(),
                            log.getScheduledTime(),
                            log.getIsTaken(),
                            log.getTakenAt(),
                            log.getVisible()
                    ))
                    .collect(Collectors.toList());

            // 복약 완료 비율 계산
            int supplementTotal = scheduleDtos.size();
            int supplementTakenCount = Math.toIntExact(scheduleDtos.stream().filter(DosageScheduleDto::isTaken).count());
            int supplementPercent = (int) (supplementTotal == 0 ? 0.0 : (supplementTakenCount * 100.0) / supplementTotal);
            DosageLogResponse response = new DosageLogResponse(
                    supplementPercent,
                    name,
                    scheduleDtos
            );
            responses.add(response);
        }
        total = allLogs.size();
        takenCount = Math.toIntExact(allLogs.stream().filter(SupplementLog::getIsTaken).count());
        totalPercent = (int) (total == 0 ? 0.0 : (takenCount * 100.0) / total);

        return new AllDosageLogResponse(
                totalPercent,
                responses
        );
    }

    public WeekDosageLogResponse getWeeklySummary(Long userId, LocalDate today) {
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

        List<SupplementLog> logs = supplementLogRepository.findWeeklySupplementLogs(userId, startOfWeek, endOfWeek);

        // 날짜별 응답
        Map<LocalDate, List<SupplementLog>> logsByDate = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getScheduledTime().toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()
                ));


        List<DayDosageDto> dailyResponses = new ArrayList<>();
        for (Map.Entry<LocalDate, List<SupplementLog>> entry : logsByDate.entrySet()) {
            Map<String, List<SupplementLog>> grouped = entry.getValue().stream()
                    .collect(Collectors.groupingBy(SupplementLog::getSupplementName));
            dailyResponses.add(new DayDosageDto(
                    entry.getKey(),
                    convertToDosageLogResponses(grouped)
            ));
        }

        // 전체 복약률 계산
        int total = logs.size();
        int taken = (int) logs.stream().filter(SupplementLog::getIsTaken).count();
        int percent = total == 0 ? 0 : (int) ((taken * 100.0) / total);

        return new WeekDosageLogResponse(percent, dailyResponses);
    }

    public SupplementLog getDosageLog(Long logId) {
        return supplementLogRepository.findById(logId).orElse(null);
    }

}
