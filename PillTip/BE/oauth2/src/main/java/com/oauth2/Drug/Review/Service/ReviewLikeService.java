package com.oauth2.Drug.Review.Service;

import com.oauth2.Drug.Review.Domain.Review;
import com.oauth2.Drug.Review.Domain.ReviewLike;
import com.oauth2.Drug.Review.Repository.ReviewLikeRepository;
import com.oauth2.Drug.Review.Repository.ReviewRepository;
import com.oauth2.User.UserInfo.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewLikeService {

    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewRepository reviewRepository;

    public boolean toggleLike(User user, Long reviewId) {
        Optional<ReviewLike> existing = reviewLikeRepository
                .findAll()
                .stream()
                .filter(like -> like.getUser().getId().equals(user.getId()) && like.getReview().getId().equals(reviewId))
                .findFirst();

        if (existing.isPresent()) {
            reviewLikeRepository.delete(existing.get());
            updateLikeCount(reviewId);
            return false; // 좋아요 취소
        } else {
            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("리뷰 없음"));

            ReviewLike like = new ReviewLike();
            like.setUser(user);  // ID만 설정
            like.setReview(review);
            reviewLikeRepository.save(like);

            updateLikeCount(reviewId);
            return true; // 좋아요 추가
        }
    }

    private void updateLikeCount(Long reviewId) {
        long count = reviewLikeRepository.countByReviewId(reviewId);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰 없음"));
        review.setLikeCount((int) count);
    }
}

