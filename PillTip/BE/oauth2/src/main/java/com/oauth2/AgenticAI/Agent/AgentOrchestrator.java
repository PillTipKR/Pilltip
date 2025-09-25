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
import com.oauth2.Drug.DUR.Dto.DurAnalysisResponse;
import com.oauth2.Drug.DUR.Dto.DurDto;
import com.oauth2.Drug.DUR.Dto.DurTagDto;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.converter.BeanOutputConverter;
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
import reactor.core.scheduler.Schedulers;
import reactor.util.context.ContextView;

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
            - 필요한 값이 비면 AskUserTool로 1~2개만 짧게 되물음.
            - (중요) 이 단계에서는 '최종 답변 문장'을 쓰지 말 것. 가능한 한 툴 호출만 생성.
            - [DurTool 규칙] 사용자가 두 개 이상의 약품/성분 간의 상호작용을 질문하면, 아래 '호출 예시'를 참고하여 문장에서 **핵심이 되는 이름 두 개를 추출**하고 `DurTool`을 호출해야 합니다.
              - 파라미터 `name1`, `name2`는 **필수**입니다.
              - 만약 이름이 하나만 있거나 불분명하면, Tool을 호출하기 전에 먼저 `AskUserTool`을 사용해 사용자에게 명확히 물어봐야 합니다.
            
              [호출 예시]
              1. 사용자 입력: "타이레놀이랑 아스피린 같이 먹어도 괜찮아?"
                 올바른 Tool 호출: DurTool(name1="타이레놀", name2="아스피린")
            
              2. 사용자 입력: "마그네슘하고 칼슘의 상호작용이 궁금해"
                 올바른 Tool 호출: DurTool(name1="마그네슘", name2="칼슘")
            
              3. 사용자 입력: "제가 먹는 영양제랑 이 약을 같이 복용해도 될까요?"
                 올바른 Tool 호출: AskUserTool(question="영양제와 약의 이름이 어떻게 되나요?")
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
              "찾아본 top-k(TOPK)개의 제품중 SAFE_COUNT개의 제품이 NICK님의 정보를 바탕으로 진행한 DUR체크를 통해 안전하게 필터링되었어요."
              "다음과 같은 제품들이 있었어요."
              SAFE_ITEMS들을 각각 컨텍스트만을 활용하여 문맥에 자연스럽고 친절하게 다듬어 소개
            원칙: 숫자/이름이 주어지면 그대로 사용, 없으면 해당 문장은 생략하고 간단 요약만 말할 것.
            """;

    private static final String SYS_ANSWER_DUR = """
            [출력 지시]
            당신은 사용자의 건강을 염려하는 친절한 전문가입니다. 아래 지침에 따라, [DUR 정보]를 바탕으로 사용자에게 설명을 생성해 주세요.
            
            **[매우 중요한 규칙]**
            - **답변은 오직 [DUR 정보] 섹션에 명시된 `name`인 '%s'와 '%s'만을 기반으로 생성해야 합니다.**
            - **대화 기록에 이전에 언급된 다른 약물 이름(예: 아세트아미노펜, 와파린 등)은 절대 답변에 사용해서는 안 됩니다.**

            1.  **어조 및 스타일**:
                - 모든 설명은 부드러운 해요체(~요)로 작성하고, '~니다' 같은 딱딱한 표현은 사용하지 마세요.
                - 전문 용어 대신, 환자가 쉽게 이해할 수 있는 단어로 풀어 설명해 주세요.
                - 사용자가 약을 '복용하기 전' 상태임을 강조하며, '복용 전 확인해 주세요', '이런 점을 주의해야 해요' 등의 표현을 사용하세요.

            2.  **내용 구성**:
                - **첫 번째 문단**: 약 A(%s)에 대해 설명합니다. 문단은 반드시 '%s' 이름으로 시작해야 합니다.
                  - `durtags`가 있다면, 각 `title`을 빠짐없이 언급하며 `reason`과 `note`를 종합해 자연스러운 문장으로 설명하세요.
                  - `durtags`가 없다면, '복용 시 특별히 알려진 주의사항은 없어요' 와 같이 간단히 언급하세요.
                - **두 번째 문단**: 약 B(%s)에 대해 설명합니다. 약 A와 같은 방식으로 '%s' 이름으로 시작하여 설명하세요.
                - **세 번째 문단**: 두 성분의 '병용'에 대해서만 설명합니다.
                  - 병용 `durtags`가 없다면, '두 가지를 함께 복용하는 것은 특별한 문제가 없어요' 와 같이 안심시키는 내용으로 작성하세요.
                  - 병용 `durtags`가 있다면, 해당 내용을 바탕으로 왜 함께 복용하면 안 되는지, 또는 어떤 주의가 필요한지 부드럽게 설명하세요.
                  - 이 문단에서는 오직 두 조합의 상호작용만 언급하고, 다른 약과의 관계는 절대 언급하지 마세요.

            3.  **형식**:
                - 절대 마크다운을 사용하지 마세요.
                - 각 설명(약 A, 약 B, 병용)은 반드시 별개의 문단으로 명확히 구분해 주세요(줄바꿈 사용).
                - 각 문단의 길이는 180자에서 200자 사이로 작성하도록 노력해 주세요.
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
        System.out.println("sum"+sum);
        // 2) 툴 콜백 + 자동실행 OFF (플래닝 전용 옵션: detectOpts)
        ToolCallback[] callbacks = ToolCallbacks.from(
                askUserTool, doseInfoTool, productRagTool, durTool
        );
        var detectOpts = OpenAiChatOptions.builder()
                .internalToolExecutionEnabled(false)   // 플래닝만
                .parallelToolCalls(true)               // 동시 툴 계획 허용
                .toolCallbacks(callbacks)
                .build();
        System.out.println("make detect");
        return Flux.defer(() -> {
            var head   = List.of(StreamEvent.status(EventCode.INTENT_DETECTED, "질문 의도를 파악했어요."));
            var events = new ArrayList<StreamEvent>(); // 단계별 상태 문구

            List<Message> history = new ArrayList<>(msgs);
            System.out.println(history);
            ChatResponse resp = chatModel.call(new Prompt(history, detectOpts));
            System.out.println("make resp");
            // DUR 결과 집계에 필요한 변수
            int topK = 0;
            int safeCount = 0;
            List<Map<String,Object>> safeItems = new ArrayList<>(); // [{id,name,effect,meta?}]
            Map<String, Map<String,Object>> candidateById = new HashMap<>();
            DurAnalysisResponse durInfoJsonResponse = null;

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

            }
            if(planned.contains("DurTool")){
                // --- 1. 제품 탐색 단계 ---
                events.add(StreamEvent.status(EventCode.DUR_CHECK_START, "복용 상호작용(DUR)을 확인 중이에요."));

                ToolExecutionResult durexec = null;
                try {
                    if (userId != null) SessionUtils.set(session, userId, nick);
                    // ProductRagTool만 지정해서 실행
                    durexec = toolManager.executeToolCalls(new Prompt(history, detectOpts), resp);
                } catch(Exception e) {
                    e.printStackTrace();
                } finally {
                    SessionUtils.clear();
                }

                if (durexec == null) {
                    // 에러 상황이므로 더 이상 진행하지 않고 Flux를 반환해야 할 수 있습니다.
                    return Flux.just(StreamEvent.error(EventCode.BAD_REQUEST, "DurTool 실행에 실패했습니다."));
                }

                List<Message> his = new ArrayList<>(durexec.conversationHistory());
                System.out.println(history);
                Message lastMessage = his.get(his.size() - 1);
                if (lastMessage instanceof ToolResponseMessage trm) {
                    var conv = new BeanOutputConverter<>(DurAnalysisResponse.class);
                    // conv.getFormat()을 프롬프트/툴 포맷 힌트로 넣고…
                    assert trm.getText() != null;
                    durInfoJsonResponse = conv.convert(trm.getResponses().get(0).responseData());

                    executedTools.add("DurTool");

                    events.add(StreamEvent.status(EventCode.DUR_CHECK_RESULT, "DUR 확인 결과를 정리하고 있어요."));
                }
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
                String finalText = Optional.ofNullable(answer.getResult())
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

            if (mode == AnswerMode.RECAP && (topK > 0 || safeCount > 0 || !safeItems.isEmpty())) {
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
            }

            if (mode == AnswerMode.DUR_INFO) {
                if(durInfoJsonResponse != null) {
                    String template = createDurPrompt(durInfoJsonResponse);

                    followupMsgs.add(new SystemMessage(template));
                }else {
                    String error = """
                            dur 상호작용을 올바르게 찾아내지못하였습니다. 사용자에게 정중히 재입력을 요청해주세요.
                            """;

                    followupMsgs.add(new SystemMessage(error));
                }
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

            // 👇 스트림 결과를 수집하여 메모리에 저장하는 로직 추가
            StringBuilder finalAnswer = new StringBuilder();

            Flux<StreamEvent> stream = streamingChatModel.stream(followup)
                    .map(r -> Optional.ofNullable(r.getResult())
                            .map(Generation::getOutput).map(AssistantMessage::getText).orElse(""))
                    .filter(s -> !s.isEmpty())
                    .doOnNext(finalAnswer::append) // 스트리밍되는 각 조각을 StringBuilder에 추가합니다.
                    .map(StreamEvent::chunk)
                    .concatWithValues(StreamEvent.done())
                    .doOnComplete(() -> {
                        // 스트림이 성공적으로 완료되면, 수집된 전체 답변을 메모리에 저장합니다.
                        if (!finalAnswer.toString().isBlank()) {
                            memory.appendToolResult(session, userText, String.valueOf(finalAnswer));
                        }
                    })
                    .onErrorResume(e -> {
                        // log.error("스트림 처리 중 에러 발생", e); // 에러 로깅을 추가하면 디버깅에 좋습니다.
                        return Flux.just(StreamEvent.error(EventCode.STREAM_FAIL, e.getMessage()));
                    });

            return Flux.concat(
                    Flux.fromIterable(head),
                    events.stream().map(Flux::just).reduce(Flux.empty(), Flux::concat),
                    Flux.just(StreamEvent.status(EventCode.TOOL_START, "도구 실행을 마쳤어요. 답변을 정리할게요.")),
                    stream
            );
        }).subscribeOn(Schedulers.boundedElastic())
        .contextCapture();
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

    public String createDurPrompt(DurAnalysisResponse response) {
        // durtag 정보를 LLM이 이해하기 쉬운 문자열 형태로 변환합니다.
        String durA_tags = formatDurtagsForPrompt(response.durA().durtags());
        String durB_tags = formatDurtagsForPrompt(response.durB().durtags());
        String interaction_tags = formatDurtagsForPrompt(response.interact().durtags());

        // 최종 프롬프트 템플릿
        String promptTemplate = """
            [컨텍스트]
            사용자는 아직 어떤 약이나 건강기능식품도 복용하고 있지 않습니다.
            사용자는 두 가지 성분을 함께 복용하기 전, 안전성에 대한 정보를 확인하고 싶어합니다.
            따라서, 사용자의 입장에서 복용 전 알아야 할 주의사항을 친절하고 이해하기 쉽게 설명해야 합니다.
            '복용 중인', '드시고 계신' 등의 표현은 절대 사용하면 안 됩니다.

            [DUR 정보]
            - 약/건강기능식품 A:
              - name: %s
              - durtags:
            %s

            - 약/건강기능식품 B:
              - name: %s
              - durtags:
            %s

            - 병용 DUR (A + B):
              - name: %s
              - durtags:
            %s
            """;

        return promptTemplate.formatted(
                response.durA().drugName(),
                durA_tags,
                response.durB().drugName(),
                durB_tags,
                response.interact().drugName(),
                interaction_tags
        );
    }

    /**
     * DurTag 리스트를 LLM 프롬프트에 삽입하기 좋은 형태의 문자열로 변환하는 헬퍼 메소드입니다.
     * @param durtags 변환할 DurTag 리스트
     * @return YAML과 유사한 형식의 문자열
     */
    private String formatDurtagsForPrompt(List<DurTagDto> durtags) {
        StringBuilder sb = new StringBuilder();
        if (durtags == null || durtags.isEmpty()) {
            sb.append("[]\n");
            return "";
        }
        sb.append("[\n");
        for (DurTagDto tag : durtags) {
            sb.append("  {\n");
            sb.append("    title: ").append(tag.title()).append(",\n");
            sb.append("    durDtos: [\n");
            for (DurDto dto : tag.durDtos()) {
                sb.append("      { name: ").append(dto.name())
                        .append(", reason: ").append(dto.reason())
                        .append(", note: ").append(dto.note()).append(" },\n");
            }
            sb.append("    ]\n");
            sb.append("  },\n");
        }
        sb.append("]\n");
        return sb.toString();
    }
}
