package com.oauth2.Drug.DrugInfo.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "drug_storage_conditions")
public class DrugStorageCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_Id")
    private Drug drug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private String value = "";

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean active;

    public enum Category {
        TEMPERATURE, CONTAINER, HUMID, LIGHT
    }
    // getter, setter 생략
}
