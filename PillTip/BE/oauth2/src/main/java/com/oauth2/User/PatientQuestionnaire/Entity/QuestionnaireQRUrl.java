// author : mireutale
// description : 문진표 QR URL 엔티티
package com.oauth2.User.PatientQuestionnaire.Entity;

import com.oauth2.User.UserInfo.Entity.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "questionnaire_qr_url")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireQRUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "qr_url", nullable = false, columnDefinition = "TEXT")
    private String qrUrl;

    @Column(name = "hospital_code", nullable = false)
    private String hospitalCode;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate = LocalDateTime.now();
} 