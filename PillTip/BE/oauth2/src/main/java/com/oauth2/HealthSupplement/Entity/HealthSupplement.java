package com.oauth2.HealthSupplement.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "health_supplement")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthSupplement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String enterprise;         // 업체명

    @Column(columnDefinition = "TEXT")
    private String productName;        // 제품명

    @Column(columnDefinition = "TEXT")
    private String registerDate;       // 등록일자

    @Column(columnDefinition = "TEXT")
    private String validTerm;          // 유효기간

    private String form;         // 성상

    @Column(columnDefinition = "TEXT")
    private String dispos;         // 성상의 특징

    @Column(columnDefinition = "TEXT")
    private String rawMaterial;  // 추출물 이름 저장
}
