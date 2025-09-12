package com.oauth2.User.TakingSupplement.Repository;

import com.oauth2.User.TakingPill.Entity.DosageSchedule;
import com.oauth2.User.TakingPill.Entity.TakingPill;
import com.oauth2.User.TakingSupplement.Entity.SupplementSchedule;
import com.oauth2.User.TakingSupplement.Entity.TakingSupplement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplementScheduleRepository extends JpaRepository<SupplementSchedule, Long> {
    List<SupplementSchedule> findByTakingSupplement(TakingSupplement takingSupplement);
    
    // TakingPill별 복용 스케줄 삭제
    @Modifying
    @Query("DELETE FROM SupplementSchedule ds WHERE ds.takingSupplement = :takingSupplement")
    void deleteAllByTakingSupplement(@Param("takingSupplement") TakingSupplement takingSupplement);
} 