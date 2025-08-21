package com.oauth2.User.TakingSupplement.Entity;


import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplement;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "taking_supplement_counter")
public class TakingSupplementCounter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplement_Id")
    private HealthSupplement supplement;

    @Column
    private int count;
}
