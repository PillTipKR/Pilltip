package com.oauth2.HealthSupplement.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "supplement_ingredient")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplementIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "supplement_id")
    private HealthSupplement supplement;

    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private HealthIngredient ingredient;

    private Double amount;            // 표시량 (기준량)

    private Double minRatio = 0.0;          // 최소 허용 비율 (예: 0.8)

    private Double maxRatio = 0.0;          // 최대 허용 비율 (예: 1.5)
}
