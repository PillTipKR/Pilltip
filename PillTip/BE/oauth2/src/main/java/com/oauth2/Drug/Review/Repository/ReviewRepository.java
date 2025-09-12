package com.oauth2.Drug.Review.Repository;

import com.oauth2.Drug.Review.Domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Review findById(long id);

    // 특정 약의 리뷰 목록
    Page<Review> findByDrugId(Long drugId, Pageable pageable);
    // 특정 유저의 리뷰 목록
    List<Review> findByUserId(Long userId);

    @Query("""
    SELECT DISTINCT r FROM Review r
    LEFT JOIN FETCH r.reviewTags rt
    LEFT JOIN FETCH rt.tag
    WHERE r.drug.id = :drugId
    """)
    List<Review> findByDrugIdWithReviewTags(Long drugId);

}
