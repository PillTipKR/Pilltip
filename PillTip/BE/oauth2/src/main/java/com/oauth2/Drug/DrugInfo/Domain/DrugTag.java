package com.oauth2.Drug.DrugInfo.Domain;

import com.oauth2.Drug.DUR.Domain.DurEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "drug_tag")
public class DrugTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long drugId;

    @Column(columnDefinition = "TEXT")
    String tag;
}
