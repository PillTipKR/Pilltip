package com.oauth2.AgenticAI.Dto;

import java.util.List;

public record Profile(
        Integer age,
        Boolean pregnant,
        List<String> conditions,
        List<String> allergies,
        List<String> currentItems // 복용 중 성분/제품명
) {}

