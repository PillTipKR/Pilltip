package com.oauth2.User.TakingSupplement.Controller;

import com.oauth2.Account.Dto.ApiResponse;
import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Service.AccountService;
import com.oauth2.User.TakingPill.Dto.TakingPillMessageConstants;
import com.oauth2.User.TakingSupplement.Dto.TakingSupplementDetailResponse;
import com.oauth2.User.TakingSupplement.Dto.TakingSupplementRequest;
import com.oauth2.User.TakingSupplement.Dto.TakingSupplementSummaryResponse;
import com.oauth2.User.TakingSupplement.Service.TakingSupplementService;
import com.oauth2.User.UserInfo.Entity.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/taking-supplement")
@RequiredArgsConstructor
public class TakingSupplementController {

    private static final Logger logger = LoggerFactory.getLogger(TakingSupplementController.class);
    private final TakingSupplementService takingSupplementService;
    private final AccountService accountService;

    // 복용 중인 약 추가
    @PostMapping("")
    public ResponseEntity<ApiResponse<TakingSupplementDetailResponse>> addTakingSupplement(
            @AuthenticationPrincipal Account account,
            @RequestBody TakingSupplementRequest request,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        try {
            takingSupplementService.addTakingSupplement(user, request);
            TakingSupplementDetailResponse supplementDetailResponse = takingSupplementService.getTakingSupplementDetail(user);
            return ResponseEntity.status(201)
                .body(ApiResponse.success(TakingPillMessageConstants.TAKING_PILL_ADD_SUCCESS, supplementDetailResponse));
        } catch (Exception e) {
            logger.error("Taking pill add failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(TakingPillMessageConstants.TAKING_PILL_ADD_FAILED, null));
        }
    }

    // 복용 중인 약 삭제
    @DeleteMapping("/{supplementId}")
    public ResponseEntity<ApiResponse<TakingSupplementSummaryResponse>> deleteTakingSupplement(
            @AuthenticationPrincipal Account account,
            @PathVariable String supplementId,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        try {
            takingSupplementService.deleteTakingSupplement(user, supplementId);
            TakingSupplementSummaryResponse takingPillSummary = takingSupplementService.getTakingSupplementSummary(user);
            return ResponseEntity.status(200)
                .body(ApiResponse.success(TakingPillMessageConstants.TAKING_PILL_DELETE_SUCCESS, takingPillSummary));
        } catch (Exception e) {
            logger.error("Taking pill delete failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(TakingPillMessageConstants.TAKING_PILL_DELETE_FAILED, null));
        }
    }

    // 복용 중인 약 수정
    @PutMapping("")
    public ResponseEntity<ApiResponse<TakingSupplementDetailResponse>> updateTakingSupplement(
            @AuthenticationPrincipal Account account,
            @RequestBody TakingSupplementRequest request,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        try {
            takingSupplementService.updateTakingSupplement(user, request);
            TakingSupplementDetailResponse takingSupplementDetail = takingSupplementService.getTakingSupplementDetail(user);
            return ResponseEntity.status(200)
                .body(ApiResponse.success(TakingPillMessageConstants.TAKING_PILL_UPDATE_SUCCESS, takingSupplementDetail));
        } catch (NumberFormatException e) {
            logger.error("Invalid medication ID format: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(TakingPillMessageConstants.INVALID_MEDICATION_ID_FORMAT, null));
        } catch (Exception e) {
            logger.error("Taking pill update failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(TakingPillMessageConstants.TAKING_PILL_UPDATE_FAILED, null));
        }
    }

    // 복용 중인 약 조회 (요약)
    @GetMapping("")
    public ResponseEntity<ApiResponse<TakingSupplementSummaryResponse>> getTakingSupplement(
            @AuthenticationPrincipal Account account,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        try {
            TakingSupplementSummaryResponse takingSupplementSummary = takingSupplementService.getTakingSupplementSummary(user);
            return ResponseEntity.status(200)
                .body(ApiResponse.success(TakingPillMessageConstants.TAKING_PILL_RETRIEVE_SUCCESS, takingSupplementSummary));
        } catch (Exception e) {
            logger.error("Taking pill retrieve failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(TakingPillMessageConstants.TAKING_PILL_RETRIEVE_FAILED, null));
        }
    }

    // 복용 중인 약 상세 조회
    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<TakingSupplementDetailResponse>> getTakingSupplementDetail(
            @AuthenticationPrincipal Account account,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        try {
            TakingSupplementDetailResponse takingSupplementDetailResponse = takingSupplementService.getTakingSupplementDetail(user);
            return ResponseEntity.status(200)
                .body(ApiResponse.success(TakingPillMessageConstants.TAKING_PILL_DETAIL_RETRIEVE_SUCCESS, takingSupplementDetailResponse));
        } catch (Exception e) {
            logger.error("Taking pill detail retrieve failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(TakingPillMessageConstants.TAKING_PILL_DETAIL_RETRIEVE_FAILED, null));
        }
    }

    // 특정 약의 상세 정보 조회
    @GetMapping("/{supplementId}")
    public ResponseEntity<ApiResponse<TakingSupplementDetailResponse.TakingSupplementDetail>> getTakingPillDetailById(
            @AuthenticationPrincipal Account account,
            @PathVariable String supplementId,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        try {
            TakingSupplementDetailResponse.TakingSupplementDetail pillDetail = takingSupplementService.getTakingSupplementDetailById(user, Long.parseLong(supplementId));
            return ResponseEntity.status(200)
                .body(ApiResponse.success(TakingPillMessageConstants.TAKING_PILL_DETAIL_RETRIEVE_SUCCESS, pillDetail));
        } catch (NumberFormatException e) {
            logger.error("Invalid medication ID format: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(TakingPillMessageConstants.INVALID_MEDICATION_ID_FORMAT, null));
        } catch (Exception e) {
            logger.error("Taking pill detail retrieve failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(TakingPillMessageConstants.TAKING_PILL_DETAIL_RETRIEVE_FAILED, null));
        }
    }
} 