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
    public List<Document> search(String q, Integer k, Map<String, Object> filterExpression, DocumentRetriever retriever) {
        Map<String, Object> ctx = new LinkedHashMap<>();
        if (k != null && k > 0) {
            ctx.put("topK", k);
        }
        // 전달받은 필터 표현식이 있으면 컨텍스트에 추가합니다.
        if (filterExpression != null && !filterExpression.isEmpty()) {
            ctx.put("filter", filterExpression);
        }

        Query query = Query.builder()
                .text(q)
                .context(ctx)
                .build();

        return retriever.retrieve(query);
    }

    //타입을 추가해야함. (아이,소아,남자,여자)
    //

    public List<Document> searchDose(String nutrientName, Integer k, Integer userAgeInMonths, String status) {
        Map<String, Object> filter = null;

        if (userAgeInMonths != null) {
            // 1. 첫 번째 조건: age_start_months <= userAgeInMonths
            Map<String, Object> condition1 = Map.of(
                    "path", List.of("age_start_months"),
                    "operator", "LessThanEqual",
                    "valueInt", userAgeInMonths
            );

            // 2. 두 번째 조건: age_end_months >= userAgeInMonths
            Map<String, Object> condition2 = Map.of(
                    "path", List.of("age_end_months"),
                    "operator", "GreaterThanEqual",
                    "valueInt", userAgeInMonths
            );

            Map<String, Object> statusCondition = Map.of(
                    "path", List.of("status"),
                    "operator", "Equal",
                    "value", status
            );

            filter = Map.of(
                    "operator", "And",
                    "operands", List.of(condition1, condition2, statusCondition)
            );
        }

        // 필터와 함께 범용 search 메서드를 호출합니다.
        return search(nutrientName, k, filter, doseRetriever);
    }

    public List<Document> searchDur(String q, Integer k) {
        return search(q,k,null,durRetriever);
    }

    public List<Document> searchProduct(String q, Integer k) {
        return search(q,k,null,productRetriever);
    }

}
