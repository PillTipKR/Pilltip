package com.oauth2.HealthSupplement.IntakeRequire.Entity;

import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthIngredient;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "health_intake_require")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntakeRequire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_ingredient_Id")
    private HealthIngredient healthIngredient;

    private String ageRange;

    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private Double intakeRecommend; //권장섭취량
    private Double intakeMinimum; //평균필요량
    private Double intakeMaximum; //상한섭취량
    private Double intakeEnough; //충분섭취량

    public enum Status {
        MALE,FEMALE,
        BABY,
        PREGNANCY,LACTATION
    }
}
