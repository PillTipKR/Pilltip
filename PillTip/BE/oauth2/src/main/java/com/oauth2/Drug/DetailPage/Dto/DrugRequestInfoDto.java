package com.oauth2.Drug.DetailPage.Dto;

public record DrugRequestInfoDto (
    String name,
    EffectDetail effect,
    EffectDetail usage,
    EffectDetail caution
){}
