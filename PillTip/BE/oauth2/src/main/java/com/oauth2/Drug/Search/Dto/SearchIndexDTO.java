package com.oauth2.Drug.Search.Dto;

import java.util.List;

public record SearchIndexDTO (
        Long id,
        String drugName,
        List<IngredientDetail> ingredient,
        String manufacturer
) {}
