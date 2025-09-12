package com.oauth2.HealthSupplement.Review.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "supplement_review_tags")
public class SupplementReviewTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private SupplementReview review;

    @ManyToOne(fetch = FetchType.LAZY)
    private SupplementTag tag;

    // Getter/Setter, 생성자 등 생략
}

