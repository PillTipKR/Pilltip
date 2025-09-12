// author : mireutale
// description : 사용자 민감정보 서비스
package com.oauth2.User.UserInfo.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.UserInfo.Dto.UserSensitiveInfoDto;
import com.oauth2.User.UserInfo.Dto.UserSensitiveInfoDeleteRequest;
import com.oauth2.User.UserInfo.Entity.UserSensitiveInfo;
import com.oauth2.User.UserInfo.Repository.UserSensitiveInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserSensitiveInfoService {

    private final UserSensitiveInfoRepository userSensitiveInfoRepository;

    /**
     * 사용자 민감정보 저장 또는 업데이트
     */
    @Transactional
    public UserSensitiveInfoDto saveOrUpdateSensitiveInfo(User user,
                                                         List<String> allergyInfo,
                                                         List<String> chronicDiseaseInfo,
                                                         List<String> surgeryHistoryInfo) throws JsonProcessingException {
        UserSensitiveInfo sensitiveInfo = userSensitiveInfoRepository.findByUser(user)
                .orElse(new UserSensitiveInfo());

        sensitiveInfo.setUser(user);
        sensitiveInfo.setAllergyInfo(convertListToCommaSeparated(allergyInfo));
        sensitiveInfo.setChronicDiseaseInfo(convertListToCommaSeparated(chronicDiseaseInfo));
        sensitiveInfo.setSurgeryHistoryInfo(convertListToCommaSeparated(surgeryHistoryInfo));

        UserSensitiveInfo saved = userSensitiveInfoRepository.save(sensitiveInfo);
        return UserSensitiveInfoDto.from(saved);
    }

    /**
     * 사용자 민감정보 조회
     */
    public UserSensitiveInfoDto getSensitiveInfo(User user) {
        UserSensitiveInfo sensitiveInfo = userSensitiveInfoRepository.findByUser(user)
                .orElse(null);
        
        if (sensitiveInfo == null) {
            return null;
        }
        
        // EncryptionConverter가 자동으로 복호화하므로 DTO 변환 사용
        return UserSensitiveInfoDto.from(sensitiveInfo);
    }

    /**
     * 사용자 민감정보 존재 여부 확인
     */
    public boolean existsByUser(User user) {
        return userSensitiveInfoRepository.existsByUser(user);
    }

    /**
     * 사용자 민감정보 전체 삭제
     */
    @Transactional
    public void deleteAllSensitiveInfo(User user) {
        userSensitiveInfoRepository.deleteByUser(user);
        
    }

    /**
     * 특정 카테고리의 민감정보만 업데이트
     */
    @Transactional
    public UserSensitiveInfoDto updateSensitiveInfoCategory(User user, String category, List<String> data) throws JsonProcessingException {
        UserSensitiveInfo sensitiveInfo = userSensitiveInfoRepository.findByUser(user)
                .orElse(new UserSensitiveInfo());

        sensitiveInfo.setUser(user);

        switch (category.toLowerCase()) {
            case "allergy":
                sensitiveInfo.setAllergyInfo(convertListToCommaSeparated(data));
                break;
            case "chronicdisease":
                sensitiveInfo.setChronicDiseaseInfo(convertListToCommaSeparated(data));
                break;
            case "surgeryhistory":
                sensitiveInfo.setSurgeryHistoryInfo(convertListToCommaSeparated(data));
                break;
            default:
                throw new IllegalArgumentException("Invalid category: " + category);
        }

        UserSensitiveInfo saved = userSensitiveInfoRepository.save(sensitiveInfo);
        return UserSensitiveInfoDto.from(saved);
    }

    
    /**
     * 기존 정보와 새로운 정보를 병합하고 중복을 제거합니다.
     * @param existingInfo 기존 정보 (쉼표로 구분된 문자열)
     * @param newInfo 새로운 정보 (쉼표로 구분된 문자열)
     * @return 병합되고 중복이 제거된 정보 (쉼표로 구분된 문자열)
     */
    private String mergeAndDeduplicate(String existingInfo, String newInfo) {
        // LinkedHashSet을 사용하여 순서를 유지하면서 중복을 제거
        LinkedHashSet<String> combinedSet = new LinkedHashSet<>();
        
        // 기존 정보를 세트에 추가
        if (existingInfo != null && !existingInfo.isEmpty()) {
            Stream.of(existingInfo.split(","))
                  .map(String::trim)
                  .filter(s -> !s.isEmpty())
                  .forEach(combinedSet::add);
        }
        
        // 새로운 정보를 세트에 추가
        if (newInfo != null && !newInfo.isEmpty()) {
            Stream.of(newInfo.split(","))
                  .map(String::trim)
                  .filter(s -> !s.isEmpty())
                  .forEach(combinedSet::add);
        }
        
        return String.join(",", combinedSet);
    }

    /**
     * 사용자 민감정보 선택적 삭제 (boolean으로 지정된 카테고리만 유지)
     */
    @Transactional
    public UserSensitiveInfoDto deleteSensitiveInfoCategories(User user, UserSensitiveInfoDeleteRequest request) throws JsonProcessingException {
        UserSensitiveInfo sensitiveInfo = userSensitiveInfoRepository.findByUser(user)
                .orElse(null);

        if (sensitiveInfo == null) {
            return null;
        }

        if (!request.isKeepAllergyInfo()) {
            sensitiveInfo.setAllergyInfo("");
            saveOrUpdateSensitiveInfo(
                user,
                null,
                convertCommaSeparatedToList(sensitiveInfo.getChronicDiseaseInfo()),
                convertCommaSeparatedToList(sensitiveInfo.getSurgeryHistoryInfo())
            );
        }
        if (!request.isKeepChronicDiseaseInfo()) {
            sensitiveInfo.setChronicDiseaseInfo("");
            saveOrUpdateSensitiveInfo(
                user,
                convertCommaSeparatedToList(sensitiveInfo.getAllergyInfo()),
                null,
                convertCommaSeparatedToList(sensitiveInfo.getSurgeryHistoryInfo())
            );
        }
        if (!request.isKeepSurgeryHistoryInfo()) {
            sensitiveInfo.setSurgeryHistoryInfo("");
            saveOrUpdateSensitiveInfo(
                user,
                convertCommaSeparatedToList(sensitiveInfo.getAllergyInfo()),
                convertCommaSeparatedToList(sensitiveInfo.getChronicDiseaseInfo()),
                null
            );
        }

        UserSensitiveInfo saved = userSensitiveInfoRepository.save(sensitiveInfo);
        return UserSensitiveInfoDto.from(saved);
    }

    /**
     * 사용자 민감정보 전체 삭제
     */
    @Transactional
    public void deleteAllSensitiveInfoByUser(User user) {
        userSensitiveInfoRepository.deleteByUser(user);
    }

    private String convertListToCommaSeparated(List<String> data) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        
        return data.stream()
                .filter(item -> item != null && !item.trim().isEmpty())
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }

    // String(콤마 구분) -> List<String> 변환 헬퍼 메서드 추가
    private List<String> convertCommaSeparatedToList(String data) {
        if (data == null || data.trim().isEmpty()) {
            return List.of();
        }
        return Stream.of(data.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
} 