package com.oauth2.User.TakingPill.Dto;

import java.util.List;

public record WeekDosageLogResponse(
   int percent,
   List<DayDosageDto> dayDosageList
) {}
