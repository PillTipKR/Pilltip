// author : mireutale
// description : 유저 정보 저장소
package com.oauth2.User.UserInfo.Repository;

import com.oauth2.User.Alarm.Dto.AlarmDto;
import com.oauth2.User.UserInfo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

// DB에서 User table 접근
// JpaRepository를 상속받아 기본적인 CRUD 작업 제공 -> SQL을 사용하지 않고, 메서드를 상속받아서 DB와 상호작용
public interface UserRepository extends JpaRepository<User, Long> {
    // SELECT * FROM users WHERE uuid = ?
    Optional<User> findById(Long id);
    // SELECT * FROM users WHERE nickname = ?
    Optional<User> findByNickname(String nickname);

    // DrugAlarmScheduler에서 사용하는 쿼리
    // 복용 중인 약이 있는 유저 조회, 로그인 상태인 유저만 조회, 성능 최적화를 위해 DISTINCT 및 JOIN FETCH 사용
    @Query("""
    SELECT new com.oauth2.User.Alarm.Dto.AlarmDto(
        u.id, t.FCMToken
    )
    FROM User u
    JOIN u.account a
    JOIN a.FCMToken t
    JOIN u.takingPills tp
    JOIN tp.dosageSchedules ds
    WHERE t.loggedIn = true
    AND t.FCMToken IS NOT NULL
    AND t.FCMToken != ''
    AND u.userPermissions.phonePermission = true
    AND ds.alarmOnOff = true
    """)
    List<AlarmDto> findAllActiveUsersWithPillInfo();

    @Query("""
    SELECT new com.oauth2.User.Alarm.Dto.AlarmDto(
        u.id, t.FCMToken
    )
    FROM User u
    JOIN u.account a
    JOIN a.FCMToken t
    JOIN u.takingSupplements ts
    JOIN ts.supplementSchedules ss
    WHERE t.loggedIn = true
    AND t.FCMToken IS NOT NULL
    AND t.FCMToken != ''
    AND u.userPermissions.phonePermission = true
    AND ss.alarmOnOff = true
    """)
    List<AlarmDto> findAllActiveUsersWithSupplementInfo();

    // 현재 사용자 정보 조회 (questionnaire 포함)
    @Query("""
    SELECT u FROM User u
    LEFT JOIN FETCH u.questionnaire
    WHERE u.id = :userId
    """)
    Optional<User> findByIdWithQuestionnaire(Long userId);

    // 전화번호로 사용자 조회 (UserProfile을 통해)
    @Query("""
    SELECT u FROM User u
    JOIN FETCH u.userProfile p
    WHERE p.phone = :phone
    """)
    Optional<User> findByPhone(String phone);

    @Query("SELECT u FROM User u JOIN FETCH u.account WHERE u.id = :friendId")
    Optional<User> findByFriendId(Long friendId);
}
