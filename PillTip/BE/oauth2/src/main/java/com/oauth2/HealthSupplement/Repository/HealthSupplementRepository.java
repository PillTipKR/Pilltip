package com.oauth2.HealthSupplement.Repository;

import com.oauth2.HealthSupplement.Entity.HealthSupplement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthSupplementRepository extends JpaRepository<HealthSupplement, Integer> {
}
