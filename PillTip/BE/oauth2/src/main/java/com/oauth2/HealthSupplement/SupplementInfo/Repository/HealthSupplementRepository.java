package com.oauth2.HealthSupplement.SupplementInfo.Repository;

import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HealthSupplementRepository extends JpaRepository<HealthSupplement, Long> {

    @Query("""
    SELECT hs.id
    FROM HealthSupplement hs
    WHERE LOWER(hs.rawMaterial) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<Long> findHealthSupplementsByRawMaterial(@Param("name") String name);

    Optional<HealthSupplement> findHealthSupplementById(Long id);

    @Query("SELECT DISTINCT si FROM HealthSupplement si " +
            "LEFT JOIN FETCH si.supplementEffects " +
            "LEFT JOIN FETCH si.storageConditions " +
            "WHERE si.id = :id")
    Optional<HealthSupplement> findSuppelmentWithAllRelations(@Param("id") long id);

    Optional<HealthSupplement> findByProductName(String name);
}
