package com.oauth2.HealthSupplement.SupplementInfo.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "health_supplement_material")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthSupplementMat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    String materialName;
}
