package com.oauth2.AgenticAI.Dto.AskUser;

import java.util.List;

public record AskUserResponse(
        String type,                   // "ask_user"
        String question,
        List<String> slots             // UI가 표시할 부족 슬롯(없으면 빈 리스트)
) {}
