package com.oauth2.Drug.Review.Controller;

import com.oauth2.Drug.Review.Dto.*;
import com.oauth2.Drug.Review.Service.ReviewLikeService;
import com.oauth2.Drug.Review.Service.ReviewService;
import com.oauth2.Account.Service.AccountService;
import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Dto.ApiResponse;
import com.oauth2.User.UserInfo.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewLikeService reviewLikeService;
    private final AccountService accountService;

    /**
     * 리뷰 작성
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Long>> createReview(
            @RequestPart("review") ReviewCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal Account account,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        Long reviewId = reviewService.createReview(user, request, images);
        return ResponseEntity.ok(ApiResponse.success("리뷰가 등록되었습니다", reviewId));
    }

    /**
     * 리뷰 삭제
     */
    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<ApiResponse<String>> deleteReview(
            @AuthenticationPrincipal Account account,
            @PathVariable Long reviewId,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        reviewService.deleteReview(user.getId(), reviewId);
        return ResponseEntity.ok(ApiResponse.success("리뷰가 삭제되었습니다", null));
    }

    /**
     * 특정 유저의 리뷰 조회
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<UserReviewResponse>>> getUserReviews(
            @AuthenticationPrincipal Account account,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        List<UserReviewResponse> response = reviewService.getReviewsByUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success("조회 성공", response));
    }

    /**
     * 특정 약의 리뷰 조회
     */
    @GetMapping("/drug")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviews(
            @AuthenticationPrincipal Account account,
            @RequestParam Long drugId,
            @RequestParam(defaultValue = "latest") String sortKey,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        Sort sort = reviewService.getSort(sortKey, direction);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ReviewResponse> reviews = reviewService.getPagedReviews(drugId, user.getId(), pageable);

        return ResponseEntity.ok(ApiResponse.success("조회 성공", reviews));
    }

    @GetMapping("/drug/{drugId}/stats")
    public ResponseEntity<ApiResponse<ReviewStats>> getDrugReviews(@PathVariable Long drugId) {
        ReviewStats reviewStats = reviewService.getReviewWithRating(drugId);
        return ResponseEntity.ok(ApiResponse.success("조회 성공", reviewStats));
    }

    @PostMapping("/{reviewId}/like")
    public ResponseEntity<ApiResponse<String>> toggleLike(
            @AuthenticationPrincipal Account account,
            @PathVariable Long reviewId,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        boolean liked = reviewLikeService.toggleLike(user, reviewId);
        String message = liked ? "좋아요 추가됨" : "좋아요 취소됨";
        return ResponseEntity.ok(ApiResponse.success(message));
    }
}
