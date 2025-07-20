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

    private String enterprise;         // 업체명
    private String productName;        // 제품명
    private String statementNo;        // 신고번호
    private String registerDate;       // 등록일자
    private String distributionPeriod; // 유통기한

    @Column(columnDefinition = "TEXT")
    private String appearance;         // 성상

    @Column(columnDefinition = "TEXT")
    private String servingMethod;      // 섭취방법

    @Column(columnDefinition = "TEXT")
    private String preservation;       // 보존방법

    @Column(columnDefinition = "TEXT")
    private String intakeCaution;      // 섭취시 주의사항

    @Column(columnDefinition = "TEXT")
    private String mainFunction;       // 주요 기능성
}
