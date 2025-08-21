package com.oauth2.HealthSupplement.DUR.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "health_supplement_cautions")
public class HealthSupplementCaution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cautionId;

    @Column(nullable = false)
    private Long supplementId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionType conditionType;

    //PERIOD : 투여기간 주의
    //PREGNANCY : 임부금기
    //AGE: 연령금기
    //ELDER: 노인주의 - 노인(65세 이상)
    //LACTATION: 수유부주의
    public enum ConditionType {
        PREGNANCY, AGE,
        ELDER, LACTATION
    }

}
