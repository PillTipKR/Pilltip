package com.oauth2.Util.Elasticsearch.Dto;

public record SupplementIngrDetail(
        String name,
        Double dose,
        String unit,
        boolean isMain
) {}
