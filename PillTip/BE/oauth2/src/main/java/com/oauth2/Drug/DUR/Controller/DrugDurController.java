package com.oauth2.Drug.DUR.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oauth2.Drug.DUR.Domain.DurType;
import com.oauth2.Drug.DUR.Dto.DurAnalysisResponse;
import com.oauth2.Drug.DUR.Service.DurService;
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
@RequiredArgsConstructor
@RequestMapping("/api")
public class DrugDurController {

    private final DurService durService;
    private final AccountService accountService;

    @GetMapping("/dur")
    public ResponseEntity<ApiResponse<DurAnalysisResponse>> analysisDur(
            @AuthenticationPrincipal Account account,
            @RequestParam long drugId1,
            @RequestParam long drugId2,
            @RequestHeader("X-Profile-Id") Long profileId) throws AccessDeniedException, JsonProcessingException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        // 복약 완료 처리 로직
        DurAnalysisResponse durAnalysisResponse = durService.generateTagsForDurEntities(user, drugId1, drugId2, DurType.DRUG, DurType.DRUG);
        return ResponseEntity.ok().body(ApiResponse.success(durAnalysisResponse));
    }
}
