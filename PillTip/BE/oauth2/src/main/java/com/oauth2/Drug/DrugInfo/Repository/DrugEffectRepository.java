package com.oauth2.Drug.DrugInfo.Repository;

import com.oauth2.Drug.DrugInfo.Domain.DrugEffect;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DrugEffectRepository extends JpaRepository<DrugEffect, Long> {
    List<DrugEffect> findByDrugId(long id);
} 