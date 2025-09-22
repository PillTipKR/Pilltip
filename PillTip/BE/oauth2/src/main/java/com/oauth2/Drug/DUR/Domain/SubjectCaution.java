package com.oauth2.Drug.DUR.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "subject_cautions")
public class SubjectCaution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cautionId;

    @Column(nullable = false)
    private Long subjectId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DurType durtype;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionType conditionType;

    @Column(columnDefinition = "TEXT")
    private String conditionValue; //조건(금기/주의 내용)

    @Column(columnDefinition = "TEXT")
    private String note; //비고

}
