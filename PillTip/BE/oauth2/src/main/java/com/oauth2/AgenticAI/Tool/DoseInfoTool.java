package com.oauth2.AgenticAI.Tool;

import com.oauth2.AgenticAI.Service.RagSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DoseInfoTool {


    private final RagSearchService search;

    @Tool(name = "DoseInfoTool", description = "섭취량/복용법(권장량·간격·주의) 정보를 제공합니다.")
    public Map<String,Object> run(
            @P("query") String query,
            @P("topK") Integer topK
    ) {
        var docs = search.searchDose(query, topK == null ? 5 : topK);
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
