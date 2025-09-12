package com.oauth2.Drug.Review.Dto;

public record TagStatsDto(
        String mostUsedTagName,
        Long mostUsedTagCount,
        Long totalTagCount
){}
