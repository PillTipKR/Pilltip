package com.oauth2.Drug.DrugInfo.Repository;

import com.oauth2.Drug.DrugInfo.Domain.DrugTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrugTagRepository extends JpaRepository<DrugTag, Long> {
}
