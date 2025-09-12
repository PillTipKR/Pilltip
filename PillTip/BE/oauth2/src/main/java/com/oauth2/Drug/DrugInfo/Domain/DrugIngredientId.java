package com.oauth2.Drug.DrugInfo.Domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class DrugIngredientId implements java.io.Serializable {
    @Column(nullable = false)
    private Long drugId;

    @Column(nullable = false)
    private Long ingredientId;

    // equals, hashCode 생략
}
