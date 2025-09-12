package com.oauth2.Drug.DUR.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "drug_therapeutic_dup")
public class DrugTherapeuticDup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String category; // 효능군명

    @Column(nullable = false, columnDefinition = "TEXT")
    private String className; // 분류명

    @Column(nullable = false)
    private Long drugId; // 약품 ID (약 이름으로 매핑)

    @Column(nullable = false, columnDefinition = "TEXT")
    private String note; // 중복내용

    @Column(columnDefinition = "TEXT")
    private String remark; // 비고
}
