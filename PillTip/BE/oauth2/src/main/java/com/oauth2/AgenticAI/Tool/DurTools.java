package com.oauth2.AgenticAI.Tool;

import com.oauth2.AgenticAI.Service.RagSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DurTools {


    private final RagSearchService search;

    /** @Tool: 모델이 자동으로 호출할 메서드 */
    @Tool(name = "DurRagTool", description = "성분/제품 상호작용(DUR) 근거 스니펫을 검색해줘.")
    public Map<String,Object> run(
            @P("query") String query,
            @P("topK") Integer topK
    ) {
        var docs = search.searchDur(query, topK == null ? 5 : topK);
        var snippets = docs.stream().map(d -> {
            assert d.getText() != null;
            return Map.of(
                    "id", d.getId(),
                    "text", d.getText(),          // Spring AI 1.0.x
                    "meta", d.getMetadata()
            );
        }).toList();
        return Map.of("snippets", snippets);
    }
}
