package com.oauth2.HealthSupplement.Review.Repository;

import com.oauth2.Drug.Review.Domain.Review;
import com.oauth2.HealthSupplement.Review.Entity.SupplementReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SupplementReviewRepository extends JpaRepository<SupplementReview, Long> {
    SupplementReview findById(long id);

    // 특정 약의 리뷰 목록
    Page<SupplementReview> findBySupplementId(Long supplementId, Pageable pageable);
    // 특정 유저의 리뷰 목록
    List<SupplementReview> findByUserId(Long userId);

    @Query("""
    SELECT DISTINCT r FROM SupplementReview r
    LEFT JOIN FETCH r.reviewTags rt
    LEFT JOIN FETCH rt.tag
    WHERE r.supplement.id = :supplementId
    """)
    List<SupplementReview> findBySupplementIdWithReviewTags(Long supplementId);
}
