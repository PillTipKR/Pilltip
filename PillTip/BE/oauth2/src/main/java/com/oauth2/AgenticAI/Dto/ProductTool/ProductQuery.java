package com.oauth2.AgenticAI.Dto.ProductTool;

import com.oauth2.AgenticAI.Dto.Profile;

import java.util.Map;

public record ProductQuery(
        String need,                    // "수면 보조", "철분 보충"
        Profile profile,
        Map<String,Object> constraints  // 가격대/형태/브랜드 등
) {}
