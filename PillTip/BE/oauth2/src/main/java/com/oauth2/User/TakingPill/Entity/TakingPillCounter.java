package com.oauth2.User.TakingPill.Entity;


import com.oauth2.Drug.DrugInfo.Domain.Drug;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "taking_pill_counter")
public class TakingPillCounter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_Id")
    private Drug drug;

    @Column
    private int count;
}
