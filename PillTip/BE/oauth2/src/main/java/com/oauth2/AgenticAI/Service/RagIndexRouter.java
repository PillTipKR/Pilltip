package com.oauth2.AgenticAI.Service;

import com.oauth2.AgenticAI.Dto.DoseInfo.DoseRow;
import com.oauth2.AgenticAI.Dto.DurInfo.DurRuleRow;
import com.oauth2.AgenticAI.Dto.ProductTool.ProductRow;
import com.oauth2.Drug.DUR.Domain.DurType;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DrugInfo.Domain.DrugEffect;
import com.oauth2.Drug.DrugInfo.Domain.Ingredient;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.Drug.DrugInfo.Repository.IngredientRepository;
import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplement;
import com.oauth2.HealthSupplement.SupplementInfo.Repository.HealthSupplementRepository;
import io.weaviate.client.WeaviateClient;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RagIndexRouter {
    @Qualifier("productVS") private final VectorStore productVS;
    @Qualifier("durVS") private final VectorStore durVS;
    @Qualifier("doseVS") private final VectorStore doseVS;

    @Value("${supplement.intake}")
    private String intakeName;

    private final TokenTextSplitter tokenSplitter = new TokenTextSplitter();
    private final DrugRepository drugRepository;
    private final HealthSupplementRepository supplementRepository;
    private final IngredientRepository ingredientRepository;
    private final WeaviateClient client;
    private final DataFormatter formatter = new DataFormatter();

    public void batchDrug(){
        List<Drug> drugs = drugRepository.findAll().stream()
                .filter(t-> t.getTag().name().equals("COMMON")).toList();
        for(Drug d: drugs) {
            upsertProduct(buildDrugRow(d.getId()));
        }
    }

    public void batchDur(){
        List<Drug> drugs = drugRepository.findAll();
        List<HealthSupplement> healthSupplements = supplementRepository.findAll();
        List<Ingredient> ingredients = ingredientRepository.findAll();
        upsertDur(buildDurRow(113L,DurType.DRUGINGR));
        for(Ingredient i : ingredients) upsertDur(buildDurRow(i.getId(), DurType.DRUGINGR));
        for(Drug d: drugs) upsertDur(buildDurRow(d.getId(),DurType.DRUG));
        for(HealthSupplement hs : healthSupplements) upsertDur(buildDurRow(hs.getId(),DurType.SUPPLEMENT));

    }

    public void batchDose() throws IOException {
        List<DoseRow> documentsToUpsert = parseAndCreateDocuments(intakeName);

        for(DoseRow doc : documentsToUpsert){
            upsertDose(doc);
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

    public DurRuleRow buildDurRow(Long id, DurType durType){
        switch (durType) {
            case DRUG:
                Drug drug = drugRepository.findById(id).orElse(null);
                assert drug != null;
                return new DurRuleRow(
                        String.valueOf(drug.getId()),
                        drug.getName(),
                        durType.name()
                );
            case SUPPLEMENT:
                HealthSupplement hs = supplementRepository.findById(id).orElse(null);
                assert hs != null;
                return new DurRuleRow(
                        String.valueOf(hs.getId()),
                        hs.getName(),
                        durType.name()
                );
            case DRUGINGR:
                Ingredient ingredient = ingredientRepository.findById(id).orElse(null);
                assert ingredient != null;
                return new DurRuleRow(
                        String.valueOf(ingredient.getId()),
                        ingredient.getName(),
                        durType.name()
                );
            default:
                return null;
        }

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
        String text = "이름:%s. 타입:%s."
                .formatted(nz(r.name()), r.durType());

        Map<String,Object> meta = new LinkedHashMap<>();
        put(meta, "ruleId", r.ruleId());
        put(meta, "name", r.name());
        put(meta, "durType", r.durType());

        //System.out.println(text);
        addAsSingleDocument(durVS, "이름:"+r.name(), text, "name",
                r.ruleId(), meta);
    }

    // 용법/용량 인덱싱
    public void upsertDose(DoseRow r) {
        String text = "이름:%s %s 상태:%s 충분섭취량:%s 권장섭취량:%s,최소량:%s,최대량:%s,비고:%s"
                .formatted(nvl(r.name()), nvl(r.ageRange()), nvl(r.status()),nvl(r.enough()), nvl(r.recommend()),
                        nz(r.min()), nz(r.max()), nz(r.unit()));

        Map<String,Object> meta = new LinkedHashMap<>();
        put(meta, "name", r.name());
        put(meta, "ageRange", r.ageRange());
        put(meta, "status", r.status());
        put(meta, "enough", r.enough());
        put(meta, "recommend", r.recommend());
        put(meta, "min", r.min());
        put(meta, "max", r.max());
        put(meta, "unit", r.unit());

        Integer[] ageInMonths = parseAgeRangeToMonths(r.ageRange());
        if (ageInMonths != null) {
            put(meta, "age_start_months", ageInMonths[0]);
            put(meta, "age_end_months", ageInMonths[1]);
        }

        add(doseVS, nvl(r.ageRange()), text, "text", nvl(r.ageRange()), meta);
    }

    private Integer[] parseAgeRangeToMonths(String ageRange) {
        if (ageRange == null || ageRange.isBlank()) return null;

        // 예시 패턴: "A~B세", "A~B개월", "A세 이상"
        Pattern yearRangePattern = Pattern.compile("(\\d+)\\s*-\\s*(\\d+)\\s*세");
        Pattern monthRangePattern = Pattern.compile("(\\d+)\\s*-\\s*(\\d+)\\s*개월");
        Pattern yearOverPattern = Pattern.compile("(\\d+)\\s*세\\s*이상");

        Matcher m;

        m = yearRangePattern.matcher(ageRange);
        if (m.find()) {
            int startYear = Integer.parseInt(m.group(1));
            int endYear = Integer.parseInt(m.group(2));
            // X세의 마지막 날은 (X+1) * 12 - 1 개월까지 포함
            return new Integer[]{startYear * 12, (endYear + 1) * 12 - 1};
        }

        m = monthRangePattern.matcher(ageRange);
        if (m.find()) {
            int startMonth = Integer.parseInt(m.group(1));
            int endMonth = Integer.parseInt(m.group(2));
            return new Integer[]{startMonth, endMonth};
        }

        m = yearOverPattern.matcher(ageRange);
        if (m.find()) {
            int startYear = Integer.parseInt(m.group(1));
            // 종료 나이를 매우 큰 값으로 설정 (예: 150세)
            return new Integer[]{startYear * 12, 150 * 12};
        }

        // 다른 형식의 패턴이 있다면 여기에 추가...

        return null; // 맞는 패턴이 없을 경우
    }

    public List<DoseRow> parseAndCreateDocuments(String intakeFilePath) throws IOException {
        List<DoseRow> documents = new ArrayList<>();
        FileInputStream fis = new FileInputStream(intakeFilePath);
        Workbook workbook = new XSSFWorkbook(fis);

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String nutrientName = sheet.getSheetName();
            if (nutrientName.equals("인")) continue;

            String currentGender = null;
            String currentAgeRange = null;

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                // ... (기존 성별, 연령 파싱 로직은 동일) ...
                Cell genderCell = row.getCell(0);
                Cell ageCell = row.getCell(1);
                String genderRaw = getString(genderCell);
                String ageRangeRaw = getString(ageCell);
                if (genderRaw != null && !genderRaw.isEmpty()) currentGender = genderRaw;
                if (ageRangeRaw != null && !ageRangeRaw.isEmpty()) currentAgeRange = ageRangeRaw;
                if (currentGender == null || currentAgeRange == null) continue;

                // 값 파싱
                String recommend = getString(row.getCell(2));
                String enough = getString(row.getCell(3));
                String minimum = getString(row.getCell(4));
                String maximum = getString(row.getCell(5));
                String unit = getString(row.getCell(6));

                DoseRow doseRow = new DoseRow(
                        nutrientName,
                        currentGender,
                        currentAgeRange,
                        minimum,
                        maximum,
                        recommend,
                        enough,
                        unit
                );
                documents.add(doseRow);
            }
        }

        workbook.close();
        return documents;
    }

    private String getString(Cell cell) {
        if (cell == null) {
            return "";
        }
        // formatCellValue는 어떤 타입의 셀이든 알아서 문자열로 변환해 줍니다.
        return formatter.formatCellValue(cell).trim();
    }

    public void deleteAllClassData(String className) {
        client.schema().classDeleter()
                .withClassName(className)
                .run();
    }

    private void addAsSingleDocument(VectorStore vs, String title, String content, String source,
                                     String docId, Map<String, Object> extraMeta) {

        // 1. 메타데이터를 준비합니다. 이 부분은 기존과 동일합니다.
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("title", title);
        metadata.put("source", source);
        metadata.put("docId", docId);
        if (extraMeta != null) {
            metadata.putAll(extraMeta);
        }

        // 2. 청킹 로직을 모두 제거하고, 'content' 전체를 내용으로 하는 단일 Document 객체를 생성합니다.
        Document singleDocument = new Document(content, metadata);

        // 3. 단일 Document를 리스트에 담아 VectorStore에 추가합니다.
        // VectorStore의 add 메소드는 List<Document>를 파라미터로 받기 때문입니다.
        vs.add(List.of(singleDocument));
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
            assert d.getText() != null;
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

        Pattern DELIMS = Pattern.compile(
                "(?:" +
                        "(?<!\\d)(,)(?!\\d)\\s+" +     // group 1: 콤마+공백 (숫자 콤마는 제외)
                        "|" +
                        "(\\.)\\s+" +                  // group 2: 온점+공백
                        "|" +
                        "([가-힣]요)([.?!…])?\\s+" +    // group 3: 한글+요, group 4: 선택 구두점
                        ")"
        );

        Matcher m = DELIMS.matcher(content);
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
            String piece = content.substring(start, end).trim();
            if (!piece.isBlank()) out.add(piece);
            start = m.end(); // 뒤 공백까지 소비 후 다음 시작
        }

        if (start < content.length()) {
            String tail = content.substring(start).trim();
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

    public Map<String, Object> toDurPayload(List<Document> docs) {
        // 1. 문서를 docId 기준으로 그룹화합니다. (청킹되지 않았지만, 일관성과 확장성을 위해 구조 유지)
        Map<String, List<Document>> byDoc = new LinkedHashMap<>();
        for (var d : docs) {
            // docKey 헬퍼 메소드는 그대로 사용합니다.
            byDoc.computeIfAbsent(docKey(d), k -> new ArrayList<>()).add(d);
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (var e : byDoc.entrySet()) {
            var list = e.getValue();
            if (list.isEmpty()) {
                continue;
            }

            // 청킹되지 않은 문서이므로, 그룹의 첫 번째 문서가 모든 정보를 가집니다.
            Document doc = list.get(0);

            // 2. 점수(score)를 가져옵니다. toProductEffectPayload와 동일한 로직을 사용합니다.
            double bestScore = Optional.ofNullable(doc.getMetadata().get("score"))
                    .filter(Number.class::isInstance)
                    .map(v -> ((Number) v).doubleValue())
                    .orElse(0.0);

            if (bestScore == 0.0) { // score가 없는 경우 distance로 대체 계산
                bestScore = Optional.ofNullable(doc.getMetadata().get("distance"))
                        .filter(Number.class::isInstance)
                        .map(v -> 1.0 / (1.0 + ((Number) v).doubleValue()))
                        .orElse(0.0);
            }

            // 3. 메타데이터에서 필요한 정보를 추출합니다.
            String name = String.valueOf(doc.getMetadata().getOrDefault("name", ""));
            String durType = String.valueOf(doc.getMetadata().getOrDefault("durType", ""));

            // 4. 최종 결과를 구성합니다.
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("docId", e.getKey());
            out.put("name", name);
            // 'effect' 대신 문서의 전체 'text'를 'description'으로 제공
            out.put("description", doc.getText());
            out.put("durType", durType); // DUR 타입은 중요한 정보이므로 추가
            out.put("score", round2(bestScore));
            items.add(out);
        }

        // 5. 최종 결과를 점수(score) 기준으로 내림차순 정렬합니다.
        items.sort(Comparator.<Map<String, Object>, Double>comparing(m -> ((Number) m.get("score")).doubleValue()).reversed());

        // 6. toProductEffectPayload와 동일한 최종 포맷으로 래핑하여 반환합니다.
        return Map.of("count", items.size(), "results", items);
    }

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
        if (!id.isBlank()) {
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
