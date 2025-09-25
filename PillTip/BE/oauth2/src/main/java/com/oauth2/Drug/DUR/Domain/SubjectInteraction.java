package com.oauth2.Drug.DUR.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "subject_interactions")
public class SubjectInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interactionId;

    //약-건기식은 무조건 약이 id1, 건기식이 id2

    private Long subjectId1;
    private Long subjectId2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DurType durtype1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DurType durtype2;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String note;
}


