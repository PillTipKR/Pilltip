package com.oauth2.Drug.DetailPage.Dto;

import com.oauth2.Drug.DUR.Dto.DurTagDto;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.Search.Dto.IngredientDetail;
import lombok.Builder;

import java.util.Date;
import java.util.List;

@Builder
public record DrugDetail (
        Long id,
        String name,
        String manufacturer,
        List<IngredientDetail> ingredients,
        String form,
        String packaging,
        String atcCode,
        String imageUrl,
        Drug.Tag tag,
        Date approvalDate,
        StorageDetail container,
        StorageDetail temperature,
        StorageDetail light,
        StorageDetail humid,
        EffectDetail effect,
        EffectDetail usage,
        EffectDetail caution,
        List<DurTagDto> durTags,
        Boolean isTaking,
        Integer count
) {}
