package com.oauth2.HealthSupplement.DetailPage.Dto;

import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplementStorageCondition;

public record SupplementStorageDetail(
    HealthSupplementStorageCondition.Category category,
    String value,
    boolean active
){}

