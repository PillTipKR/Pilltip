package com.oauth2.AgenticAI.Util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class WeaviateHybridRetriever implements DocumentRetriever {

    private final WeaviateClient client;
    private final String className;
    private final List<String> bm25Props;     // e.g. ["content"] or ["effectText"]
    private final List<String> textFieldCandidates;
    private final String selectFields;        // e.g. "content metadata _additional { id score distance }"
    private final double alpha;
    private final EmbeddingModel embeddingModel;


    @Override
    public List<Document> retrieve(Query query) {

        final String q = query.text();
        final Map<String, Object> ctx = query.context();
        final int topK = (ctx.get("topK") instanceof Number n && n.intValue() > 0) ? n.intValue() : 5;

        // properties: context override → 없으면 기본값
        List<String> props = this.bm25Props;
        Object ctxProps = ctx.get("properties");
        if (ctxProps instanceof List<?> l && !l.isEmpty()) {
            props = sanitizeStringList(l);
        }
        final String propsArg = props.isEmpty() ? "" : ", properties: [" + quoteJoin(props) + "]";

        // where (동적으로 변경된 부분)
        // 1. Query의 context에서 'filter' 키로 동적 필터 문자열을 가져옵니다.
        String dynamicFilter = (ctx.get("filter") instanceof String s) ? s : null;

        // 2. 동적 필터가 있을 경우에만 whereLine을 구성합니다.
        final String whereLine = (dynamicFilter != null && !dynamicFilter.isBlank())
                ? "where: " + dynamicFilter + "," // Weaviate GraphQL 형식에 맞게
                : "";

        // 쿼리 벡터 (vectorizer:none 해결 포인트)
        final float[] vec = embeddingModel.embed(q);
        final String vecLiteral = toVectorLiteral(vec);

        // GraphQL
        final String gql = """
        {
          Get {
            %s(
              %s
              hybrid: { query: %s, alpha: %s%s, vector: [%s] },
              limit: %d
            ) {
              %s
            }
          }
        }
        """.formatted(
                className,
                whereLine,
                jsonString(q),
                String.valueOf(alpha),
                propsArg,
                vecLiteral,
                topK,
                selectFields
        );

        // 호출
        Result<GraphQLResponse> resp = client.graphQL().raw().withQuery(gql).run();


        if (resp.hasErrors()) {
            throw new RuntimeException("Weaviate hybrid error: " + resp.getError());
        }
        // data -> Json
        JsonObject data = dataAsJson(resp);
        JsonObject getNode = data.has("Get") ? data.getAsJsonObject("Get") : data;
        JsonArray rows = getNode.has(className) ? getNode.getAsJsonArray(className) : new JsonArray();

        List<Document> out = new ArrayList<>(rows.size());
        for (JsonElement el : rows) {
            JsonObject obj = el.getAsJsonObject();

            // 2-a) metadata 파싱 (JsonObject 또는 String(JSON) 모두 수용)
            Map<String,Object> meta = new LinkedHashMap<>();
            if (obj.has("metadata") && !obj.get("metadata").isJsonNull()) {
                JsonElement md = obj.get("metadata");
                if (md.isJsonObject()) {
                    meta.putAll(gsonToMap(md.getAsJsonObject()));
                } else if (md.isJsonPrimitive() && md.getAsJsonPrimitive().isString()) {
                    try {
                        meta.putAll(new Gson().fromJson(md.getAsString(), Map.class));
                    } catch (com.google.gson.JsonSyntaxException ignore) { /* 문자열이 JSON이 아니면 무시 */ }
                }
            }

            // 2-b) _additional → id/score/distance를 메타로 옮겨 후처리에서 쓰기 쉽게
            if (obj.has("_additional") && obj.get("_additional").isJsonObject()) {
                JsonObject add = obj.getAsJsonObject("_additional");
                putIfNotNull(meta, "id",       add.has("id")       ? add.get("id").getAsString()      : null);
                putIfNotNull(meta, "score",    add.has("score")    ? add.get("score").getAsDouble()    : null);
                putIfNotNull(meta, "distance", add.has("distance") ? add.get("distance").getAsDouble() : null);
            }

            // 2-c) 본문 텍스트 필드 선택 (스키마에 맞는 후보들 중 첫 번째)
            String content = pickText(obj, this.textFieldCandidates); // 예: ["text","content"]

            out.add(new Document(content == null ? "" : content, meta));
        }

        return out;
    }


    private static String toVectorLiteral(float[] v) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < v.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format(java.util.Locale.US, "%.6f", v[i]));
        }
        return sb.toString();
    }
    private static String pickText(JsonObject obj, List<String> candidates) {
        for (String f : candidates) {
            if (obj.has(f) && !obj.get(f).isJsonNull()) return obj.get(f).getAsString();
        }
        return "";
    }
    // ---------- helpers ----------
    private static String jsonString(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"","\\\"") + "\"";
    }

    // raw 결과에서 "data"를 JsonObject로 뽑는다 (Map/JsonObject 둘 다 커버)
    static JsonObject dataAsJson(Result<GraphQLResponse> resp) {
        GraphQLResponse r = resp.getResult();
        Object data = r.getData(); // 버전에 따라 Map 이거나 JsonObject

        Gson gson = new Gson();
        if (data instanceof JsonObject jo) {
            return jo;
        }
        // Map, LinkedTreeMap, 기타 객체도 JSON 트리로 안전 변환
        return gson.toJsonTree(data).getAsJsonObject();
    }
    private static List<String> sanitizeStringList(List<?> in) {
        if (in == null) return List.of();
        return in.stream()
                .filter(Objects::nonNull)
                .map(x -> String.valueOf(x).trim())
                .filter(s -> !s.isEmpty() && s.matches("[A-Za-z_][A-Za-z0-9_]*"))
                .collect(Collectors.toList());
    }

    private static String quoteJoin(List<String> props) {
        return props.stream().map(p -> "\"" + p + "\"").collect(Collectors.joining(", "));
    }
    private static void putIfNotNull(Map<String,Object> m, String k, Object v){
        if (v != null) m.put(k, v);
    }
    @SuppressWarnings("unchecked")
    private static Map<String,Object> gsonToMap(JsonObject jo) {
        return new Gson().fromJson(jo, Map.class);
    }

}
