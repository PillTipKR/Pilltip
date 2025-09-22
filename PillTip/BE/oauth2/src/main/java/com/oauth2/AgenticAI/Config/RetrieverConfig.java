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
        // 서버측 필터로 source를 쓰고 싶다면, 'source'가 스키마 속성으로 존재해야 함
        String where = null; // 예: "where: { path: [\"source\"], operator: Equal, valueText: \"effect\" }"
        return new WeaviateHybridRetriever(
                client,
                "ProductDoc",                // 클래스명
                List.of("content"),                    // BM25 대상
                List.of("content"),                    // 텍스트 필드 후보
                SELECT_BASE,                 // 선택 필드
                where,                       // where 절(옵션)
                0.6,                          // alpha
                embeddingModel
        );
    }

    @Bean("durRetriever")
    public WeaviateHybridRetriever durRetriever(WeaviateClient client) {
        return new WeaviateHybridRetriever(
                client,
                "DurDoc",
                List.of("content"),
                List.of("content"),
                SELECT_BASE,
                null,
                0.6,
                embeddingModel
        );
    }

    @Bean("doseRetriever")
    public WeaviateHybridRetriever doseRetriever(WeaviateClient client) {
        return new WeaviateHybridRetriever(
                client,
                "DoseDoc",
                List.of("content"),
                List.of("content"),
                SELECT_BASE,
                null,
                0.6,
                embeddingModel
        );
    }
}
