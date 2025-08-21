package com.oauth2.User.TakingSupplement.Repository;

import com.oauth2.User.TakingPill.Entity.TakingPill;
import com.oauth2.User.TakingSupplement.Entity.TakingSupplement;
import com.oauth2.User.UserInfo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TakingSupplementRepository extends JpaRepository<TakingSupplement, Long> {
    List<TakingSupplement> findByUser(User user);
    List<TakingSupplement> findByUserAndSupplementId(User user, Long supplementId);

    @Query("SELECT DISTINCT ts FROM TakingSupplement ts LEFT JOIN FETCH ts.supplementSchedules WHERE ts.user = :user")
    List<TakingSupplement> findByUserWithDosageSchedules(@Param("user") User user);

    @Modifying
    @Query("DELETE FROM TakingSupplement ts WHERE ts.endYear < :year OR " +
            "(ts.endYear = :year AND ts.endMonth < :month) OR " +
            "(ts.endYear = :year AND ts.endMonth = :month AND ts.endDay < :day)")
    void deleteExpiredSupplements(@Param("year") int year, @Param("month") int month, @Param("day") int day);

    // 사용자별 복용 중인 약 삭제
    @Modifying
    @Query("DELETE FROM TakingSupplement ts WHERE ts.user = :user")
    void deleteAllByUser(@Param("user") User user);
}
