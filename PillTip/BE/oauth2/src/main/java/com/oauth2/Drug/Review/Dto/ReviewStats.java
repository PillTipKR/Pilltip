package com.oauth2.Drug.Review.Dto;

import com.oauth2.Drug.Review.Domain.TagType;

import java.util.Map;

public record ReviewStats(
    Long total,
    Long like,
    RatingStatsResponse ratingStatsResponse,
    Map<TagType, TagStatsDto> tagStatsByType
){}


