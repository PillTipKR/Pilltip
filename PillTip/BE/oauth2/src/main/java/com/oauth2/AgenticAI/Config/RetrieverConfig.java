package com.oauth2.AgenticAI.Config;

import com.oauth2.AgenticAI.Util.WeaviateHybridRetriever;
import io.weaviate.client.WeaviateClient;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RetrieverConfig {

    private static final String SELECT_BASE = """
        content
        metadata
        _additional { id score distance }
    """;

    private final EmbeddingModel embeddingModel;

    @Bean("productRetriever")
    public WeaviateHybridRetriever productRetriever(WeaviateClient client) {
        return new WeaviateHybridRetriever(
                client,
                "ProductDoc",                // 클래스명
                List.of("content"),                    // BM25 대상
                List.of("content"),                    // 텍스트 필드 후보
                SELECT_BASE,                 // 선택 필드
                0.5,                          // alpha: 0에 가까울수록 키워드 검색 비중 높음.
                embeddingModel
        );
    }

    @Bean("durRetriever")
    public WeaviateHybridRetriever durRetriever(WeaviateClient client) {
        return new WeaviateHybridRetriever(
                client,
                "DurRule",
                List.of("content"),
                List.of("content"),
                SELECT_BASE,
                0.3,
                embeddingModel
        );
    }

    @Bean("doseRetriever")
    public WeaviateHybridRetriever doseRetriever(WeaviateClient client) {
        return new WeaviateHybridRetriever(
                client,
                "DoseInfo",
                List.of("content"),
                List.of("content"),
                SELECT_BASE,
                0.4,
                embeddingModel
        );
    }
}
