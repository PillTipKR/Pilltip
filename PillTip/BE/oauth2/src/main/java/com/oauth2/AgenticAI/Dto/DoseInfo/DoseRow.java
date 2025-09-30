package com.oauth2.AgenticAI.Dto.DoseInfo;

public record DoseRow(
        String name,
        String status,
        String ageRange,
        String min,
        String max,
        String recommend,
        String enough,
        String unit
) {}
