package com.oauth2.Drug.Review.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "review_images")
public class ReviewImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Review review;

    private String imageUrl;

    private int sortOrder;  // 사진 정렬 순서 (0부터 시작)

    // createdAt, updatedAt 추가해도 좋음

    // Getter / Setter / 생성자 등 생략
}

