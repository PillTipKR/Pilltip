package com.oauth2.HealthSupplement.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "health_ingredient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;                // 성분명 (예: 비타민C, 비타민D)
    private String unit;                // 단위 (예: mg, ㎍, mgNE, mgα-TE 등)
}

