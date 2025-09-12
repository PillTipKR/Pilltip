package com.oauth2.User.TakingPill.Dto;

import java.time.LocalDate;

public record DayDosageDto(
   LocalDate date,
   AllDosageLogResponse allDosageLogResponse
) {}
