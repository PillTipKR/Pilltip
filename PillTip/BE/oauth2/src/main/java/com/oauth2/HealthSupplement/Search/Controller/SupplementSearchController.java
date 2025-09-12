package com.oauth2.HealthSupplement.Search.Controller;

import com.oauth2.Account.Dto.ApiResponse;
import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Service.AccountService;
import com.oauth2.HealthSupplement.DUR.Dto.SupplementSearchDurDto;
import com.oauth2.HealthSupplement.DUR.Service.SupplementDurTaggingService;
import com.oauth2.HealthSupplement.Search.Dto.SupplementSearchIndexDto;
import com.oauth2.HealthSupplement.Search.Service.SupplementSearchService;
import com.oauth2.User.UserInfo.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SupplementSearchController {

    @Value("${elastic.supplement.name}")
    private String supplementName;

    @Value("${elastic.supplement.enterprise}")
    private String enterprise;
    @Value("${elastic.supplement.ingredient}")
    private String ingredient;

    @Value("${elastic.page}")
    private int pageSize;

    private final SupplementSearchService supplementSearchService;
    private final SupplementDurTaggingService supplementDurTaggingService;
    private final AccountService accountService;

    @GetMapping("/supplements")
    public ResponseEntity<ApiResponse<List<SupplementSearchDurDto>>> getSupplementSearch(
            @AuthenticationPrincipal Account account,
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws IOException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        return getTagSearch(user,input,page,supplementName);
    }

    @GetMapping("/enterprises")
    public ResponseEntity<ApiResponse<List<SupplementSearchDurDto>>> getEnterpriseSearch(
            @AuthenticationPrincipal Account account,
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws IOException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        return getTagSearch(user,input,page,enterprise);
    }

    @GetMapping("/supplementIngredients")
    public ResponseEntity<ApiResponse<List<SupplementSearchDurDto>>> getIngredientSearch(
            @AuthenticationPrincipal Account account,
            @RequestParam String input,
            @RequestParam(defaultValue="0") int page,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws IOException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        return getTagSearch(user,input,page,ingredient);
    }

    private ResponseEntity<ApiResponse<List<SupplementSearchDurDto>>> getTagSearch(
            User user, String input, int page, String field) throws IOException {
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated", null));
        }
        List<SupplementSearchIndexDto> searchIndexDTOList = supplementSearchService.getSupplementSearch(input, field, pageSize, page);
        List<SupplementSearchDurDto> result = supplementDurTaggingService.generateTagsForSupplements(user, searchIndexDTOList);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
