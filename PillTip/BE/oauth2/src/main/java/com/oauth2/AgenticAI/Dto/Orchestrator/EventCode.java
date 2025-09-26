
package com.oauth2.AgenticAI.Dto.Orchestrator;

public enum EventCode {
    INTENT_DETECTED("INTENT"),
    TOOL_START("TOOL_START"),
    TOOL_RESULT("TOOL_RESULT"),
    CHUNK("CHUNK"),
    END("END"),
    STREAM_FAIL("STREAM_FAIL"),
    ANSWER("ANSWER"),
    READY("READY"),
    PRODUCT_SEARCH_START("PRODUCT_SEARCH_START"),
    PRODUCT_SEARCH_RESULT("PRODUCT_SEARCH_RESULT"),
    DUR_CHECK_START("DUR_CHECK_START"),
    DUR_CHECK_RESULT("DUR_CHECK_RESULT"),
    DOSE_INFO_CHECK_START("DOSE_INFO_CHECK_START"),
    DOSE_INFO_CHECK_RESULT("DOSE_INFO_CHECK_RESULT"),
    BAD_REQUEST("BAD_REQUEST"),
    ASK_USER("ASK_USER");
    private final String json;
    EventCode(String json){ this.json=json; }
    @com.fasterxml.jackson.annotation.JsonValue
    public String json(){ return json; }
}
