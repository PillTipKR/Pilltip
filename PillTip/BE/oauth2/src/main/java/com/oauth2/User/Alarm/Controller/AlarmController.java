package com.oauth2.User.Alarm.Controller;

import com.oauth2.Account.Service.AccountService;
import com.oauth2.Account.Entity.Account;
import com.oauth2.User.Alarm.Service.AlarmService;
import com.oauth2.Account.Dto.ApiResponse;
import com.oauth2.User.TakingSupplement.Service.SupplementLogService;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.UserInfo.Repository.UserRepository;
import com.oauth2.User.Alarm.Dto.AlarmMessageConstants;
import com.oauth2.User.Friend.Service.FriendService;
import com.oauth2.User.TakingPill.Entity.DosageLog;
import com.oauth2.User.TakingPill.Service.DosageLogService;
import com.oauth2.Util.Exception.CustomException.NotExistDosageLogException;
import com.oauth2.Util.Exception.CustomException.NotExistUserException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;
    private final DosageLogService dosageLogService;
    private final SupplementLogService supplementLogService;
    private final UserRepository userRepository;
    private final FriendService friendService;
    private final AccountService accountService;

    /* FCM Token 서버 저장 API */
    @PostMapping("/token")
    public String getToken(@AuthenticationPrincipal Account account, @RequestParam String token,
                           @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        alarmService.getToken(user.getId(), token);
        return "OK";
    }


    @PostMapping("/drug/{logId}/pending")
    public ResponseEntity<ApiResponse<String>> markPending(@PathVariable Long logId) {
        try {
            dosageLogService.markPending(logId);
            return ResponseEntity.ok().body(ApiResponse.success(AlarmMessageConstants.ALARM_RESEND_SUCCESS));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(AlarmMessageConstants.ALARM_RESEND_FAILED, null));
        }
    }


    @PostMapping("/drug/{logId}/taken")
    public ResponseEntity<ApiResponse<String>> markAsDrugTaken(
            @PathVariable Long logId) {
        try {
            dosageLogService.alarmTaken(logId);
            return ResponseEntity.ok().body(ApiResponse.success(AlarmMessageConstants.DOSAGE_HISTORY_UPDATE_SUCCESS));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(AlarmMessageConstants.DOSAGE_HISTORY_UPDATE_FAILED, null));
        }
    }

    @PostMapping("/supplement/{logId}/pending")
    public ResponseEntity<ApiResponse<String>> markSupplementPending(@PathVariable Long logId) {
        try {
            supplementLogService.markPending(logId);
            return ResponseEntity.ok().body(ApiResponse.success(AlarmMessageConstants.ALARM_RESEND_SUCCESS));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(AlarmMessageConstants.ALARM_RESEND_FAILED, null));
        }
    }

    @PostMapping("/supplement/{logId}/taken")
    public ResponseEntity<ApiResponse<String>> markAsSupplementTaken(
            @PathVariable Long logId) {
        try {
            supplementLogService.alarmTaken(logId);
            return ResponseEntity.ok().body(ApiResponse.success(AlarmMessageConstants.DOSAGE_HISTORY_UPDATE_SUCCESS));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(AlarmMessageConstants.DOSAGE_HISTORY_UPDATE_FAILED, null));
        }
    }

    // 안 먹은 친구 콕 찌르기
    @GetMapping("/{friendId}/{logId}")
    public ResponseEntity<ApiResponse<String>> reminder(
            @AuthenticationPrincipal Account account,
            @PathVariable Long friendId, @PathVariable Long logId,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        try {
            User friend = userRepository.findByFriendId(friendId)
                    .orElseThrow(NotExistUserException::new);

            friendService.assertIsFriend(user.getId(), friendId);

            DosageLog dosageLog = dosageLogService.getDosageLog(logId);
            if(dosageLog == null) throw new NotExistDosageLogException();
            alarmService.sendFriendMedicationReminder(
                    friend.getAccount().getFCMToken(),
                    dosageLog.getId(),
                    user.getNickname(),
                    dosageLog.getMedicationName(),
                    dosageLog.getScheduledTime()
            );

            return ResponseEntity.ok(ApiResponse.success(AlarmMessageConstants.FRIEND_ALARM_SEND_SUCCESS));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(AlarmMessageConstants.FRIEND_ALARM_SEND_FAILED, null));
        }
    }
}
