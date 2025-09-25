package com.oauth2.AgenticAI.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.weaviate.WeaviateVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 너가 이미 쓰는 v5 Weaviate 클라/하이브리드 리트리버와 잘 맞게 구성
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.Config;

@Configuration
@RequiredArgsConstructor
public class WeaviateConfig {

    @Value("${spring.ai.vectorstore.weaviate.scheme}")
    private String scheme;

    @Value("${spring.ai.vectorstore.weaviate.host}") // compose에서 8081:8080 매핑 기준
    private String host;

    // Spring AI가 auto-config로 EmbeddingModel을 이미 만들어 둠
    private final EmbeddingModel embeddingModel;

    // 1) 네이티브 클라이언트 (Weaviate Java v5)
    @Bean
    public WeaviateClient weaviateClient() {
        return new WeaviateClient(new Config(scheme, host));
        // API Key 쓰면 인증 옵션 추가해주면 됨
    }

    // 2) 공통 빌더
    private WeaviateVectorStore buildVS(WeaviateClient client, String objectClass) {
        return WeaviateVectorStore.builder(client, embeddingModel)
                .objectClass(objectClass)    // 핵심: 클래스만 다르게
                // .consistencyLevel(...)
                // .readTimeout(...)
                .build();
    }

    // 3) 목적별 VectorStore 3개
    @Bean
    @Qualifier("productVS")
    public VectorStore productVS(WeaviateClient client) {
        return buildVS(client, "ProductDoc");
    }

    @Bean
    @Qualifier("durVS")
    public VectorStore durVS(WeaviateClient client) {
        return buildVS(client, "DurRule");
    }

    @Bean
    @Qualifier("doseVS")
    public VectorStore doseVS(WeaviateClient client) {
        return buildVS(client, "DoseInfo");
    }
}


