package com.oauth2.Drug.Search.Controller;

import com.oauth2.Drug.DUR.Dto.SearchDurDto;
import com.oauth2.Drug.DUR.Service.DrugDurTaggingService;
import com.oauth2.Drug.Search.Dto.SearchIndexDTO;
import com.oauth2.Drug.Search.Service.DrugSearchService;
import com.oauth2.Account.Service.AccountService;
import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Dto.ApiResponse;
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
public class DrugSearchController {

    @Value("${elastic.drug.drug}")
    private String drug;

    @Value("${elastic.drug.manufacturer}")
    private String manufacturer;
    @Value("${elastic.drug.ingredient}")
    private String ingredient;
    @Value("${elastic.page}")
    private int pageSize;

    private final DrugSearchService drugSearchService;
    private final DrugDurTaggingService drugDurTaggingService;
    private final AccountService accountService;

    @GetMapping("/drugs")
    public ResponseEntity<ApiResponse<List<SearchDurDto>>> getDrugSearch(
            @AuthenticationPrincipal Account account,
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws IOException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        return getTagSearch(user,input,page,drug);
    }

    @GetMapping("/manufacturers")
    public ResponseEntity<ApiResponse<List<SearchDurDto>>> getManufacturerSearch(
            @AuthenticationPrincipal Account account,
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws IOException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        return getTagSearch(user,input,page,manufacturer);
    }

    @GetMapping("/ingredients")
    public ResponseEntity<ApiResponse<List<SearchDurDto>>> getIngredientSearch(
            @AuthenticationPrincipal Account account,
            @RequestParam String input,
            @RequestParam(defaultValue="0") int page,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws IOException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        return getTagSearch(user,input,page,ingredient);
    }

    private ResponseEntity<ApiResponse<List<SearchDurDto>>> getTagSearch(
            User user, String input, int page, String field) throws IOException {
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated", null));
        }
        List<SearchIndexDTO> searchIndexDTOList = drugSearchService.getDrugSearch(input, field, pageSize, page);
        List<SearchDurDto> result = drugDurTaggingService.generateTagsForDrugs(user, searchIndexDTOList);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
