package com.oauth2.User.PatientQuestionnaire.Service;

import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.PatientQuestionnaire.Entity.QuestionnaireQRUrl;
import com.oauth2.User.PatientQuestionnaire.Entity.PatientQuestionnaire;
import com.oauth2.User.PatientQuestionnaire.Repository.QuestionnaireQRUrlRepository;
import com.oauth2.User.PatientQuestionnaire.Dto.QuestionnaireQRUrlResponse;
import com.oauth2.User.Hospital.HospitalService;
import com.oauth2.Account.Service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuestionnaireQRUrlService {
    
    private final QuestionnaireQRUrlRepository qrUrlRepository;
    private final TokenService tokenService;
    private final HospitalService hospitalService;
    private final PatientQuestionnaireService patientQuestionnaireService;
    
    /**
     * 사용자의 QR URL 생성 또는 업데이트
     */
    @Transactional
    public QuestionnaireQRUrlResponse generateOrUpdateQRUrl(User user, String hospitalCode) {
        // 1. 병원 코드 유효성 검사
        if (!hospitalService.existsByHospitalCode(hospitalCode)) {
            throw new IllegalArgumentException("유효하지 않은 병원 코드입니다.");
        }
        
        // 2. 기존 QR URL 삭제 (새로운 URL 생성 전)
        qrUrlRepository.deleteByUser(user);
        
        // 3. JWT 토큰 생성 (1시간 제한)
        String jwtToken = tokenService.createCustomJwtToken(
            user.getId(),
            hospitalCode,
            3600  // 1시간 = 3600초
        );
        
        // 4. QR URL 생성 (앱에서 접근할 수 있는 형태)
        String qrUrl = String.format("localhost:3000/questionnaire/public/%s?token=%s",
            user.getId(),
            jwtToken);
        
        String realName = user.getRealName();
        // 5. DB에 저장
        QuestionnaireQRUrl qrUrlEntity = QuestionnaireQRUrl.builder()
            .user(user)
            .qrUrl(qrUrl)
            .hospitalCode(hospitalCode)
            .createDate(LocalDateTime.now())
            .build();
        
        QuestionnaireQRUrl savedQrUrl = qrUrlRepository.save(qrUrlEntity);
        
        // 6. 응답 생성
        LocalDateTime createDateTime = savedQrUrl.getCreateDate();
        LocalDateTime expirationDateTime = createDateTime.plusHours(1);
        
        // 문진표 정보 조회
        PatientQuestionnaire questionnaire = null;
        try {
            questionnaire = patientQuestionnaireService.getCurrentUserQuestionnaire(user);
        } catch (Exception e) {
            // 문진표가 없는 경우 null로 처리
        }
        
        return QuestionnaireQRUrlResponse.builder()
            .qrUrl(savedQrUrl.getQrUrl())
            .realName(realName)
            .address(user.getAddress())
            .phoneNumber(user.getUserProfile() != null ? user.getUserProfile().getPhone() : null)
            .gender(user.getUserProfile() != null && user.getUserProfile().getGender() != null ? 
                   user.getUserProfile().getGender().name() : null)
            .birthDate(user.getUserProfile() != null && user.getUserProfile().getBirthDate() != null ? 
                     user.getUserProfile().getBirthDate().toString() : null)
            .createDate(createDateTime)
            .expirationDate(expirationDateTime.toEpochSecond(java.time.ZoneOffset.UTC) * 1000) // Unix timestamp in milliseconds
            .isMedication(questionnaire != null && questionnaire.getMedicationInfo() != null && !questionnaire.getMedicationInfo().trim().isEmpty() && !"null".equals(questionnaire.getMedicationInfo().trim()))
            .isAllergy(questionnaire != null && questionnaire.getAllergyInfo() != null && !questionnaire.getAllergyInfo().trim().isEmpty() && !"null".equals(questionnaire.getAllergyInfo().trim()))
            .isChronicDisease(questionnaire != null && questionnaire.getChronicDiseaseInfo() != null && !questionnaire.getChronicDiseaseInfo().trim().isEmpty() && !"null".equals(questionnaire.getChronicDiseaseInfo().trim()))
            .isSurgeryHistory(questionnaire != null && questionnaire.getSurgeryHistoryInfo() != null && !questionnaire.getSurgeryHistoryInfo().trim().isEmpty() && !"null".equals(questionnaire.getSurgeryHistoryInfo().trim()))
            .hospitalName(hospitalService.findByHospitalCode(hospitalCode).getName())
            .build();
    }
    
    /**
     * 사용자의 QR URL 조회
     */
    public QuestionnaireQRUrlResponse getQRUrl(User user, String hospitalCode) {
        QuestionnaireQRUrl qrUrl = qrUrlRepository.findByUser(user)
            .orElseThrow(() -> new IllegalArgumentException("QR URL이 존재하지 않습니다."));
        
        String realName = user.getRealName();
        LocalDateTime createDateTime = qrUrl.getCreateDate() != null ? qrUrl.getCreateDate() : LocalDateTime.now();
        LocalDateTime expirationDateTime = createDateTime.plusHours(1);
        
        // 문진표 정보 조회
        PatientQuestionnaire questionnaire = null;
        try {
            questionnaire = patientQuestionnaireService.getCurrentUserQuestionnaire(user);
        } catch (Exception e) {
            // 문진표가 없는 경우 null로 처리
        }
        
        return QuestionnaireQRUrlResponse.builder()
            .qrUrl(qrUrl.getQrUrl())
            .realName(realName)
            .address(user.getAddress())
            .phoneNumber(user.getUserProfile() != null ? user.getUserProfile().getPhone() : null)
            .gender(user.getUserProfile() != null && user.getUserProfile().getGender() != null ? 
                   user.getUserProfile().getGender().name() : null)
            .birthDate(user.getUserProfile() != null && user.getUserProfile().getBirthDate() != null ? 
                     user.getUserProfile().getBirthDate().toString() : null)
            .createDate(createDateTime)
            .expirationDate(expirationDateTime.toEpochSecond(java.time.ZoneOffset.UTC) * 1000) // Unix timestamp in milliseconds
            .isMedication(questionnaire != null && questionnaire.getMedicationInfo() != null && !questionnaire.getMedicationInfo().trim().isEmpty() && !"null".equals(questionnaire.getMedicationInfo().trim()))
            .isAllergy(questionnaire != null && questionnaire.getAllergyInfo() != null && !questionnaire.getAllergyInfo().trim().isEmpty() && !"null".equals(questionnaire.getAllergyInfo().trim()))
            .isChronicDisease(questionnaire != null && questionnaire.getChronicDiseaseInfo() != null && !questionnaire.getChronicDiseaseInfo().trim().isEmpty() && !"null".equals(questionnaire.getChronicDiseaseInfo().trim()))
            .isSurgeryHistory(questionnaire != null && questionnaire.getSurgeryHistoryInfo() != null && !questionnaire.getSurgeryHistoryInfo().trim().isEmpty() && !"null".equals(questionnaire.getSurgeryHistoryInfo().trim()))
            .hospitalName(hospitalService.findByHospitalCode(hospitalCode).getName())
            .build();
    }

    public List<QuestionnaireQRUrlResponse> getAllQRUrl(String hospitalCode) {
        List<QuestionnaireQRUrl> qrUrl = qrUrlRepository.findByHospitalCodeWithUser(hospitalCode);
        if (qrUrl.isEmpty()) {
            return new ArrayList<>();
        }
        return qrUrl.stream()
            .map(qr -> {
                User user = qr.getUser();
                PatientQuestionnaire questionnaire = null;
                try {
                    questionnaire = patientQuestionnaireService.getCurrentUserQuestionnaire(user);
                } catch (Exception e) {
                    // 문진표가 없는 경우 null로 처리
                }
                
                // createDate와 expirationDate 설정
                LocalDateTime createDateTime = qr.getCreateDate() != null ? qr.getCreateDate() : LocalDateTime.now();
                LocalDateTime expirationDateTime = createDateTime.plusHours(1);
                
                QuestionnaireQRUrlResponse response = QuestionnaireQRUrlResponse.builder()
                    .qrUrl(qr.getQrUrl())
                    .realName(user.getRealName())
                    .address(user.getAddress())
                    .phoneNumber(user.getUserProfile() != null ? user.getUserProfile().getPhone() : null)
                    .gender(user.getUserProfile() != null && user.getUserProfile().getGender() != null ? 
                           user.getUserProfile().getGender().name() : null)
                    .birthDate(user.getUserProfile() != null && user.getUserProfile().getBirthDate() != null ? 
                             user.getUserProfile().getBirthDate().toString() : null)
                    .createDate(createDateTime)
                    .expirationDate(expirationDateTime.toEpochSecond(java.time.ZoneOffset.UTC) * 1000) // Unix timestamp in milliseconds
                    .isMedication(questionnaire != null && questionnaire.getMedicationInfo() != null && !questionnaire.getMedicationInfo().trim().isEmpty() && !"null".equals(questionnaire.getMedicationInfo().trim()))
                    .isAllergy(questionnaire != null && questionnaire.getAllergyInfo() != null && !questionnaire.getAllergyInfo().trim().isEmpty() && !"null".equals(questionnaire.getAllergyInfo().trim()))
                    .isChronicDisease(questionnaire != null && questionnaire.getChronicDiseaseInfo() != null && !questionnaire.getChronicDiseaseInfo().trim().isEmpty() && !"null".equals(questionnaire.getChronicDiseaseInfo().trim()))
                    .isSurgeryHistory(questionnaire != null && questionnaire.getSurgeryHistoryInfo() != null && !questionnaire.getSurgeryHistoryInfo().trim().isEmpty() && !"null".equals(questionnaire.getSurgeryHistoryInfo().trim()))
                    .hospitalName(hospitalService.findByHospitalCode(hospitalCode).getName())
                    .build();
                return response;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 사용자의 QR URL 삭제
     */
    @Transactional
    public void deleteQRUrl(User user) {
        qrUrlRepository.deleteByUser(user);
    }
} 