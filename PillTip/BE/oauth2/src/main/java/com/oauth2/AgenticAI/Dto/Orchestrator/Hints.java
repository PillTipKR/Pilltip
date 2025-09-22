package com.oauth2.AgenticAI.Dto.Orchestrator;

import java.util.*;

public record Hints(
        Anchors anchors,
        Constraints constraints,
        String intentHint,
        List<String> riskFlags,
        List<String> allowFromContext,
        Map<String, List<String>> synonyms
) {
    public record Anchors(List<String> current, List<String> context, List<String> soft) {}
    public record Constraints(List<String> mustInclude, List<String> mustNot) {}
}

