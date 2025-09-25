package com.oauth2.HealthSupplement.SupplementInfo.Entity;

import com.oauth2.Drug.DUR.Domain.DurEntity;
import com.oauth2.Drug.DrugInfo.Domain.DrugEffect;
import com.oauth2.Drug.DrugInfo.Domain.DrugStorageCondition;
import com.oauth2.Drug.Review.Domain.Review;
import com.oauth2.HealthSupplement.Review.Entity.SupplementReview;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "health_supplement")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthSupplement implements DurEntity {

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
    private String indivMaterial;  // 원료 추출물 이름 저장

    @Column(columnDefinition = "TEXT")
    private String rawMaterial;  // 추출물 이름 저장

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "supplement", cascade = CascadeType.ALL)
    private Set<HealthSupplementEffect> supplementEffects;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "supplement", cascade = CascadeType.ALL)
    private Set<HealthSupplementStorageCondition> storageConditions;

    @OneToMany(mappedBy = "supplement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplementReview> reviews = new ArrayList<>();

    // getter, setter 생략
    @Override
    public Long getId(){
        return id;
    }

    @Override
    public String getName(){
        return productName;
    }
}
