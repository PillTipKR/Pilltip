package com.oauth2.HealthSupplement.DUR.Dto;

import com.oauth2.Drug.DUR.Dto.DurPerProductDto;

// DUR redis 결과 저장용
public record SupplementDurAnalysisResponse(
   DurPerProductDto durSupplement,
   DurPerProductDto durDrug,
   DurPerProductDto interact,
   boolean userTaken
) {}
