package com.oauth2.HealthSupplement.Review.Repository;

import com.oauth2.Drug.Review.Domain.ReviewImage;
import com.oauth2.HealthSupplement.Review.Entity.SupplementReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplementReviewImageRepository extends JpaRepository<SupplementReviewImage, Long> {

    List<SupplementReviewImage> findByReviewIdOrderBySortOrderAsc(Long reviewId);
}

