package com.oauth2.Drug.Review.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RatingStatsResponse {
    private Double average; // 예: 4.35
    private Map<Integer, Long> ratingCounts; // 예: {5: 10, 4: 8, 3: 1, 2: 0, 1: 0}
}

