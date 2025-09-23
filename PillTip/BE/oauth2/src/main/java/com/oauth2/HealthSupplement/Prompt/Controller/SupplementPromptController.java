package com.oauth2.HealthSupplement.Prompt.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oauth2.Account.Dto.ApiResponse;
import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Service.AccountService;
import com.oauth2.Drug.DUR.Domain.DurType;
import com.oauth2.Drug.DUR.Service.DurService;
import com.oauth2.Drug.Prompt.Dto.DurResponse;
import com.oauth2.HealthSupplement.DetailPage.Dto.SupplementDetail;
import com.oauth2.HealthSupplement.Prompt.Service.SupplementPromptService;
import com.oauth2.User.UserInfo.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/supplement")
@RequiredArgsConstructor
public class SupplementPromptController {

    private final SupplementPromptService supplementPromptService;
    private final AccountService accountService;
    private final DurService durService;

    @PostMapping("/detailPage/gpt")
    public ResponseEntity<ApiResponse<String>> askGPT(
            @AuthenticationPrincipal Account account,
            @RequestBody SupplementDetail detail,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        String gptExplain = supplementPromptService.getAsk(user, detail);
        return ResponseEntity.ok(ApiResponse.success(gptExplain));
    }


    @GetMapping("/dur/gpt")
    public ResponseEntity<ApiResponse<DurResponse>> analysisDur(
            @AuthenticationPrincipal Account account,
            @RequestParam long drugId,
            @RequestParam long supplementId,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException, JsonProcessingException {
        User user = accountService.findUserByProfileId(profileId, account.getId());
        // 복약 완료 처리 로직
        DurResponse response =
                supplementPromptService.askDur(durService.generateTagsForDurEntities(user, supplementId, drugId, DurType.SUPPLEMENT,DurType.DRUG));
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }


}
