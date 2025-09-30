package com.oauth2.HealthSupplement.SupplementInfo.Repository;

import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplementMat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HealthSupplementMatRepository extends JpaRepository<HealthSupplementMat, Long> {

    List<HealthSupplementMat> findByMaterialName(String materialName);
}
