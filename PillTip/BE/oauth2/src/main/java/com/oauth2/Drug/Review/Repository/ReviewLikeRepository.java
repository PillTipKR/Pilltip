package com.oauth2.Drug.Review.Repository;

import com.oauth2.Drug.Review.Domain.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    // 특정 리뷰에 대해 유저가 좋아요를 눌렀는지 확인
    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);

    // 좋아요 삭제
    void deleteByUserIdAndReviewId(Long userId, Long reviewId);

    // 특정 리뷰의 좋아요 수
    Long countByReviewId(Long reviewId);

    // 특정 유저가 좋아요 누른 리뷰들
    List<ReviewLike> findByUserId(Long userId);

    @Query("""
    SELECT rl.review.id FROM ReviewLike rl
    WHERE rl.user.id = :userId AND rl.review.id IN :reviewIds
    """)
    List<Long> findLikedReviewIds(@Param("userId") Long userId, @Param("reviewIds") List<Long> reviewIds);

}

