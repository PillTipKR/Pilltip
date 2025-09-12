package com.oauth2.User.TakingPill.Controller;

import com.oauth2.Account.Service.AccountService;
import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Dto.ApiResponse;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.Friend.Service.FriendService;
import com.oauth2.User.TakingPill.Dto.AllDosageLogResponse;
import com.oauth2.User.TakingPill.Dto.WeekDosageLogResponse;
import com.oauth2.User.TakingPill.Service.DosageLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/dosageLog")
@RequiredArgsConstructor
public class DosageLogController {

    private final DosageLogService dosageLogService;
    private final FriendService friendService;
    private final AccountService accountService;

    @GetMapping("/date")
    public ResponseEntity<ApiResponse<AllDosageLogResponse>> getDateLogs(
           @AuthenticationPrincipal Account account,
           @RequestParam LocalDate date,
           @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        AllDosageLogResponse responses = dosageLogService.getDateLog(user.getId(), date);
        return ResponseEntity.ok().body(ApiResponse.success(responses));
    }


    @GetMapping("/week")
    public ResponseEntity<ApiResponse<WeekDosageLogResponse>> getWeekLogs(
            @AuthenticationPrincipal Account account,
            @RequestParam LocalDate date,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        WeekDosageLogResponse responses = dosageLogService.getWeeklySummary(user.getId(), date);
        return ResponseEntity.ok().body(ApiResponse.success(responses));
    }

    @GetMapping("/{friendId}/date")
    public ResponseEntity<ApiResponse<AllDosageLogResponse>> getFriendDateLogs(
            @AuthenticationPrincipal Account account,
            @PathVariable Long friendId,
            @RequestParam LocalDate date,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        friendService.assertIsFriend(user.getId(), friendId);
        AllDosageLogResponse responses = dosageLogService.getDateLog(friendId, date);
        return ResponseEntity.ok().body(ApiResponse.success(responses));
    }

    @PostMapping("/{logId}/taken")
    public ResponseEntity<ApiResponse<String>> markAsTaken(@PathVariable Long logId) {
        // 복약 완료 처리 로직
        dosageLogService.updateTaken(logId);
        return ResponseEntity.ok().body(ApiResponse.success("복용 이력 수정!"));
    }


}
