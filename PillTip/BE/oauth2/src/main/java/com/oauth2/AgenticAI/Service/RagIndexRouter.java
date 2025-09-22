package com.oauth2.AgenticAI.Service;

import com.oauth2.AgenticAI.Dto.DoseInfo.DoseRow;
import com.oauth2.AgenticAI.Dto.DurInfo.DurRuleRow;
import com.oauth2.AgenticAI.Dto.ProductTool.ProductRow;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DrugInfo.Domain.DrugEffect;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RagIndexRouter {
    @Qualifier("productVS") private final VectorStore productVS;
    @Qualifier("durVS") private final VectorStore durVS;
    @Qualifier("doseVS") private final VectorStore doseVS;

    private final TokenTextSplitter tokenSplitter = new TokenTextSplitter();
    private final DrugRepository drugRepository;

    public void batchDrug(){
        List<Drug> drugs = drugRepository.findAll().stream()
                .filter(t-> t.getTag().name().equals("COMMON")).toList();
        for(Drug d: drugs) {
            upsertProduct(buildDrugRow(d.getId()));
            System.out.println(d.getId());
        }
    }

    public ProductRow buildDrugRow(Long id) {
        // 한 번의 쿼리로 Drug과 관련된 DrugEffect, DrugStorageCondition을 가져옵니다.
        Optional<Drug> drug = drugRepository.findDrugWithAllRelations(id);
        Set<DrugEffect> effectDetails = new HashSet<>();
        if (drug.isPresent()) {
            effectDetails = drug.get().getDrugEffects();

        }
        DrugEffect effects = effectDetails.stream()
                .filter(e -> e.getType() == DrugEffect.Type.EFFECT)
                .toList().get(0);
        return drug.map(value -> new ProductRow(
                id,
                value.getName(),
                value.getManufacturer(),
                effects.getContent(),
                value.getForm(),
                "일반의약품"

                )).orElse(null);
    }

    // 제품 인덱싱
    public void upsertProduct(ProductRow r) {
        String text = "%s(%s, %s). 분류: %s. 효능: %s"
                .formatted(nvl(r.name()), nvl(r.manufacturer()), nvl(r.dispos()),
                        nvl(r.category()), nz(r.effect()));

        // 제품 식별자 우선 사용, 없으면 name|manufacturer로 대체
        String docId = (r.id() != null) ? String.valueOf(r.id())
                : nvl(r.name()) + "|" + nvl(r.manufacturer());

        Map<String,Object> meta = new LinkedHashMap<>();
        put(meta, "dispos", r.dispos());
        put(meta, "manufacturer", r.manufacturer());
        put(meta, "category", r.category());

        add(productVS, "제품:" + nvl(r.name()), text, "effect", docId, meta);
    }

    // DUR 인덱싱
    public void upsertDur(DurRuleRow r) {
        String text = "[%s] %s + %s. 기전: %s. 조치: %s."
                .formatted(nz(r.severity()).toUpperCase(), nvl(r.a()), nvl(r.b()),
                        nz(r.mechanism()), nz(r.action()));

        Map<String,Object> meta = new LinkedHashMap<>();
        put(meta, "ruleId", r.ruleId());
        put(meta, "a", r.a());
        put(meta, "b", r.b());
        put(meta, "severity", r.severity());

        add(durVS, "DUR:" + nvl(r.a()) + "+" + nvl(r.b()), text, "db:dur",
                nvl(r.ruleId()), meta);
    }

    // 용법/용량 인덱싱
    public void upsertDose(DoseRow r) {
        String text = "%s(%s, 단위 %s). 성인: %s. 소아: %s. 주의: %s."
                .formatted(nvl(r.title()), nvl(r.form()), nvl(r.unit()),
                        nz(r.adult()), nz(r.child()), nz(r.caution()));

        Map<String,Object> meta = new LinkedHashMap<>();
        put(meta, "doseId", r.doseId());
        put(meta, "form", r.form());
        put(meta, "unit", r.unit());

        add(doseVS, nvl(r.title()), text, "db:dose", nvl(r.doseId()), meta);
    }

    private void add(VectorStore vs, String title, String content, String source,
                     String docId, Map<String,Object> extraMeta) {

        Map<String,Object> baseMeta = new LinkedHashMap<>();
        baseMeta.put("title", title);
        baseMeta.put("source", source);
        baseMeta.put("docId", docId);
        if (extraMeta != null) baseMeta.putAll(extraMeta);

        // 1) KR 구분자(콤마+공백 | 온점+공백 | 한글+요(+옵션 .?!…)+공백)로 1차 분할
        // 2) 각 조각이 너무 길면 tokenSplitter로 2차 분할
        List<Document> splitDocs = splitForEmbedding(content, baseMeta);

        // 3) 청크 ID/메타 정리 후 업서트
        List<Document> finalDocs = new ArrayList<>(splitDocs.size());
        for (int i = 0; i < splitDocs.size(); i++) {
            Document d = splitDocs.get(i);
            Map<String,Object> meta = new LinkedHashMap<>(d.getMetadata());
            meta.put("chunk", i);

            // 일부 spring-ai 버전은 id를 생성자에서 받지 않으므로 메타에 넣어둠
            String chunkId = docId + "#" + i;
            meta.put("id", chunkId);

            // 권장 생성자: (content, metadata)
            finalDocs.add(new Document(d.getText(), meta));
        }

        vs.add(finalDocs);
    }

    /**
     * 1차: 길이 무시하고 KR 경계로 분할
     * 2차: 각 조각이 너무 길면 토큰 스플리터로 재분할
     */
    private List<Document> splitForEmbedding(String content, Map<String,Object> baseMeta) {
        List<String> primary = splitByKRDelims(content);

        // 운영에서 튜닝하세요(문자수 기준). 예: 1000자 초과 시 토큰 스플릿
        final int MAX_CHAR = 1000;

        List<Document> out = new ArrayList<>();
        for (String piece : primary) {
            if (piece.isBlank()) continue;

            if (piece.length() <= MAX_CHAR) {
                out.add(new Document(piece, baseMeta));
            } else {
                // 긴 조각만 토큰 기반으로 2차 청킹 (오버랩 포함)
                Document tmp = new Document(piece, baseMeta);
                out.addAll(tokenSplitter.split(tmp));
            }
        }
        return out;
    }

    private List<String> splitByKRDelims(String content) {
        if (content == null) return List.of();
        String text = content;

        Pattern DELIMS = Pattern.compile(
                "(?:" +
                        "(?<!\\d)(,)(?!\\d)\\s+" +     // group 1: 콤마+공백 (숫자 콤마는 제외)
                        "|" +
                        "(\\.)\\s+" +                  // group 2: 온점+공백
                        "|" +
                        "([가-힣]요)([.?!…])?\\s+" +    // group 3: 한글+요, group 4: 선택 구두점
                        ")"
        );

        Matcher m = DELIMS.matcher(text);
        List<String> out = new ArrayList<>();
        int start = 0;

        while (m.find()) {
            int end;
            if (m.group(1) != null) {              // 콤마
                end = m.start(1) + 1;
            } else if (m.group(2) != null) {       // 온점
                end = m.start(2) + 1;
            } else {                               // '요' (+옵션 구두점)
                end = (m.group(4) != null) ? m.end(4) : m.end(3);
            }
            String piece = text.substring(start, end).trim();
            if (!piece.isBlank()) out.add(piece);
            start = m.end(); // 뒤 공백까지 소비 후 다음 시작
        }

        if (start < text.length()) {
            String tail = text.substring(start).trim();
            if (!tail.isBlank()) out.add(tail);
        }
        return out;
    }

    /** 문장 경계(.,!?,…, 개행) 우선으로 자르고, 없으면 하드컷. 오버랩 적용 */

    // 콤마+공백 기준(숫자 천단위 콤마 제외)으로만 자르는 함수
    public List<Document> makeChunksByDelimiter(
            String title,
            String content,
            String source,
            String docId,
            Map<String, Object> extra
    ) {
        if (content == null) content = "";

        Pattern delim = Pattern.compile("(?:" +
                "(?<!\\d)(,)(?!\\d)\\s+" +         // 1) 콤마 + 공백 (숫자 콤마 제외)
                "|" +
                "(\\.)\\s+" +                      // 2) 온점 + 공백
                "|" +
                "([가-힣]요)([.?!…])?\\s+" +        // 3) 한글+요 (+선택적 구두점) + 공백
                ")");
        Matcher m = delim.matcher(content);

        List<Document> out = new ArrayList<>();
        int start = 0;
        int chunkNo = 0;

        while (m.find()) {
            int commaPos = m.start();      // 콤마 위치
            int end = commaPos + 1;        // 콤마까지 포함(뒤 공백은 제외)
            String piece = content.substring(start, end).trim();
            if (!piece.isBlank()) {
                out.add(new Document(piece, buildMeta(title, source, docId, extra, chunkNo++)));
            }
            start = m.end();               // 공백까지 건너뛴 다음 문자부터 다음 조각 시작
        }

        if (start < content.length()) {
            String tail = content.substring(start).trim();
            if (!tail.isBlank()) {
                out.add(new Document(tail, buildMeta(title, source, docId, extra, chunkNo++)));
            }
        }

        return out;
    }

    private Map<String,Object> buildMeta(String title, String source, String docId,
                                         Map<String,Object> extra, int chunkNo) {
        Map<String,Object> meta = new LinkedHashMap<>();
        meta.put("title", title != null ? title : "");
        meta.put("source", source != null ? source : "");
        meta.put("docId", docId != null ? docId : "");
        meta.put("chunk", chunkNo);
        meta.put("delimiter", "comma-space");
        if (extra != null) {
            for (var e : extra.entrySet()) {
                if (e.getValue() != null) meta.put(e.getKey(), e.getValue());
            }
        }
        return meta;
    }


    private static void put(Map<String,Object> m, String k, Object v){ if (v != null) m.put(k, v); }
    private static String nvl(String s){ return (s == null) ? "" : s; }
    private String nz(String s){ return (s == null || s.isBlank()) ? "정보 없음" : s; }

    public Map<String,Object> toProductEffectPayload(List<Document> docs) {
        Map<String, List<Document>> byDoc = new LinkedHashMap<>();
        for (var d : docs) {
            byDoc.computeIfAbsent(docKey(d), k -> new ArrayList<>()).add(d);
        }

        List<Map<String,Object>> items = new ArrayList<>();
        for (var e : byDoc.entrySet()) {
            var list = e.getValue();

            double bestScore = list.stream()
                    .map(d -> d.getMetadata().get("score"))
                    .filter(Number.class::isInstance)
                    .mapToDouble(v -> ((Number)v).doubleValue())
                    .max().orElse(Double.NEGATIVE_INFINITY);

            if (bestScore == Double.NEGATIVE_INFINITY) {
                bestScore = list.stream()
                        .map(d -> d.getMetadata().get("distance"))
                        .filter(Number.class::isInstance)
                        .mapToDouble(v -> 1.0 / (1.0 + ((Number)v).doubleValue()))
                        .max().orElse(0.0);
            }

            String title = String.valueOf(firstMeta(list, "title"));
            String name  = stripPrefix(title, "제품:");
            if (name.isBlank()) name = guessNameFromText(list);

            // 상위 M개 조각만, 중복 제거
            final int topM = 10;
            list.sort((a, b) -> Double.compare(num(b.getMetadata().get("score")), num(a.getMetadata().get("score"))));

            LinkedHashSet<String> picked = new LinkedHashSet<>();
            for (var d : list) {
                if (picked.size() >= topM) break;
                String t = normalizePiece(String.valueOf(d.getText()));
                if (!t.isBlank()) picked.add(t);
            }

            // chunk 번호 기준 정렬(없으면 그대로)
            List<String> ordered = new ArrayList<>(picked);
            ordered.sort((a, b) -> Integer.compare(chunkOf(list, a), chunkOf(list, b)));

            Map<String,Object> out = new LinkedHashMap<>();
            out.put("docId", e.getKey());
            out.put("name", name);
            out.put("effect", String.join(" ", ordered));
            out.put("score", round2(bestScore));
            items.add(out);
        }

        items.sort(Comparator.<Map<String,Object>, Double>comparing(m -> ((Number)m.get("score")).doubleValue()).reversed());
        return Map.of("count", items.size(), "results", items);
    }

    /* === helpers (컨트롤러 유틸) === */
    private static Object firstMeta(List<Document> list, String key) {
        for (var d : list) { Object v = d.getMetadata().get(key); if (v != null) return v; }
        return null;
    }
    private static String docKey(Document d) {
        Object v = d.getMetadata().get("docId");
        if (v != null && !String.valueOf(v).isBlank()) return String.valueOf(v);
        Object addId = d.getMetadata().get("id");
        if (addId != null) {
            String s = String.valueOf(addId);
            int i = s.indexOf('#'); return (i > 0) ? s.substring(0, i) : s;
        }
        String id = d.getId();
        if (id != null && !id.isBlank()) {
            int i = id.indexOf('#'); return (i > 0) ? id.substring(0, i) : id;
        }
        return "";
    }
    private static String stripPrefix(String s, String p){ return (s!=null && s.startsWith(p)) ? s.substring(p.length()) : (s==null?"":s); }
    private static String normalizePiece(String t) {
        if (t == null) return "";
        t = t.replaceAll("\\s+", " ").trim();
        t = t.replaceAll("^\\(|\\)$", "");
        t = t.replaceAll("[,，]+\\s*$", "");
        return t;
    }
    private static int chunkOf(List<Document> group, String piece) {
        for (var d : group) {
            String norm = normalizePiece(String.valueOf(d.getText()));
            if (norm.equals(piece)) {
                Object c = d.getMetadata().get("chunk");
                if (c instanceof Number n) return n.intValue();
            }
        }
        return Integer.MAX_VALUE;
    }
    private static String guessNameFromText(List<Document> list) {
        var pat = java.util.regex.Pattern.compile("^([^(,]+)\\s*\\(");
        for (var d : list) {
            String t = String.valueOf(d.getText());
            var m = pat.matcher(t);
            if (m.find()) return m.group(1).trim();
        }
        return "";
    }
    private static double num(Object o){ return (o instanceof Number n) ? n.doubleValue() : Double.NEGATIVE_INFINITY; }
    private static double round2(double v){ return Math.round(v * 100.0) / 100.0; }

}
