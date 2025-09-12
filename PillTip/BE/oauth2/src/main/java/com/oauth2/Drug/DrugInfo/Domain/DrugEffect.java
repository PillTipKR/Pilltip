package com.oauth2.Drug.DrugInfo.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "drug_effects")
public class DrugEffect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_Id")
    private Drug drug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(columnDefinition = "LONGTEXT",nullable = false)
    private String content;

    public enum Type {
        EFFECT, USAGE, CAUTION
    }
    // getter, setter 생략
}
