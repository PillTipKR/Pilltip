package com.oauth2.Drug.DrugInfo.Repository;

import com.oauth2.Drug.DrugInfo.Domain.Drug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DrugRepository extends JpaRepository<Drug, Long> {
    Optional<Drug> findByName(String name);
    List<Drug> findByIdIn(List<Long> ids);
    List<Drug> findByNameContaining(String name);

    @Query("SELECT DISTINCT d FROM Drug d " +
            "LEFT JOIN FETCH d.drugEffects " +
            "LEFT JOIN FETCH d.storageConditions " +
            "WHERE d.id = :id")
    Optional<Drug> findDrugWithAllRelations(@Param("id") long id);


}
