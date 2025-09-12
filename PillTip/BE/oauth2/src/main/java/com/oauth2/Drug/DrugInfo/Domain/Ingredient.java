package com.oauth2.Drug.DrugInfo.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ingredients")
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String nameKr;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String nameEn;
    // getter, setter 생략
} 