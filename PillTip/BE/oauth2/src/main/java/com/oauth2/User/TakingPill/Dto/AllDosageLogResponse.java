package com.oauth2.User.TakingPill.Dto;

import java.util.List;

public record AllDosageLogResponse(
        int percent,
        List<DosageLogResponse> perDrugLogs
) {}
