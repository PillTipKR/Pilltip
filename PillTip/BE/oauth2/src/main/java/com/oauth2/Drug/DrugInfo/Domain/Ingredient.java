package com.oauth2.Drug.DrugInfo.Domain;

import com.oauth2.Drug.DUR.Domain.DurEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ingredients")
public class Ingredient implements DurEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String nameKr;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String nameEn;
    // getter, setter 생략

    // getter, setter 생략
    @Override
    public Long getId(){
        return id;
    }

    @Override
    public String getName(){
        return nameKr;
    }
} 