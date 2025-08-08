package com.oauth2.HealthSupplement.SupplementInfo.Entity;

import com.oauth2.HealthSupplement.IntakeRequire.Entity.IntakeRequire;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "healthIngredient", cascade = CascadeType.ALL)
    private List<IntakeRequire> intakeRequireList;
}

