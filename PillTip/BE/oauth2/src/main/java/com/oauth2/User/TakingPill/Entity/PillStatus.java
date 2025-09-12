package com.oauth2.User.TakingPill.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public enum PillStatus {
    NEW, ACTIVE, COMPLETED;

    public static PillStatus calculateStatus(LocalDate endDate, LocalDateTime created) {
        LocalDateTime now = LocalDateTime.now();
        //24시간이 지나지 않았으면 new
        if (!now.isAfter(created.plusHours(24))) {
            return NEW;
        } else if (now.toLocalDate().isAfter(endDate)) {
            return COMPLETED;
        } else {
            return ACTIVE;
        }
    }
}
