package com.oauth2.AgenticAI.Controller;

import com.oauth2.AgenticAI.Dto.DoseInfo.DoseRow;
import com.oauth2.AgenticAI.Dto.ProductTool.ProductRow;
import com.oauth2.AgenticAI.Service.RagIndexRouter;
import com.oauth2.AgenticAI.Service.RagSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/index")
public class WeaviateController {
    private final RagIndexRouter index;
    private final RagSearchService ragSearchService;

    @PostMapping("/preview/product")
    public Map<String,Object> previewProduct(@RequestBody ProductRow r) {
        String text = "%s(%s, %s). 분류: %s. 효능: %s"
                .formatted(nvl(r.name()), nvl(r.manufacturer()), nvl(r.dispos()),
                        nvl(r.category()), nvl(r.effect()));

        var extra = new LinkedHashMap<String,Object>();
        putIfNotNull(extra, "dispos", r.dispos());
        putIfNotNull(extra, "manufacturer", r.manufacturer());
        putIfNotNull(extra, "category", r.category());

        // docId는 가급적 제품 식별자(코드) 사용
        String docId = r.id() != null
                ? String.valueOf(r.id())
                : r.name() + "|" + nvl(r.manufacturer());

        var chunks = routerPreview("제품:" + nvl(r.name()), text, r.effect(), docId, extra);
        return Map.of("count", chunks.size(), "chunks", chunks);
    }

    private static void copyIfPresent(Document d, Map<String,Object> target, String key) {
        Object v = d.getMetadata().get(key);
        if (v != null) target.put(key, v);
    }
    private static void putIfNotNull(Map<String,Object> m, String k, Object v){
        if (v != null) m.put(k, v);
    }
    private static String nvl(String s){ return s == null ? "" : s; }

    @PostMapping("/product")
    public String product(){

        index.batchDrug();
        return "OK";
    }

    @PostMapping("/dur")
    public String dur(){
        index.batchDur();
        return "OK";
    }

    @PostMapping("/dose")
    public String dose() throws IOException {
        index.batchDose();
        return "OK";
    }

    @GetMapping("/retrieve/product")
    public Map<String,Object> retrieveProduct(
            @RequestParam String q,
            @RequestParam(required = false) Integer k
    ) {
        List<Document> docs = ragSearchService.searchProduct(q, k);
        return index.toProductEffectPayload(docs);
    }

    @GetMapping("/retrieve/dur")
    public Map<String,Object> retrieveDur(
            @RequestParam String q,
            @RequestParam(required = false) Integer k
    ) {
        List<Document> hits = ragSearchService.searchDur(q, k);
        return index.toDurPayload(hits);
    }

    @GetMapping("/retrieve/dose")
    public Map<String,Object> retrieveDose(
            @RequestParam String name,
            @RequestParam Integer age,
            @RequestParam String gender,
            @RequestParam(required = false) Integer k
    ) {
        var hits = ragSearchService.searchDose(name,k,age,gender);
        return Map.of("count", hits.size(), "matches", hits);
    }

    @DeleteMapping("/delete")
    public String deleteDoc(@RequestParam String docName){
        index.deleteAllClassData(docName);
        return "OK";
    }


    private List<Map<String,Object>> routerPreview(String title, String content, String source,
                                                   String docId, Map<String,Object> extra) {
        List<Document> docs = index.makeChunksByDelimiter(
                nvl(title), nvl(content), nvl(source), nvl(docId), extra
        );
        System.out.print(title);
        // Map.of는 null 금지 → 안전하게 LinkedHashMap으로 변환
        List<Map<String,Object>> out = new ArrayList<>(docs.size());
        for (Document d : docs) {
            var m = new LinkedHashMap<String,Object>();
            m.put("id", d.getId());
            Object chunkNo = d.getMetadata().get("chunk");
            m.put("chunk", chunkNo != null ? chunkNo : -1);
            String txt = d.getText();
            m.put("len", txt != null ? txt.length() : 0);
            m.put("text", txt != null ? txt : "");
            // 원하면 메타의 일부를 그대로 노출
            copyIfPresent(d, m, "title");
            copyIfPresent(d, m, "source");
            copyIfPresent(d, m, "docId");
            copyIfPresent(d, m, "manufacturer");
            copyIfPresent(d, m, "category");
            copyIfPresent(d, m, "dispos");
            out.add(m);
        }
        return out;
    }


}

