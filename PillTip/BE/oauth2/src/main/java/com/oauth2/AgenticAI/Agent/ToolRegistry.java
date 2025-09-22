package com.oauth2.AgenticAI.Agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.AgenticAI.Tool.*;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import org.springframework.ai.tool.definition.ToolDefinition;   // ★ 핵심 변경

import java.lang.reflect.Method;
import java.util.*;
import java.util.Arrays;

@Component
public class ToolRegistry {

    private final ObjectMapper om;
    private final ApplicationContext ctx;

    private final ToolCallback[] callbacks;

    public ToolRegistry(ObjectMapper om, ApplicationContext ctx,
                        DurTools dur, AskUserTool ask, ProductRagTool rag,
                        DoseInfoTool dose, DurFilterTool filter) {
        this.om = om;
        this.ctx = ctx;
        // ★ 실제 등록된 @Tool 콜백들 모두 포함
        this.callbacks = ToolCallbacks.from(dur, ask, rag, dose, filter);
    }

    public ToolCallback[] toolCallbacks() {
        return callbacks;
    }



    /** LLM에 보여줄 스펙(이름을 @Tool과 1:1로 정정) */
    public List<ToolDefinition> specs() {
        // DurCheckTool (DurTools)
        String durSchema = """
          {
            "type": "object",
            "properties": {
              "drugs": { "type": "array", "items": { "type": "string" } },
              "supps": { "type": "array", "items": { "type": "string" } }
            },
            "required": ["drugs"]
          }
        """;

        // ProductRagTool (RagTools 내부 @Tool 이름을 ProductRagTool로 맞춰주세요)
        String productRagSchema = """
          {
            "type": "object",
            "properties": {
              "need": { "type": "string" },
              "constraints": { "type": "object" },
              "profile": { "type": "object" }
            },
            "required": ["need"]
          }
        """;

        // DoseInfoTool
        String doseSchema = """
          {
            "type": "object",
            "properties": {
              "item": { "type": "string" },
              "context": { "type": "string" }
            },
            "required": ["item"]
          }
        """;

        // DurFilterTool
        String durFilterSchema = """
          {
            "type": "object",
            "properties": {
              "candidates": {
                "type": "array",
                "items": {
                  "type": "object",
                  "properties": {
                    "id":   { "type": "string" },
                    "name": { "type": "string" },
                    "meta": { "type": "object" }
                  },
                  "required": ["id","name"]
                }
              },
              "profile": { "type": "object" }
            },
            "required": ["candidates"]
          }
        """;

        // AskUserTool
        String askSchema = """
          {
            "type": "object",
            "properties": {
              "question": { "type": "string" },
              "missingSlots": { "type": "array", "items": { "type": "string" } }
            },
            "required": ["question"]
          }
        """;

        return List.of(
                ToolDefinition.builder()
                        .name("DurCheckTool")
                        .description("의약품/건기식 병용 주의·금기 조회")
                        .inputSchema(durSchema)
                        .build(),

                ToolDefinition.builder()
                        .name("ProductRagTool")
                        .description("요구조건에 맞는 제품 후보 3~8개 검색")
                        .inputSchema(productRagSchema)
                        .build(),

                ToolDefinition.builder()
                        .name("DoseInfoTool")
                        .description("섭취량/복용 시간/간격/1일 허용량 등 용법 정보 제공")
                        .inputSchema(doseSchema)
                        .build(),

                ToolDefinition.builder()
                        .name("DurFilterTool")
                        .description("사용자 프로필 기준으로 제품 후보 DUR 필터링")
                        .inputSchema(durFilterSchema)
                        .build(),

                ToolDefinition.builder()
                        .name("AskUserTool")
                        .description("부족한 정보를 사용자에게 질문(해당 턴 종료)")
                        .inputSchema(askSchema)
                        .build()
        );
    }


    /**
     * 모델이 선택한 tool_call을 수동 실행.
     * - @Tool 메서드의 파라미터 타입을 읽어서, JsonNode를 해당 타입으로 변환해 인자로 전달.
     * - 파라미터가 없으면 인자 없이 실행.
     * - 파라미터가 Map이면 Map으로, 레코드/POJO면 DTO로 변환.
     */
    public String call(String name, JsonNode args) throws Exception {
        Object bean = switch (name) {
            case "DurCheckTool" -> ctx.getBean(DurTools.class);
            case "QueryRagTool" -> ctx.getBean(ProductRagTool.class);
            case "AskUserTool"  -> ctx.getBean(AskUserTool.class);
            default -> throw new IllegalArgumentException("Unknown tool: " + name);
        };

        Method method = Arrays.stream(bean.getClass().getMethods())
                .filter(m -> m.isAnnotationPresent(org.springframework.ai.tool.annotation.Tool.class)
                        && m.getAnnotation(org.springframework.ai.tool.annotation.Tool.class).name().equals(name))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No @Tool method named " + name));

        Object result;
        var paramTypes = method.getParameterTypes();

        if (paramTypes.length == 0) {
            result = method.invoke(bean);
        } else if (paramTypes.length == 1) {
            Class<?> pt = paramTypes[0];

            Object argObj;
            if (Map.class.isAssignableFrom(pt)) {
                // 기존 Map 시그니처도 그대로 지원
                argObj = om.convertValue(args, Map.class);
            } else if (pt.equals(String.class)) {
                // String 단일 인자 시그니처 지원
                // args가 {"question":"..."} 꼴이면 question 필드를 우선 시도
                if (args != null && args.hasNonNull("question")) {
                    argObj = args.get("question").asText();
                } else {
                    // 그냥 루트가 문자열인 경우 대비
                    argObj = om.treeToValue(args, String.class);
                }
            } else {
                // 레코드/POJO DTO → 자동 매핑
                argObj = om.treeToValue(args, pt);
            }

            result = method.invoke(bean, argObj);
        } else {
            throw new IllegalArgumentException("Tool method must have 0 or 1 parameter: " + name);
        }

        return om.writeValueAsString(result);
    }
}
