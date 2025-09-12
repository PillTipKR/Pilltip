package com.oauth2.Drug.DUR.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "drug_cautions")
public class DrugCaution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cautionId;

    @Column(nullable = false)
    private Long drugId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionType conditionType;

    @Column(columnDefinition = "TEXT")
    private String conditionValue; //조건(금기/주의 내용)

    @Column(columnDefinition = "TEXT")
    private String note; //비고

    //PERIOD : 투여기간 주의
    //PREGNANCY : 임부금기
    //AGE: 연령금기
    //ELDER: 노인주의 - 노인(65세 이상)
    //LACTATION: 수유부주의
    public enum ConditionType {
        PERIOD, PREGNANCY, AGE,
        ELDER, LACTATION, OVERDOSE
    }

}
