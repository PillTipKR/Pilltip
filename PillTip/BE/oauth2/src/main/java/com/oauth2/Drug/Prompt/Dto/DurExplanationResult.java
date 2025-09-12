package com.oauth2.Drug.Prompt.Dto;

// GPT 상충작용 분석 결과 파싱용 DTO
public record DurExplanationResult(
        String drugA,
        String drugB,
        String interact
) {}
