package com.oauth2.Account.Repository;

import com.oauth2.Account.Entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    // SELECT * FROM accounts WHERE social_id = ?
    Optional<Account> findBySocialId(String socialId);
    // SELECT * FROM accounts WHERE login_id = ?
    Optional<Account> findByLoginId(String loginId);

    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.users WHERE a.id = :id")
    Optional<Account> findByIdWithUsers(@Param("id") Long id);


}
