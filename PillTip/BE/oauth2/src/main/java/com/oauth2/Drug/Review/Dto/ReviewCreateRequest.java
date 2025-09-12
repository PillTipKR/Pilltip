package com.oauth2.Drug.Review.Dto;

import java.util.List;
import java.util.Map;

public record ReviewCreateRequest (
        Long drugId,
        Float rating,
        String content,
        Map<String, List<String>> tags // "efficacy": [...], "sideEffect": [...], "other": [...]
){}

