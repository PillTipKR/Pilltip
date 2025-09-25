package com.oauth2.AgenticAI.Tool;

import com.oauth2.AgenticAI.Service.RagSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductRagTool {

    private final RagSearchService search;

    @Tool(name="ProductRagTool", description="제품 관련 문서 스니펫을 하이브리드로 검색")
    public Map<String,Object> run(@P("query") String query, @P("topK") Integer topK) {
        var docs = search.searchProduct(query, topK == null ? 20 : topK);

        var snippets = docs.stream().map(d -> {
            assert d.getText() != null;
            return Map.of(
                    "id", d.getId(),
                    "text", d.getText(),
                    "meta", d.getMetadata()
            );
        }).toList();
        // DurFilterTool로 바로 넘길 수 있게 candidates도 같은 페이로드에 포함
        var candidates = docs.stream().map(d -> Map.of(
                "id", d.getId(),
                // 메타에 제품명/브랜드가 있으면 우선, 없으면 텍스트/ID로 보정
                "name", nameFromMetaOrText(d.getMetadata(), d.getText(), d.getId()),
                "meta", d.getMetadata()
        )).toList();
        return Map.of("snippets", snippets, "candidates", candidates);
    }

    private String nameFromMetaOrText(Map<String,Object> meta, String text, String fallback) {
        if (meta != null) {
            Object v = meta.get("title");
            if (v instanceof String s && !s.isBlank()) return s;

        }
        // 텍스트에서 1~2단어 후보 뽑기(대충이나마)
        if (text != null) {
            var t = text.trim();
            if (!t.isEmpty()) {
                var first = t.split("[\\n\\r]")[0];
                if (first.length() > 2) return first.length() > 64 ? first.substring(0,64) : first;
            }
        }
        return fallback;
    }

}
