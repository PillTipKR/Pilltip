package com.oauth2.HealthSupplement.Search.Dto;

import com.oauth2.Util.Elasticsearch.Dto.SupplementIngrDetail;

import java.util.List;

public record SupplementSearchIndexDto(
        Long id,
        String supplementName,
        List<SupplementIngrDetail> ingredient,
        String enterprise
) {}
