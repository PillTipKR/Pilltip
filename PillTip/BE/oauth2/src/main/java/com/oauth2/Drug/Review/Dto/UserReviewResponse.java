package com.oauth2.Drug.Review.Dto;


public record UserReviewResponse (
        Long drugId,
        String drugName,
        ReviewResponse reviews
)
{}
