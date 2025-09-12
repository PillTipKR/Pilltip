package com.oauth2.User.TakingPill.Controller;

import com.oauth2.Account.Service.AccountService;
import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Dto.ApiResponse;
import com.oauth2.User.TakingPill.Dto.TakingPillRequest;
import com.oauth2.User.TakingPill.Dto.TakingPillSummaryResponse;
import com.oauth2.User.TakingPill.Dto.TakingPillDetailResponse;
import com.oauth2.User.TakingPill.Dto.TakingPillMessageConstants;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.TakingPill.Service.TakingPillService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/taking-pill")
@RequiredArgsConstructor
public class TakingPillController {

    private static final Logger logger = LoggerFactory.getLogger(TakingPillController.class);
    private final TakingPillService takingPillService;
    private final AccountService accountService;

    // 복용 중인 약 추가
    @PostMapping("")
    public ResponseEntity<ApiResponse<TakingPillDetailResponse>> addTakingPill(
            @AuthenticationPrincipal Account account,
            @RequestBody TakingPillRequest request,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        try {
            takingPillService.addTakingPill(user, request);
            TakingPillDetailResponse takingPillDetail = takingPillService.getTakingPillDetail(user);
            return ResponseEntity.status(201)
                .body(ApiResponse.success(TakingPillMessageConstants.TAKING_PILL_ADD_SUCCESS, takingPillDetail));
        } catch (Exception e) {
            logger.error("Taking pill add failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(TakingPillMessageConstants.TAKING_PILL_ADD_FAILED, null));
        }
    }

    // 복용 중인 약 삭제
    @DeleteMapping("/{medicationId}")
    public ResponseEntity<ApiResponse<TakingPillSummaryResponse>> deleteTakingPill(
            @AuthenticationPrincipal Account account,
            @PathVariable String medicationId,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        try {
            takingPillService.deleteTakingPill(user, medicationId);
            TakingPillSummaryResponse takingPillSummary = takingPillService.getTakingPillSummary(user);
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
    public ResponseEntity<ApiResponse<TakingPillDetailResponse>> updateTakingPill(
            @AuthenticationPrincipal Account account,
            @RequestBody TakingPillRequest request,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        try {
            takingPillService.updateTakingPill(user, request);
            TakingPillDetailResponse takingPillDetail = takingPillService.getTakingPillDetail(user);
            return ResponseEntity.status(200)
                .body(ApiResponse.success(TakingPillMessageConstants.TAKING_PILL_UPDATE_SUCCESS, takingPillDetail));
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
    public ResponseEntity<ApiResponse<TakingPillSummaryResponse>> getTakingPill(
            @AuthenticationPrincipal Account account,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        try {
            TakingPillSummaryResponse takingPillSummary = takingPillService.getTakingPillSummary(user);
            return ResponseEntity.status(200)
                .body(ApiResponse.success(TakingPillMessageConstants.TAKING_PILL_RETRIEVE_SUCCESS, takingPillSummary));
        } catch (Exception e) {
            logger.error("Taking pill retrieve failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(TakingPillMessageConstants.TAKING_PILL_RETRIEVE_FAILED, null));
        }
    }

    // 복용 중인 약 상세 조회
    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<TakingPillDetailResponse>> getTakingPillDetail(
            @AuthenticationPrincipal Account account,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        try {
            TakingPillDetailResponse takingPillDetail = takingPillService.getTakingPillDetail(user);
            return ResponseEntity.status(200)
                .body(ApiResponse.success(TakingPillMessageConstants.TAKING_PILL_DETAIL_RETRIEVE_SUCCESS, takingPillDetail));
        } catch (Exception e) {
            logger.error("Taking pill detail retrieve failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(TakingPillMessageConstants.TAKING_PILL_DETAIL_RETRIEVE_FAILED, null));
        }
    }

    // 특정 약의 상세 정보 조회
    @GetMapping("/{medicationId}")
    public ResponseEntity<ApiResponse<TakingPillDetailResponse.TakingPillDetail>> getTakingPillDetailById(
            @AuthenticationPrincipal Account account,
            @PathVariable String medicationId,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        try {
            TakingPillDetailResponse.TakingPillDetail pillDetail = takingPillService.getTakingPillDetailById(user, Long.parseLong(medicationId));
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