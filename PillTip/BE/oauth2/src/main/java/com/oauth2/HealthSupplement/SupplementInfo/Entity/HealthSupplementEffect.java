package com.oauth2.HealthSupplement.SupplementInfo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "health_supplement_effects")
public class HealthSupplementEffect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplement_Id")
    private HealthSupplement supplement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(columnDefinition = "LONGTEXT",nullable = false)
    private String content;

    public enum Type {
        EFFECT, USAGE, CAUTION
    }

}

