package com.oauth2.User.TakingPill.Repositoty;

import com.oauth2.User.TakingPill.Entity.TakingPill;
import com.oauth2.User.UserInfo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TakingPillRepository extends JpaRepository<TakingPill, Long> {
   List<TakingPill> findByUser(User user);
   List<TakingPill> findByUserAndMedicationId(User user, Long medicationId);
   Optional<TakingPill> findById(Long id);

   @Query("SELECT DISTINCT tp FROM TakingPill tp LEFT JOIN FETCH tp.dosageSchedules WHERE tp.user = :user")
   List<TakingPill> findByUserWithDosageSchedules(@Param("user") User user);
  
   @Modifying
   @Query("DELETE FROM TakingPill tp WHERE tp.endYear < :year OR " +
           "(tp.endYear = :year AND tp.endMonth < :month) OR " +
           "(tp.endYear = :year AND tp.endMonth = :month AND tp.endDay < :day)")
   void deleteExpiredPills(@Param("year") int year, @Param("month") int month, @Param("day") int day);
   
   // 사용자별 복용 중인 약 삭제
   @Modifying
   @Query("DELETE FROM TakingPill tp WHERE tp.user = :user")
   void deleteAllByUser(@Param("user") User user);
}