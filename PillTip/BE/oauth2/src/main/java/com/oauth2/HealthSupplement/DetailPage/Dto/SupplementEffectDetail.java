package com.oauth2.HealthSupplement.DetailPage.Dto;

import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplementEffect;

public record SupplementEffectDetail(
        HealthSupplementEffect.Type Type,
        String effect
){}

