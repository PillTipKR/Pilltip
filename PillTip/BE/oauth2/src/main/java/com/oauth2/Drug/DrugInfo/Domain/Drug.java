package com.oauth2.Drug.DrugInfo.Domain;

import com.oauth2.Drug.Review.Domain.Review;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "drugs")
public class Drug {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String name;
    private String code;

    @Column(nullable = false)
    private String manufacturer;
    private Date approvalDate;

    @Column(columnDefinition = "TEXT",nullable=false)
    private String packaging;

    @Column(columnDefinition = "TEXT")
    private String form;

    private String atcCode;
    private String validTerm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tag tag;

    public enum Tag {
        EXPERT, COMMON
    }

    private String image;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "drug", cascade = CascadeType.ALL)
    private Set<DrugEffect> drugEffects;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "drug", cascade = CascadeType.ALL)
    private Set<DrugStorageCondition> storageConditions;

    @OneToMany(mappedBy = "drug", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    // getter, setter 생략
}
