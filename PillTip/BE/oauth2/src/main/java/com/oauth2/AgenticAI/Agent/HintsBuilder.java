package com.oauth2.AgenticAI.Agent;


import com.oauth2.AgenticAI.Dto.Orchestrator.Hints;
import org.springframework.stereotype.Service;

import java.util.*;
import static java.util.stream.Collectors.toList;


public class HintsBuilder {

    // 기존 CONTRACT 수식 그대로 쓰되, queries/tool_hints 같은 "계획" 산출은 제거
    public static Hints fromContract(String U, Map<String,Object> F, Map<String,Object> LM) {
        // 아주 간단한 예시(너 규칙으로 대체)
        List<String> AC = anchorsFrom(U);                       // 토큰화/NER 등
        List<String> soft = softFrom(LM, F);
        List<String> ctx  = ctxFrom(F);
        List<String> must = new ArrayList<>(AC);
        must.addAll(allowFromContext(F, AC));
        must = dedup(must);

        return new Hints(
                new Hints.Anchors(AC, ctx, soft),
                new Hints.Constraints(must, List.of()),
                (String)F.getOrDefault("last_intent","DUR체크"),
                riskFlags(AC),
                allowFromContext(F, AC),
                Map.of("유산균", List.of("프로바이오틱스"))
        );
    }

    // --- 아래는 더미 유틸(실제 로직 대체) ---
    static List<String> anchorsFrom(String u){ return List.of("유산균"); }
    static List<String> softFrom(Map<String,Object> LM, Map<String,Object> F){ return List.of("칼슘"); }
    static List<String> ctxFrom(Map<String,Object> F){
        List<String> s = (List<String>)F.getOrDefault("suppl", List.of());
        List<String> d = (List<String>)F.getOrDefault("drugs", List.of());
        return dedup(merge(s,d));
    }
    static List<String> allowFromContext(Map<String,Object> F, List<String> AC){ return (List<String>)F.getOrDefault("suppl", List.of()); }
    static List<String> riskFlags(List<String> AC){ return new ArrayList<>(); }
    static List<String> merge(List<String> a, List<String> b){ var r=new ArrayList<String>(); r.addAll(a); r.addAll(b); return r; }
    static List<String> dedup(List<String> a){ return a.stream().distinct().collect(toList()); }
}
