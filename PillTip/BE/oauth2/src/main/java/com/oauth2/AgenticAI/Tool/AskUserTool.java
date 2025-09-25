package com.oauth2.AgenticAI.Tool;

import com.oauth2.AgenticAI.Dto.AskUser.AskUserRequest;
import com.oauth2.AgenticAI.Dto.AskUser.AskUserResponse;
import org.springframework.stereotype.Component;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;
import java.util.Map;

@Component
public class AskUserTool {

    @Tool(
            name = "AskUserTool",
            description = "추가 정보가 필요할 때 사용자에게 질문을 전달한다. question은 필수, missingSlots는 선택."
    )
    public AskUserResponse ask(AskUserRequest req) {
        var q = req.question() == null ? "" : req.question().trim();
        var slots = req.missingSlots() == null ? List.<String>of() : req.missingSlots();
        return new AskUserResponse("ask_user", q, slots);
    }
}

