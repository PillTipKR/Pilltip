package com.oauth2.User.Alarm.Init;

import com.oauth2.User.Alarm.Dto.AlarmDto;
import com.oauth2.User.Alarm.Service.AlarmService;
import com.oauth2.User.TakingPill.Entity.DosageLog;
import com.oauth2.User.TakingSupplement.Entity.SupplementLog;
import com.oauth2.User.TakingSupplement.Repository.SupplementLogRepository;
import com.oauth2.User.UserInfo.Repository.UserRepository;
import com.oauth2.User.TakingPill.Repositoty.DosageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AlarmScheduler {

    private final AlarmService alarmService;
    private final UserRepository userRepository;
    private final DosageLogRepository dosageLogRepository;
    private final SupplementLogRepository supplementLogRepository;

    @Scheduled(cron = "0 * * * * *")
    public void dispatchMedicationAlarms() {
        LocalDateTime now = LocalDateTime.now();

        List<AlarmDto> drugAlarmDtos = userRepository.findAllActiveUsersWithPillInfo();
        List<AlarmDto> supplementAlarmDtos = userRepository.findAllActiveUsersWithSupplementInfo();

        for (AlarmDto alarmDto : drugAlarmDtos) {
            // TakingPill 엔티티에서 직접 복용 중인 약 정보 조회
            List<DosageLog> dosageLogs = dosageLogRepository.findByUserAndDate(alarmDto.userId(), LocalDate.now());
            List<DosageLog> rescheduled = dosageLogRepository.findByUserAndRescheduledDate(alarmDto.userId(), LocalDate.now());
            for(DosageLog dosageLog : dosageLogs) {
                LocalDateTime dateTime = dosageLog.getScheduledTime();
                if(dateTime.getHour() == now.getHour() && dateTime.getMinute() == now.getMinute()) {
                    alarmService.sendMedicationAlarm(alarmDto.FCMToken(), dosageLog.getId(),
                            dosageLog.getAlarmName(), dosageLog.getMedicationName());
                }
            }
            for(DosageLog dosageLog : rescheduled) {
                LocalDateTime rescheduledTime = dosageLog.getRescheduledTime();
                if(rescheduledTime.getHour() == now.getHour() && rescheduledTime.getMinute() == now.getMinute()) {
                    alarmService.sendMedicationAlarm(alarmDto.FCMToken(), dosageLog.getId(),
                            dosageLog.getAlarmName(), dosageLog.getMedicationName());
                }
            }
        }

        for (AlarmDto alarmDto : supplementAlarmDtos) {
            // TakingPill 엔티티에서 직접 복용 중인 약 정보 조회
            List<SupplementLog> dosageLogs = supplementLogRepository.findByUserAndDate(alarmDto.userId(), LocalDate.now());
            List<SupplementLog> rescheduled = supplementLogRepository.findByUserAndRescheduledDate(alarmDto.userId(), LocalDate.now());
            for(SupplementLog supplementLog : dosageLogs) {
                LocalDateTime dateTime = supplementLog.getScheduledTime();
                if(dateTime.getHour() == now.getHour() && dateTime.getMinute() == now.getMinute()) {
                    alarmService.sendMedicationAlarm(alarmDto.FCMToken(), supplementLog.getId(),
                            supplementLog.getAlarmName(), supplementLog.getSupplementName());
                }
            }
            for(SupplementLog supplementLog : rescheduled) {
                LocalDateTime rescheduledTime = supplementLog.getRescheduledTime();
                if(rescheduledTime.getHour() == now.getHour() && rescheduledTime.getMinute() == now.getMinute()) {
                    alarmService.sendMedicationAlarm(alarmDto.FCMToken(), supplementLog.getId(),
                            supplementLog.getAlarmName(), supplementLog.getSupplementName());
                }
            }
        }
    }
}
