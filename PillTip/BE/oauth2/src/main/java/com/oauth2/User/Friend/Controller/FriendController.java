package com.oauth2.User.Friend.Controller;

import com.oauth2.Account.Service.AccountService;
import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Dto.ApiResponse;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.Friend.Dto.FriendListDto;
import com.oauth2.User.Friend.Dto.FriendMessageConstants;
import com.oauth2.User.Friend.Service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {

    @Value("${server.link}")
    private String server;

    private final FriendService friendService;
    private final AccountService accountService;

    /**
     * 친구 초대용 링크(JWT 포함) 생성
     */
    @PostMapping("/invite")
    public ResponseEntity<?> generateInviteLink(
            @AuthenticationPrincipal Account account,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        Long userId = user.getId();
        String token = friendService.generateInviteToken(userId);

        String inviteUrl = server + "/api/friend/inviting/" + token;

        return ResponseEntity.ok(Map.of("inviteUrl", inviteUrl));
    }

    @GetMapping("/inviting/{token}")
    public RedirectView redirectToHtml(@PathVariable String token) {
        String url = server + "/invite.html?token=" + token;
        return new RedirectView(url);
    }


    /**
     * 친구 초대 수락 → 친구 관계 등록
     */
    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<String>> acceptInvite(
            @RequestBody Map<String, String> requestBody,
            @AuthenticationPrincipal Account account,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        try {
            String inviteToken = requestBody.get("inviteToken");
            if (inviteToken == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error(FriendMessageConstants.INVITE_TOKEN_REQUIRED, null));
            }

            Long receiverId = user.getId();
            friendService.acceptInvite(inviteToken, receiverId);

            return ResponseEntity.ok().body(ApiResponse.success(FriendMessageConstants.FRIEND_ADD_SUCCESS));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(FriendMessageConstants.FRIEND_ADD_FAILED, null));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<FriendListDto>>> listFriends(
            @AuthenticationPrincipal Account account,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        return ResponseEntity.ok().body(ApiResponse.success(friendService.getFriends(user.getId())));
    }
}
