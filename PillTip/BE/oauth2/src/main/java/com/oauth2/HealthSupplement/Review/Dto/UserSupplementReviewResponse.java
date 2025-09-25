package com.oauth2.HealthSupplement.Review.Dto;


import com.oauth2.Drug.Review.Dto.ReviewResponse;

public record UserSupplementReviewResponse(
        Long supplementId,
        String supplementName,
        SupplementReviewResponse reviews
)
{}
