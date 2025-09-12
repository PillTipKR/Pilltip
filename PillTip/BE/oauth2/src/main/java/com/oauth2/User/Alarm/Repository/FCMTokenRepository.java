package com.oauth2.User.Alarm.Repository;

import com.oauth2.Account.Entity.Account;
import com.oauth2.User.Alarm.Domain.FCMToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {
    
    // 사용자별 FCM 토큰 삭제
    @Modifying
    @Query("DELETE FROM FCMToken f WHERE f.account = :account")
    void deleteByAccount(@Param("account") Account account);
}
