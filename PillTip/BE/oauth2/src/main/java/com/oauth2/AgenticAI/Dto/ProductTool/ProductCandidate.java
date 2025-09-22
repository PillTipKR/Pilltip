package com.oauth2.AgenticAI.Dto.ProductTool;

import java.util.Map;

public record ProductCandidate(
        String id, String name, Map<String,Object> meta
) {}
