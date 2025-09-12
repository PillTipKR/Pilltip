package com.oauth2.Drug.DrugInfo.Repository;

import com.oauth2.Drug.DrugInfo.Domain.DrugStorageCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DrugStorageConditionRepository extends JpaRepository<DrugStorageCondition, Long> {
    List<DrugStorageCondition> findByDrugId(Long drugId);
}
