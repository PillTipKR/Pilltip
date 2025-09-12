// author : mireutale
// description : UserToken 엔티티를 위한 리포지토리 인터페이스.
package com.oauth2.Account.Repository;

import com.oauth2.Account.Entity.AccountToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;

public interface AccountTokenRepository extends JpaRepository<AccountToken, Long> {

    @Query("SELECT at FROM AccountToken at WHERE at.refreshToken = :refreshToken")
    Optional<AccountToken> findByRefreshToken(@Param("refreshToken") String refreshToken);

    @Modifying
    @Query("UPDATE AccountToken at SET at.accessToken = :accessToken, at.accessTokenExpiry = :accessTokenExpiry WHERE at.accountId = :accountId")
    void updateAccessToken(@Param("accessToken") String accessToken, @Param("accessTokenExpiry") LocalDateTime accessTokenExpiry);
}
