package com.oauth2.AgenticAI.Dto.DurInfo;

import java.util.List;

public record DurInfoResult(
        String summary,         // 한 줄 요약
        String level,           // "safe" | "caution" | "warn"
        List<String> reasons,   // 근거 포인트
        List<String> citations  // 근거 문서/키
) {}

