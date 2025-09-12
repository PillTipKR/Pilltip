package com.oauth2.Drug.Prompt.Dto;

import java.util.List;
import java.util.Map;

//GPT 입력 토큰
public record GPTRequest(
        String model,
        List<Map<String, String>> messages,
        double temperature,
        int max_tokens
){}

