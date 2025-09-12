// author : mireutale
// description : 유저 동의 저장소
package com.oauth2.User.UserInfo.Repository;

import com.oauth2.User.UserInfo.Entity.UserPermissions;

import org.springframework.data.jpa.repository.JpaRepository;
import com.oauth2.User.UserInfo.Entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPermissionsRepository extends JpaRepository<UserPermissions, Long> {
    // SELECT * FROM user_permissions WHERE user_id = ?
    Optional<UserPermissions> findByUser(User user);
    
    // SELECT * FROM user_permissions WHERE user_id = ?
    Optional<UserPermissions> findByUserId(Long userId);
    
    // 사용자별 권한 삭제
    @Modifying
    @Query("DELETE FROM UserPermissions up WHERE up.user = :user")
    void deleteByUser(@Param("user") User user);
} 