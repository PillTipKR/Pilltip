package com.oauth2.AgenticAI.Dto.Orchestrator;

// 1) enum: JSON 값은 기존 문자열과 동일하게 직렬화
public enum EventType {
    STATUS("status"), ANSWER_CHUNK("answer_chunk"), DONE("done"), ERROR("error"), ANSWER("answer");
    private final String json;
    EventType(String json){ this.json=json; }
    @com.fasterxml.jackson.annotation.JsonValue
    public String json(){ return json; }
}
