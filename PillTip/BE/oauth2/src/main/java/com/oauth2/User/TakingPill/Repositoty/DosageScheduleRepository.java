package com.oauth2.User.TakingPill.Repositoty;

import com.oauth2.User.TakingPill.Entity.DosageSchedule;
import com.oauth2.User.TakingPill.Entity.TakingPill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DosageScheduleRepository extends JpaRepository<DosageSchedule, Long> {
    List<DosageSchedule> findByTakingPill(TakingPill takingPill);
    
    // TakingPill별 복용 스케줄 삭제
    @Modifying
    @Query("DELETE FROM DosageSchedule ds WHERE ds.takingPill = :takingPill")
    void deleteAllByTakingPill(@Param("takingPill") TakingPill takingPill);
} 