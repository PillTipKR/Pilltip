package com.oauth2.Drug.DrugInfo.Repository;

import com.oauth2.Drug.DrugInfo.Domain.DrugEffect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DrugEffectRepository extends JpaRepository<DrugEffect, Long> {
    List<DrugEffect> findByDrugId(long id);

    @Query("""
        select e.content
        from DrugEffect e
        where e.drug.id = :drugId and e.type = :type
        order by e.id asc
    """)
    List<String> findContentsByDrugIdAndType(
            @Param("drugId") Long drugId,
            @Param("type") DrugEffect.Type type
    );
} 