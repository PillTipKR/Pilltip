package com.oauth2.User.PatientQuestionnaire.Repository;

import com.oauth2.User.PatientQuestionnaire.Entity.PatientQuestionnaire;
import com.oauth2.User.UserInfo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PatientQuestionnaireRepository extends JpaRepository<PatientQuestionnaire, Long> {
    List<PatientQuestionnaire> findByUser(User user);
    
    @Query("SELECT pq FROM PatientQuestionnaire pq JOIN FETCH pq.user WHERE pq.questionnaireId = :id")
    Optional<PatientQuestionnaire> findByIdWithUser(@Param("id") Long id);
    
    // 사용자의 최신 문진표 조회
    @Query("SELECT pq FROM PatientQuestionnaire pq WHERE pq.user = :user ORDER BY pq.issueDate DESC")
    Optional<PatientQuestionnaire> findTopByUserOrderByIssueDateDesc(@Param("user") User user);
    
    // 사용자의 문진표 삭제
    @Modifying
    @Query("DELETE FROM PatientQuestionnaire pq WHERE pq.user = :user")
    void deleteByUser(@Param("user") User user);
} 