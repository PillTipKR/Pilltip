package com.oauth2.Drug.DUR.Dto;


import java.util.List;
import java.util.Map;
import java.util.Set;

// 사용자의 상황,복약정보
public record DurUserContext(
        boolean isElderly,
        boolean isPregnant,
        Map<String, List<Long>> classToProductIdsMap,
        Set<String> userInteractionProductNames
) {}
