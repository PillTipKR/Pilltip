package com.oauth2.Drug.Prompt.Dto;

// GPT 상충작용 분석 결과지 전송용 DTO
public record DurResponse(
    String drugA,
    String drugB,
    String durA,
    String durB,
    String interact,
    Boolean durTrueA,
    Boolean durTrueB,
    Boolean durTrueInter
) {}
