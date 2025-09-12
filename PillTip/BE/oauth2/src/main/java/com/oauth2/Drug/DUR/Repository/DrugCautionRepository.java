package com.oauth2.Drug.DUR.Repository;

import com.oauth2.Drug.DUR.Domain.DrugCaution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DrugCautionRepository extends JpaRepository<DrugCaution, Long> {
    List<DrugCaution> findByDrugIdAndConditionType(Long drugId, DrugCaution.ConditionType conditionType);
}