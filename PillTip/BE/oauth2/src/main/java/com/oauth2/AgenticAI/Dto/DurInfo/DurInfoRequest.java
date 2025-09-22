package com.oauth2.AgenticAI.Dto.DurInfo;

import com.oauth2.AgenticAI.Dto.Profile;

import java.util.List;

public record DurInfoRequest(
        List<String> items,     // ["magnesium","lactobacillus"] 처럼 표준화된 이름/ID
        Profile profile,
        String questionType     // "interaction" | "contraindication" | "caution"
) {}
