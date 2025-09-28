package com.oauth2.AgenticAI.Dto.DoseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class AgeSynonymCategory {
    // Getters (Setter는 JSON 읽기 전용이므로 필요 없음)
    @JsonProperty("category")
    private String category;
    @JsonProperty("synonyms")
    private List<String> synonyms;
    @JsonProperty("requires_clarification")
    private boolean requiresClarification;
    @JsonProperty("clarification_question")
    private String clarificationQuestion;
    @JsonProperty("representative_age")
    private String representativeAge;
}
