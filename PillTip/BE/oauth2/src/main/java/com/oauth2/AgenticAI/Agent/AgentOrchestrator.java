package com.oauth2.AgenticAI.Agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.AgenticAI.Dto.Orchestrator.EventCode;
import com.oauth2.AgenticAI.Dto.Orchestrator.StreamEvent;
import com.oauth2.AgenticAI.Dto.ProductTool.DurFilterRequest;
import com.oauth2.AgenticAI.Dto.ProductTool.DurFilterResult;
import com.oauth2.AgenticAI.Dto.ProductTool.FillteredDto;
import com.oauth2.AgenticAI.Dto.ProductTool.ProductCandidate;
import com.oauth2.AgenticAI.Tool.*;
import com.oauth2.AgenticAI.Util.SessionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AgentOrchestrator {

    private final MemoryStore memory;

    private final ChatModel chatModel;                    // 1패스(툴콜 감지/실행 계획)
    private final StreamingChatModel streamingChatModel;  // 최종 답변 스트리밍
    private final ToolCallingManager toolManager;

    private final AskUserTool askUserTool;
    private final DoseInfoTool doseInfoTool;
    private final DurFilterTool durFilterTool;
    private final DurTool durTool;
    private final ProductRagTool productRagTool;

    private final ObjectMapper objectMapper; // JSON 파싱용

    // --- 1단계: 툴 전용 프롬프트(초단) ---
    private static final String SYS_BASE = """
            역할: 한국어 우선, 도구-우선 에이전트.
            규칙:
            - 증상을 설명하면 반드시 ProductRagTool 툴을 호출
            - 제품/추천 요청이면 반드시 ProductRagTool 툴 호출.
            - ProductRagTool 응답의 candidates를 DurFilterTool req.candidates로 그대로 전달.
            - 약,약의 성분,건강기능식품,건강기능식품의 성분들 간의 상호작용 질문시, 제품 이름 혹은 성분 이름들을 미리 파싱해두고, 이를 DurTool에 전달
            - 필요한 값이 비면 AskUserTool로 1~2개만 짧게 되물음.
            - (중요) 이 단계에서는 '최종 답변 문장'을 쓰지 말 것. 가능한 한 툴 호출만 생성.
            지원 범위:
            - 제품/성분 후보 찾기(ProductRagTool), DUR 상호작용/금기/주의 정보 알림(DurTool), 추가질문(AskUserTool)
            출력 스키마:
            - DurFilterTool 응답(JSON): { "filtered":[{ "id":long, "name":string, "effect":string }], "count": int }
            스코프 밖 요청 처리:
            - 범위를 벗어나거나 도구가 없으면 툴 호출을 만들지 말고 아래 형식 한 줄만 출력:
              REFUSAL: 요청하신 내용은 현재 제공 범위를 벗어나요. (제가 할 수 있는 것: 제품 후보 찾기·상호작용(DUR) 확인·복용 주의 안내) 원하는 제품/성분 기준으로 다시 말씀해 주시겠어요?
            언어: 한국어.
            """;

    // --- 2단계: 최종 답변 전용 프롬프트(모드 3종) ---
    private static final String SYS_ANSWER_RECAP = """
            말투: 한국어 구어체 존댓말(~요).
            형식:
              "찾아본 top-k(TOPK)개의 제품중 SAFE_COUNT개의 제품이 DUR체크를 통해 안전하게 필터링되었어요."
              "NICK님에게 추천하는 제품들은 다음과 같아요."
              SAFE_ITEMS들을 각각 컨텍스트만을 활용하여 문맥에 자연스럽고 친절하게 다듬어 소개
            원칙: 숫자/이름이 주어지면 그대로 사용, 없으면 해당 문장은 생략하고 간단 요약만 말할 것.
            """;

    private static final String SYS_ANSWER_DUR = """
            말투: 한국어 구어체 존댓말(~요). 안전 우선, 신중한 톤.
            형식:
              ① DUR 결론(금기/주의/안전) 한줄
              ② •상호작용 포인트 요약(원인·기전 간단히)
              ③ 권장 행동(복용 간격/대체/의료상담)
              ④ 필요 시 TOPK/SAFE_COUNT 한 줄로 첨언
            원칙: 단정 대신 정도 표현, 근거 수준/불확실성 명시.
            """;

    private static final String SYS_ANSWER_DOSE = """
            말투: 한국어 구어체 존댓말(~요).
            형식:
              ① 누가·언제·얼마(요약)
              ② •복용 방법(용량/간격/식전후)
              ③ 주의/금기
              ④ 잊었을 때/과량 시 대처
            원칙: 연령/상태별 차이는 분명히, 불확실성 명시.
            """;

    private enum AnswerMode { RECAP, DUR_INFO, DOSE_INFO }

    public Flux<StreamEvent> run(String session, String userText, Long userId, String nick) {
        // 0) 히스토리 적재
        memory.appendUser(session, userText == null ? "" : userText);

        // 1) 1단계(툴 전용) 프롬프트 구성
        var msgs = new ArrayList<Message>();
        msgs.add(new SystemMessage(SYS_BASE));
        String sum = memory.summary(session);
        if (sum != null && !sum.isBlank()) msgs.add(new SystemMessage("대화요약: " + sum));
        msgs.addAll(memory.recentTurns(session, 30));
        msgs.add(new UserMessage(userText == null ? "" : userText));

        // 2) 툴 콜백 + 자동실행 OFF (플래닝 전용 옵션: detectOpts)
        ToolCallback[] callbacks = ToolCallbacks.from(
                askUserTool, doseInfoTool, productRagTool, durTool
        );
        var detectOpts = OpenAiChatOptions.builder()
                .internalToolExecutionEnabled(false)   // 플래닝만
                .parallelToolCalls(true)               // 동시 툴 계획 허용
                .toolCallbacks(callbacks)
                .build();

        return Flux.defer(() -> {
            var head   = List.of(StreamEvent.status(EventCode.INTENT_DETECTED, "질문 의도를 파악했어요."));
            var events = new ArrayList<StreamEvent>(); // 단계별 상태 문구

            List<Message> history = new ArrayList<>(msgs);
            ChatResponse resp = chatModel.call(new Prompt(history, detectOpts));

            // DUR 결과 집계에 필요한 변수
            int topK = 0;
            int safeCount = 0;
            List<Map<String,Object>> safeItems = new ArrayList<>(); // [{id,name,effect,meta?}]
            Map<String, Map<String,Object>> candidateById = new HashMap<>();
            List<String> executedTools = new ArrayList<>();

            var planned = safePlannedToolNames(resp);

            if (planned.contains("ProductRagTool")) {

                // --- 1. 제품 탐색 단계 ---
                events.add(StreamEvent.status(EventCode.PRODUCT_SEARCH_START, "제품을 탐색 중이에요."));

                ToolExecutionResult ragExec = null;
                try {
                    if (userId != null) SessionUtils.set(session, userId, nick);
                    // ProductRagTool만 지정해서 실행
                    ragExec = toolManager.executeToolCalls(new Prompt(history, detectOpts), resp);
                } catch(Exception e) {
                    e.printStackTrace();
                } finally {
                    SessionUtils.clear();
                }

                if (ragExec == null) {
                    // 에러 상황이므로 더 이상 진행하지 않고 Flux를 반환해야 할 수 있습니다.
                    return Flux.just(StreamEvent.error(EventCode.BAD_REQUEST, "ProductRagTool 실행에 실패했습니다."));
                }

                history = new ArrayList<>(ragExec.conversationHistory());

                String ragJsonResponse = "";
                Message lastMessage = history.get(history.size() - 1);
                if (lastMessage instanceof ToolResponseMessage trm) {
                    ragJsonResponse = trm.getResponses().get(0).responseData();
                }

                memory.appendToolResult(session, "ProductRagTool", ragJsonResponse);
                executedTools.add("ProductRagTool");

                Map<String,Object> body = parseMap(ragJsonResponse);
                List<Map<String,Object>> candidatesAsMap = getList(body, "candidates");

                if (!candidatesAsMap.isEmpty()) {
                    topK = candidatesAsMap.size();
                    for (Map<String,Object> c : candidatesAsMap) {
                        String id = String.valueOf(c.getOrDefault("id",""));
                        candidateById.put(id, c);
                    }
                }
                events.add(StreamEvent.status(EventCode.PRODUCT_SEARCH_RESULT, "후보 제품을 찾았어요."));


                // --- 2. DUR 상호작용 검색 단계 (LLM 개입 없이 직접 실행) ---
                events.add(StreamEvent.status(EventCode.DUR_CHECK_START, "복용 상호작용(DUR)을 확인 중이에요."));

                String durJsonResponse = "{}";
                DurFilterResult dr = new DurFilterResult(List.of(), 0);
                try {
                    if (userId != null) SessionUtils.set(session, userId, nick); // Set context again
                    List<ProductCandidate> productCandidates = new ArrayList<>();
                    for (Map<String, Object> map : candidatesAsMap) {
                        String id = String.valueOf(map.getOrDefault("id", ""));
                        String name = String.valueOf(map.getOrDefault("name", ""));
                        @SuppressWarnings("unchecked")
                        Map<String, Object> meta = (map.get("meta") instanceof Map)
                                ? (Map<String, Object>) map.get("meta")
                                : new HashMap<>();
                        productCandidates.add(new ProductCandidate(id, name, meta));
                    }

                    var durRequest = new DurFilterRequest(productCandidates);
                    dr = durFilterTool.filter(durRequest);

                    durJsonResponse = objectMapper.writeValueAsString(dr);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    // LLM이 초기에 계획했던 ToolCall에서 'id'를 찾아옵니다.
                    // OpenAI API 규칙을 맞추기 위해 이 'id'가 필요합니다.
                    String durToolCallId = findToolCallId(resp, "DurFilterTool");
                    if(durToolCallId == null) {
                        // 만약 LLM이 DurFilterTool을 계획하지 않았다면 임의의 ID를 생성합니다.
                        durToolCallId = "manual_tool_call_" + UUID.randomUUID().toString().replace("-", "");
                    }

                    ToolResponseMessage durToolResponse = new ToolResponseMessage(List.of(
                            new ToolResponseMessage.ToolResponse(durToolCallId, "DurFilterTool", durJsonResponse)
                    ));
                    history.add(durToolResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                memory.appendToolResult(session, "DurFilterTool", durJsonResponse);
                executedTools.add("DurFilterTool");

                safeCount = dr.count();
                // safeItems를 effect를 포함한 Map 리스트로 변환
                List<Map<String, Object>> tmp = getMaps(dr);
                if (!tmp.isEmpty()) safeItems = tmp;

                events.add(StreamEvent.status(EventCode.DUR_CHECK_RESULT, "DUR 확인 결과를 정리하고 있어요."));
                history = new ArrayList<>(ragExec.conversationHistory());

            }

            // 3) 툴콜이 전혀 없던 케이스 → 거절/단답 처리
            if (!hasToolCalls(resp)) {
                String draft = Optional.of(resp.getResult())
                        .map(Generation::getOutput).map(AssistantMessage::getText).orElse("").trim();

                if (draft.startsWith("REFUSAL:")) {
                    String polite = draft.substring("REFUSAL:".length()).trim();
                    return Flux.concat(
                            Flux.fromIterable(head),
                            Flux.fromIterable(events),
                            Flux.just(
                                    StreamEvent.status(EventCode.READY, "요청 범위를 확인했어요."),
                                    StreamEvent.answer(polite),
                                    StreamEvent.done()
                            )
                    );
                }

                var answerMsgs = new ArrayList<Message>(history);
                answerMsgs.add(new SystemMessage(SYS_ANSWER_RECAP));
                var answer = chatModel.call(new Prompt(
                        answerMsgs, OpenAiChatOptions.builder()
                        .internalToolExecutionEnabled(false)
                        .streamUsage(true).build()
                ));
                String finalText = Optional.of(answer.getResult())
                        .map(res -> res.getOutput().getText())
                        .orElse("");

                return Flux.concat(
                        Flux.fromIterable(head),
                        Flux.fromIterable(events),
                        Flux.just(
                                StreamEvent.status(EventCode.READY, "답변을 정리했어요."),
                                StreamEvent.answer(finalText),
                                StreamEvent.done()
                        )
                );
            }

            // 4) 2단계(답변 전용) 스트리밍 — 툴콜 금지, 말하기만
            var followupMsgs = new ArrayList<Message>(history);      // 루프의 마지막 히스토리

            // 모드 결정
            AnswerMode mode = pickMode(executedTools);

            if (mode != AnswerMode.DOSE_INFO && (topK > 0 || safeCount > 0 || !safeItems.isEmpty())) {
                List<String> lines = new ArrayList<>();
                int limit = Math.min(5, safeItems.size());
                for (int i = 0; i < limit; i++) {
                    Map<String,Object> it = safeItems.get(i);
                    String name = String.valueOf(it.getOrDefault("name","이름없음"));
                    String cat  = categoryOf(it, candidateById); // 일반의약품/건강기능식품/기타
                    String effect = String.valueOf(it.getOrDefault("effect",""));
                    lines.add("- [" + cat + "] " + name + (effect.isBlank() ? "" : " — " + effect));
                }
                String safeListBlock = String.join("\n", lines);

                // topK가 0이면 safeCount로 대체 출력하도록 안내
                int topKOrSafe = topK > 0 ? topK : Math.max(safeCount, lines.size());

                String recap = """
                [변수]
                TOPK=%d
                SAFE_COUNT=%d
                NICK=%s
                SAFE_ITEMS:
                %s

                [출력 지시]
                TOPK가 0이면 "추천 후보 중"으로 표현하고, 숫자는 SAFE_COUNT를 사용해도 된다.
                숫자/이름은 임의 변경하지 말 것.
                """.formatted(topKOrSafe, safeCount, nick, safeListBlock);

                followupMsgs.add(new SystemMessage(recap));
                System.out.println(recap);
            }

            // 모드별 말투/형식 프롬프트 부착
            switch (mode) {
                case DUR_INFO -> followupMsgs.add(new SystemMessage(SYS_ANSWER_DUR));
                case DOSE_INFO -> followupMsgs.add(new SystemMessage(SYS_ANSWER_DOSE));
                default -> followupMsgs.add(new SystemMessage(SYS_ANSWER_RECAP)); // RECAP
            }

            var followup = new Prompt(
                    followupMsgs,
                    OpenAiChatOptions.builder()
                            .internalToolExecutionEnabled(false)
                            .streamUsage(true)
                            .build()
            );

            Flux<StreamEvent> stream = streamingChatModel.stream(followup)
                    .map(r -> Optional.of(r.getResult())
                            .map(Generation::getOutput).map(AssistantMessage::getText).orElse(""))
                    .filter(s -> !s.isEmpty())
                    .map(StreamEvent::chunk)
                    .concatWithValues(StreamEvent.done())
                    .onErrorResume(e -> Flux.just(StreamEvent.error(EventCode.STREAM_FAIL, e.getMessage())));

            return Flux.concat(
                    Flux.fromIterable(head),
                    Flux.fromIterable(events), // 상태 이벤트 먼저 쭉
                    Flux.just(StreamEvent.status(EventCode.TOOL_START, "도구 실행을 마쳤어요. 답변을 정리할게요.")),
                    stream
            );
        }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    private static List<Map<String, Object>> getMaps(DurFilterResult dr) {
        List<Map<String,Object>> tmp = new ArrayList<>();
        if (dr.filtered() != null) {
            for (FillteredDto f : dr.filtered()) {
                Map<String,Object> row = new HashMap<>();
                row.put("name", f.name());
                if (f.effect() != null) {
                    // 한 줄 설명에 쓰기 좋게 notes[0] 대용
                    row.put("effect", f.effect());
                }
                tmp.add(row);
            }
        }
        return tmp;
    }

    // --- 유틸 ---

    private boolean hasToolCalls(ChatResponse r) {
        try {
            var gen = r.getResult();
            var out = gen.getOutput();
            var tc  = out.getToolCalls();
            return !tc.isEmpty();
        } catch (Exception e) { return false; }
    }

    private List<String> safePlannedToolNames(ChatResponse r) {
        try {
            var gen = r.getResult();
            var out = gen.getOutput();
            return out.getToolCalls().stream()
                    .map(tc -> {
                        try { return tc.name();    } catch (Throwable ignore) {}
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception ignored) {
            return List.of();
        }
    }

    // === JSON 파싱/요약 헬퍼 ===
    private Map<String,Object> parseMap(String json) {
        try { return objectMapper.readValue(json, new TypeReference<Map<String,Object>>(){}); }
        catch (Exception e) { return Map.of(); }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String,Object>> getList(Map<String,Object> m, String key) {
        Object v = m.get(key);
        return (v instanceof List) ? (List<Map<String,Object>>) v : List.of();
    }

    @SuppressWarnings("unchecked")
    private Map<String,Object> getMap(Map<String,Object> m, String key) {
        Object v = m.get(key);
        return (v instanceof Map) ? (Map<String,Object>) v : Map.of();
    }

    private String categoryOf(Map<String,Object> item, Map<String, Map<String,Object>> candidateById) {
        String id = String.valueOf(item.getOrDefault("id",""));
        Map<String,Object> meta = getMap(item, "meta");
        Object cat = meta.get("category");
        if (cat != null) return String.valueOf(cat);
        Map<String,Object> cand = candidateById.get(id);
        if (cand != null) {
            Map<String,Object> cmeta = getMap(cand, "meta");
            Object ccat = cmeta.get("category");
            if (ccat != null) return String.valueOf(ccat);
        }
        return "기타";
    }

    private AnswerMode pickMode(List<String> executedTools) {
        boolean usedDur  = executedTools.stream().anyMatch("DurFilterTool"::equalsIgnoreCase);
        boolean usedDose = executedTools.stream().anyMatch("DoseInfoTool"::equalsIgnoreCase);
        if (usedDur)  return AnswerMode.RECAP;
        if (usedDose) return AnswerMode.DOSE_INFO;
        return AnswerMode.DUR_INFO;
    }

    private String findToolCallId(ChatResponse r, String toolName) {
        try {
            return r.getResult().getOutput().getToolCalls().stream()
                    .filter(tc -> toolName.equals(tc.name()))
                    .map(AssistantMessage.ToolCall::id)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            // 예외 발생 시 null 반환
            return null;
        }
    }
}
