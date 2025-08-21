package com.oauth2.HealthSupplement.DUR.Repository;

import com.oauth2.HealthSupplement.DUR.Entity.HealthSupplementCaution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthSupplementCautionRepository extends JpaRepository<HealthSupplementCaution, Long> {
    boolean existsBySupplementIdAndConditionType(Long healthSupplementId, HealthSupplementCaution.ConditionType conditionType);

}
