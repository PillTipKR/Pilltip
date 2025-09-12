package com.oauth2.Drug.DetailPage.Dto;

import com.oauth2.Drug.DrugInfo.Domain.DrugStorageCondition;

public record StorageDetail (
    DrugStorageCondition.Category category,
    String value,
    boolean active
){}

