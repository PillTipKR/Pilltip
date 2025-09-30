package com.oauth2.AgenticAI.Dto.DoseInfo;

public record NormalizationResult(
        String finalQuery,              // 최종적으로 RAG에 전달될 쿼리
        boolean needsClarification,     // 되묻기 필요 여부
        String question,                // 되물을 질문
        String categoryContext          // 되묻기를 유발한 카테고리 (예: "초등학생")
) {}
