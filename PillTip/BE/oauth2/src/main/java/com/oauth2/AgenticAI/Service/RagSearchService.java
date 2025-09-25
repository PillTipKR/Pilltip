package com.oauth2.AgenticAI.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RagSearchService {

    // 하이브리드 리트리버 3종 (Config에서 Bean으로 등록해 둔 것)
    @Qualifier("productRetriever") private final DocumentRetriever productRetriever;
    @Qualifier("durRetriever")     private final DocumentRetriever durRetriever;
    @Qualifier("doseRetriever")    private final DocumentRetriever doseRetriever;

    // Service
    public List<Document> search(String q, Integer k, DocumentRetriever retriever) {
        Map<String,Object> ctx = new LinkedHashMap<>();
        if (k != null && k > 0) ctx.put("topK", k);
        // 원하면 메타 필터도 같이
        // ctx.put("filter", Map.of("source","effect"));

        Query query = Query.builder()
                .text(q)
                .context(ctx)
                .build();

        return retriever.retrieve(query);
    }

    public List<Document> searchDose(String q, Integer k) {
        return search(q,k,doseRetriever);
    }

    public List<Document> searchDur(String q, Integer k) {
        return search(q,k,durRetriever);
    }

    public List<Document> searchProduct(String q, Integer k) {
        return search(q,k,productRetriever);
    }

}
