package com.oauth2.AgenticAI.Dto.Orchestrator;

import java.util.Map;

// 2) record: of(...)는 유지, 의미별 헬퍼 추가
public record StreamEvent(
        EventType type,
        EventCode code,
        String message,
        Map<String, Object> meta,
        long ts
) {
    public static StreamEvent of(String type, String code, String msg, Map<String,Object> meta){
        // 레거시 호출부 호환 (문자열 → enum 매핑; 실패 시 예외 또는 기본값)
        return new StreamEvent(
                EventType.valueOf(type.toUpperCase()),
                EventCode.valueOf(code.toUpperCase()),
                msg,
                meta == null ? Map.of() : meta,
                System.currentTimeMillis()
        );
    }

    // 의미별 팩토리(권장 호출 경로)
    // 편의 오버로드(메타 없이도 호출 가능)
    public static StreamEvent status(EventCode code, String msg) {
        return status(code, msg, Map.of()); // 내부에서 빈 맵 채움
    }
    public static StreamEvent status(EventCode code, String msg, Map<String,Object> meta){
        return new StreamEvent(EventType.STATUS, code, msg, meta == null ? Map.of() : meta, System.currentTimeMillis());
    }
    public static StreamEvent chunk(String token){
        return new StreamEvent(EventType.ANSWER_CHUNK, EventCode.CHUNK, token, Map.of(), System.currentTimeMillis());
    }
    public static StreamEvent done(){
        return new StreamEvent(EventType.DONE, EventCode.END, "답변을 마쳤어요.", Map.of(), System.currentTimeMillis());
    }
    public static StreamEvent error(EventCode code, String msg){
        return new StreamEvent(EventType.ERROR, code, msg, Map.of(), System.currentTimeMillis());
    }
    public static StreamEvent answer(String text) {
        return new StreamEvent(EventType.ANSWER, EventCode.ANSWER, text, Map.of(), System.currentTimeMillis());
    }
}
