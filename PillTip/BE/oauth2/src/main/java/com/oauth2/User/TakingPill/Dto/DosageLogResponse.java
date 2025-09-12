package com.oauth2.User.TakingPill.Dto;


import java.util.List;

public record DosageLogResponse (
    int percent,
    String medicationName,
    List<DosageScheduleDto> dosageSchedule
){}

