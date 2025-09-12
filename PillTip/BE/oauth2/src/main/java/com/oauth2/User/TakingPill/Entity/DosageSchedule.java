package com.oauth2.User.TakingPill.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "dosage_schedule")
public class DosageSchedule {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @JsonBackReference
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "taking_pill_id", nullable = false)
   private TakingPill takingPill;

   @Column(nullable = false)
   private Integer hour; // 0-12

   @Column(nullable = false)
   private Integer minute; // 0-59

   @Column(nullable = false)
   private String period; // AM/PM

   @Column(name = "alarm_on_off", nullable = false)
   private Boolean alarmOnOff; // 알림 여부

   @Column(name = "dosage_unit", nullable = false)
   private String dosageUnit; // 단위 (회, 정, 포 등)

   @Builder
   public DosageSchedule(TakingPill takingPill, Integer hour, Integer minute, String period, Boolean alarmOnOff, String dosageUnit) {
       this.takingPill = takingPill;
       this.hour = hour;
       this.minute = minute;
       this.period = period;
       this.alarmOnOff = alarmOnOff;
       this.dosageUnit = dosageUnit;
   }
}