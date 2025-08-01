package com.oauth2.Drug.DUR.Dto;

// DUR redis 결과 저장용
public record DurAnalysisResponse(
   DurPerProductDto durA,
   DurPerProductDto durB,
   DurPerProductDto interact,
   boolean userTaken
) {}
