package com.oauth2.Drug.Prompt.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oauth2.Drug.DUR.Domain.DurType;
import com.oauth2.Drug.DUR.Service.DurService;
import com.oauth2.Drug.DetailPage.Dto.DrugDetail;
import com.oauth2.Drug.Prompt.Dto.DurResponse;
import com.oauth2.Drug.Prompt.Service.DrugPromptService;
import com.oauth2.Account.Service.AccountService;
import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Dto.ApiResponse;
import com.oauth2.User.UserInfo.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PromptController {

    private final DrugPromptService drugPromptService;
    private final DurService durService;
    private final AccountService accountService;

    @PostMapping("/detailPage/gpt")
    public ResponseEntity<ApiResponse<String>> askGPT(
            @AuthenticationPrincipal Account account,
            @RequestBody DrugDetail detail,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        String gptExplain = drugPromptService.getAsk(user, detail);
        return ResponseEntity.ok(ApiResponse.success(gptExplain));
    }


    @GetMapping("/dur/gpt")
    public ResponseEntity<ApiResponse<DurResponse>> analysisDur(
            @AuthenticationPrincipal Account account,
            @RequestParam long drugId1,
            @RequestParam long drugId2,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException, JsonProcessingException {
        User user = accountService.findUserByProfileId(profileId, account.getId());
        // 복약 완료 처리 로직
        DurResponse response =
                drugPromptService.askDur(durService.generateTagsForDurEntities(user, drugId1, drugId2, DurType.DRUG,DurType.DRUG));
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }


}
