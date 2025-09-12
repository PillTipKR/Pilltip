package com.oauth2.HealthSupplement.SupplementInfo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "health_supplement_storage_conditions")
public class HealthSupplementStorageCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplement_Id")
    private HealthSupplement supplement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private String value = "";

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean active;

    public enum Category {
        TEMPERATURE, HUMID, LIGHT,FRIDGE
    }
}
