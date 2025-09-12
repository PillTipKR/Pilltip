package com.oauth2.User.PatientQuestionnaire.Repository;

import com.oauth2.User.PatientQuestionnaire.Entity.QuestionnaireQRUrl;
import com.oauth2.User.UserInfo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface QuestionnaireQRUrlRepository extends JpaRepository<QuestionnaireQRUrl, Long> {
    // 병원 코드로 QR URL 조회
    List<QuestionnaireQRUrl> findByHospitalCode(String hospitalCode);
    
    // 사용자별 QR URL 조회
    Optional<QuestionnaireQRUrl> findByUser(User user);
    
    // 사용자 ID로 QR URL 조회
    Optional<QuestionnaireQRUrl> findByUserId(Long userId);
    
    @Query("SELECT q FROM QuestionnaireQRUrl q JOIN FETCH q.user u LEFT JOIN FETCH u.userProfile WHERE q.hospitalCode = :hospitalCode")
    List<QuestionnaireQRUrl> findByHospitalCodeWithUser(@Param("hospitalCode") String hospitalCode);
    // 사용자의 기존 QR URL 삭제
    @Modifying
    @Query("DELETE FROM QuestionnaireQRUrl q WHERE q.user = :user")
    void deleteByUser(@Param("user") User user);
} 