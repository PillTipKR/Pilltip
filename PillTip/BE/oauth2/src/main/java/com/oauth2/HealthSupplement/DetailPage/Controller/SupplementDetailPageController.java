package com.oauth2.HealthSupplement.DetailPage.Controller;

import com.oauth2.Account.Dto.ApiResponse;
import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Service.AccountService;
import com.oauth2.HealthSupplement.DetailPage.Dto.SupplementDetail;
import com.oauth2.HealthSupplement.DetailPage.Service.SupplementDetailService;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.Util.Exception.CustomException.InvalidProfileIdException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/supplement/detailPage")
@RequiredArgsConstructor
public class SupplementDetailPageController {

    private final SupplementDetailService supplementDetailService;
    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<ApiResponse<SupplementDetail>> detailPage(
            @AuthenticationPrincipal Account account,
            @RequestParam long id,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId) throws IOException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        if (user == null) throw new InvalidProfileIdException();

        SupplementDetail supplementDetail = supplementDetailService.getDetail(user, id);
        return ResponseEntity.ok(ApiResponse.success(supplementDetail));
    }

}
