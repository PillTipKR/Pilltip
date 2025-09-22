package com.oauth2.AgenticAI.Dto.ProductTool;

import java.util.List;

public record DurFilterResult(
        List<FillteredDto> filtered,
        int count
) {}
