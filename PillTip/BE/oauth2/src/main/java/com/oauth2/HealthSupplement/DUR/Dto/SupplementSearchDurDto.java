package com.oauth2.HealthSupplement.DUR.Dto;

import com.oauth2.Drug.DUR.Dto.DurTagDto;
import com.oauth2.Drug.Search.Dto.IngredientDetail;
import com.oauth2.Util.Elasticsearch.Dto.SupplementIngrDetail;

import java.util.List;

// 검색결과 + dur 태그
public record SupplementSearchDurDto(
        Long id,
        String supplementName,
        List<SupplementIngrDetail> ingredients,
        String enterprise,
        String imageUrl,
        List<DurTagDto> durTags,
        Boolean isTaking
)
{}
