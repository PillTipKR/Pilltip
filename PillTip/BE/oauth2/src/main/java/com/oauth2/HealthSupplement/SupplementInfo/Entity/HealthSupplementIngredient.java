package com.oauth2.HealthSupplement.SupplementInfo.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "health_supplement_ingredient")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthSupplementIngredient {

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
    private String unit;              // 단위 (예: mg, ㎍, mgNE, mgα-TE 등)
}
