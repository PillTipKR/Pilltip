package com.oauth2.Drug.DUR.Dto;

import com.oauth2.Drug.Search.Dto.IngredientDetail;

import java.util.List;

// 검색결과 + dur 태그
public record SearchDurDto(
        Long id,
        String drugName,
        List<IngredientDetail> ingredients,
        String manufacturer,
        String imageUrl,
        List<DurTagDto> durTags,
        Boolean isTaking
)
{}
