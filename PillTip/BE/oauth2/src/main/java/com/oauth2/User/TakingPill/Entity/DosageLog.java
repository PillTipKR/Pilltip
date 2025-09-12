package com.oauth2.User.TakingPill.Entity;

import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.Util.Encryption.EncryptionConverter;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "dosage_log")
public class DosageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Convert(converter = EncryptionConverter.class)
    private User user;  // 사용자 기준으로 복약 기록을 저장

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taking_pill_id", nullable = false)
    @Convert(converter = EncryptionConverter.class)
    private TakingPill takingPill;  // 사용자 기준으로 복약 기록을 저장

    @Column(nullable = false)
    private String medicationName;

    @Column(nullable = false)
    @Convert(converter = EncryptionConverter.class)
    private String alarmName;

    @Column(nullable = false)
    private LocalDateTime scheduledTime; // 복약 예정 시간

    @Column
    private LocalDateTime takenAt; // 실제 복약 완료 시간

    @Column(nullable = false)
    private Boolean isTaken = false; // 복약 완료 여부

    @Column
    private LocalDateTime rescheduledTime; // 알림 재전송 시간

    @Column
    private Boolean visible = true; //알람을 끄면 안 보이는


    @Builder
    public DosageLog(User user, TakingPill takingPill, String alarmName, String medicationName, LocalDateTime scheduledTime, Boolean visible) {
        this.user = user;
        this.takingPill = takingPill;
        this.medicationName = medicationName;
        this.alarmName = alarmName;
        this.scheduledTime = scheduledTime;
        this.visible = visible;
        this.isTaken = false;
    }
}
