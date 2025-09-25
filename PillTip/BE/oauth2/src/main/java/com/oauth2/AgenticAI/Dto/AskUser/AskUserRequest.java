package com.oauth2.AgenticAI.Dto.AskUser;

import java.util.List;

public record AskUserRequest(
        String question,               // 사용자에게 보여줄 질문
        List<String> missingSlots      // (선택) 부족한 슬롯 키들: ["age","pregnant","conditions",...]
) {}

