package com.oauth2.AgenticAI.Dto.DoseInfo;

import com.oauth2.AgenticAI.Dto.Profile;

public record DoseInfoRequest(
        String item,
        Profile profile,
        String context // "야간 복용 가능?" 같은 free text
) {}
