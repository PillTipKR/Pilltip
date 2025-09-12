package com.oauth2.Drug.DrugInfo.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "drug_ingredients")
public class DrugIngredient {
    @EmbeddedId
    private DrugIngredientId id;

    private Float amount;
    private String amountBackup;
    private String unit;

    // getter, setter 생략
}

