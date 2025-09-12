package com.oauth2.Drug.Prompt.Dto;

import com.oauth2.Drug.DUR.Dto.DurTagDto;
import com.oauth2.Drug.DetailPage.Dto.DrugRequestInfoDto;

import java.util.List;

// GPT 개인복약 가이드라인 생성용 입력
public record PromptRequestDto(
        List<DurTagDto> durInfo,         // DUR 정보 (없을 경우 빈 문자열)
        String nickname,           // 사용자 닉네임
        int age,                  // 사용자 나이
        String gender,            // "MALE" 또는 "FEMALE"
        boolean isPregnant,
        String underlyingDisease, // 기저질환 (예: "고혈압", 없으면
        String allegy,
        List<String> currentDrugs, // 현재 복약 중인 약들
        DrugRequestInfoDto drugInfo        // 약물 요약 정보 (예: "스피드펜연질캡슐200밀리그램(이부프로펜)")
){}

