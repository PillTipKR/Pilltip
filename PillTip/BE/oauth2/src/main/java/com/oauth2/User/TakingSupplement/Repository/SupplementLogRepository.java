package com.oauth2.User.TakingSupplement.Repository;

import com.oauth2.User.TakingPill.Entity.DosageLog;
import com.oauth2.User.TakingPill.Entity.TakingPill;
import com.oauth2.User.TakingSupplement.Entity.SupplementLog;
import com.oauth2.User.TakingSupplement.Entity.TakingSupplement;
import com.oauth2.User.UserInfo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface SupplementLogRepository extends JpaRepository<SupplementLog, Long> {

    @Query("SELECT sl FROM SupplementLog sl " +
            "JOIN sl.user u " +
            "WHERE u.id = :userId " +
            "AND DATE(sl.scheduledTime) = :targetDate")
    List<SupplementLog> findByUserAndDate(
            @Param("userId") Long userId,
            @Param("targetDate") LocalDate targetDate
    );

    @Query("SELECT sl FROM SupplementLog sl " +
            "JOIN sl.user u " +
            "WHERE u.id = :userId " +
            "AND DATE(sl.rescheduledTime) = :targetDate")
    List<SupplementLog> findByUserAndRescheduledDate(
            @Param("userId") Long userId,
            @Param("targetDate") LocalDate targetDate
    );

    @Query("SELECT sl FROM SupplementLog sl " +
            "JOIN sl.user u " +
            "WHERE u.id = :userId " +
            "AND DATE(sl.scheduledTime) BETWEEN :startDate AND :endDate")
    List<SupplementLog> findWeeklySupplementLogs(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<SupplementLog> findByTakingSupplementAndScheduledTimeAfter(TakingSupplement takingSupplement, LocalDateTime scheduledTime);

    List<SupplementLog> findByUserAndTakingSupplement(User user, TakingSupplement takingSupplement);

    void deleteAllByUserAndSupplementNameAndScheduledTimeAfter(User user, String supplementName, LocalDateTime time);

    void deleteAllByUserAndSupplementNameAndScheduledTimeBetween(User user, String supplementName, LocalDateTime from, LocalDateTime to);

    // 사용자별 복용 로그 삭제
    @Modifying
    @Query("DELETE FROM SupplementLog sl WHERE sl.user = :user")
    void deleteAllByUser(@Param("user") User user);

    Long user(User user);
}
