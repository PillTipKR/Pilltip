package com.oauth2.AgenticAI.Dto.DoseInfo;

import java.util.List;

public record DoseInfoResult(
        String guideline,       // 권장량/간격
        List<String> tips,
        List<String> citations
) {}
