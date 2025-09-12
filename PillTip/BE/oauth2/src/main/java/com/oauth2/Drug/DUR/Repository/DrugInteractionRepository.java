package com.oauth2.Drug.DUR.Repository;

import com.oauth2.Drug.DUR.Domain.DrugInteraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DrugInteractionRepository extends JpaRepository<DrugInteraction, Long> {

    List<DrugInteraction> findByDrugId1AndDrugId2(Long drugId1, Long drugId2);
} 