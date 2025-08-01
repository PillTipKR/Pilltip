package com.oauth2.HealthSupplement.Review.Entity;

import com.oauth2.Drug.Review.Domain.Review;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "supplement_review_images")
public class SupplementReviewImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private SupplementReview review;

    private String imageUrl;

    private int sortOrder;  // 사진 정렬 순서 (0부터 시작)

    // createdAt, updatedAt 추가해도 좋음

    // Getter / Setter / 생성자 등 생략
}

