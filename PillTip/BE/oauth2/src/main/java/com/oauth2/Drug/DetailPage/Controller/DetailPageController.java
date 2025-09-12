package com.oauth2.Drug.DetailPage.Controller;

import com.oauth2.Drug.DetailPage.Dto.DrugDetail;
import com.oauth2.Drug.DetailPage.Service.DrugDetailService;
import com.oauth2.Account.Service.AccountService;
import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Dto.ApiResponse;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.Util.Exception.CustomException.InvalidProfileIdException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/detailPage")
@RequiredArgsConstructor
public class DetailPageController {

    private final DrugDetailService drugDetailService;
    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<ApiResponse<DrugDetail>> detailPage(
            @AuthenticationPrincipal Account account,
            @RequestParam long id,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId) throws IOException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        if (user == null) throw new InvalidProfileIdException();

        DrugDetail detail = drugDetailService.getDetail(user, id);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

}
