package com.oauth2.HealthSupplement.Review.Service;

import com.oauth2.HealthSupplement.Review.Entity.SupplementReview;
import com.oauth2.HealthSupplement.Review.Entity.SupplementReviewLike;
import com.oauth2.HealthSupplement.Review.Repository.SupplementReviewLikeRepository;
import com.oauth2.HealthSupplement.Review.Repository.SupplementReviewRepository;
import com.oauth2.User.UserInfo.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplementReviewLikeService {

    private final SupplementReviewLikeRepository supplementReviewLikeRepository;
    private final SupplementReviewRepository supplementReviewRepository;

    public boolean toggleLike(User user, Long reviewId) {
        Optional<SupplementReviewLike> existing = supplementReviewLikeRepository
                .findAll()
                .stream()
                .filter(like -> like.getUser().getId().equals(user.getId()) && like.getReview().getId().equals(reviewId))
                .findFirst();

        if (existing.isPresent()) {
            supplementReviewLikeRepository.delete(existing.get());
            updateLikeCount(reviewId);
            return false; // 좋아요 취소
        } else {
            SupplementReview review = supplementReviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("리뷰 없음"));

            SupplementReviewLike like = new SupplementReviewLike();
            like.setUser(user);  // ID만 설정
            like.setReview(review);
            supplementReviewLikeRepository.save(like);

            updateLikeCount(reviewId);
            return true; // 좋아요 추가
        }
    }

    private void updateLikeCount(Long reviewId) {
        long count = supplementReviewLikeRepository.countByReviewId(reviewId);
        SupplementReview review = supplementReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰 없음"));
        review.setLikeCount((int) count);
    }
}

