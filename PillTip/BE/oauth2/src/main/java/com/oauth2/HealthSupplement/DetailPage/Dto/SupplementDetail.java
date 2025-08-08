package com.oauth2.HealthSupplement.DetailPage.Dto;

import com.oauth2.Drug.DUR.Dto.DurTagDto;
import com.oauth2.Util.Elasticsearch.Dto.SupplementIngrDetail;
import lombok.Builder;

import java.util.List;

@Builder
public record SupplementDetail(
        Long id,
        String name,
        String enterprise,
        List<SupplementIngrDetail> ingredients,
        String form,
        String dispos,
        String rawMatrl,
        String imageUrl,
        String validTerm,
        SupplementStorageDetail fridge,
        SupplementStorageDetail temperature,
        SupplementStorageDetail light,
        SupplementStorageDetail humid,
        SupplementEffectDetail effect,
        SupplementEffectDetail usage,
        SupplementEffectDetail caution,
        List<DurTagDto> durTags,
        Boolean isTaking,
        Integer count
) {}
