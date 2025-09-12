package com.oauth2.HealthSupplement.DetailPage.Dto;

public record SupplementRequestInfoDto(
    String name,
    SupplementEffectDetail effect,
    SupplementEffectDetail usage,
    SupplementEffectDetail caution
){}
