package com.oauth2.User.TakingPill.Dto;

public record DosageLogUpdateRequest(
   int total,
   int taken,
   int medTotal,
   int medTaken
) {}
