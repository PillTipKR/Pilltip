package com.oauth2.User.TakingPill.Dto;

import java.time.LocalDateTime;

public record DosageScheduleDto(
        Long logId,
        LocalDateTime scheduledTime,
        Boolean isTaken,
        LocalDateTime takenAt,
        boolean isVisible
) {}
