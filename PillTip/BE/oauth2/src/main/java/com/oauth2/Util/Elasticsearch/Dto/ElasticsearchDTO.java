package com.oauth2.Util.Elasticsearch.Dto;

public record ElasticsearchDTO(
        String type,   // "drug", "ingredient", "manufacturer"
        Long id,       // drugId, ingredientId
        String value,   // 자동완성에 노출할 텍스트
        String imageUrl // 이미지 url
) {}
