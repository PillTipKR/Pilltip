// author : mireutale
// description : 유저 프로필 저장소
package com.oauth2.User.UserInfo.Repository;

import com.oauth2.User.UserInfo.Entity.UserProfile;
import com.oauth2.User.UserInfo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    // SELECT * FROM user_profile WHERE phone = ?
    Optional<UserProfile> findByPhone(String phone);
    
    // SELECT * FROM user_profile WHERE user_id = ?
    Optional<UserProfile> findByUserId(Long userId);
    
    // 사용자별 프로필 삭제
    @Modifying
    @Query("DELETE FROM UserProfile up WHERE up.user = :user")
    void deleteByUser(@Param("user") User user);
}
