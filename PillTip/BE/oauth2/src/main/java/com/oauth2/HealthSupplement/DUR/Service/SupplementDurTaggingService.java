package com.oauth2.HealthSupplement.DUR.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oauth2.Drug.DUR.Domain.DurType;
import com.oauth2.Drug.DUR.Dto.DurTagDto;
import com.oauth2.Drug.DUR.Dto.DurUserContext;
import com.oauth2.Drug.DUR.Service.DurCheckService;
import com.oauth2.HealthSupplement.DUR.Dto.SupplementSearchDurDto;
import com.oauth2.HealthSupplement.Search.Dto.SupplementSearchIndexDto;
import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplement;
import com.oauth2.HealthSupplement.SupplementInfo.Repository.HealthSupplementRepository;
import com.oauth2.User.TakingSupplement.Entity.TakingSupplement;
import com.oauth2.User.TakingSupplement.Repository.TakingSupplementRepository;
import com.oauth2.User.UserInfo.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplementDurTaggingService {

    private final HealthSupplementRepository healthSupplementRepository;
    private final SupplementDurCheckService supplementDurCheckService;
    private final DurCheckService durCheckService;
    private final TakingSupplementRepository takingSupplementRepository;

    public List<SupplementSearchDurDto> generateTagsForSupplements(User user, List<SupplementSearchIndexDto> supplements) throws JsonProcessingException {

        //사용자의 복약정보 (약품)관련 dur 정보를 불러옴
        DurUserContext drugUserContext = durCheckService.buildUserContext(user);
        DurUserContext supplementUserContext = supplementDurCheckService.buildUserContext(user);

        //검색된 건기식들과 사용자가 복약중인 약품들간의 dur 체크
        List<Long> supplmentIds = supplements.stream().map(SupplementSearchIndexDto::id).toList();
        Map<Long, HealthSupplement> supplementMap = healthSupplementRepository.findAllById(supplmentIds).stream()
                .collect(Collectors.toMap(HealthSupplement::getId, hs->hs));

        // 건기식 정보로 수정하기
        List<Long> takingSupplements = takingSupplementRepository.findByUser(user).stream()
                .map(TakingSupplement::getId).toList();

        List<SupplementSearchDurDto> result = new ArrayList<>();
        for (SupplementSearchIndexDto supplementDto : supplements) {
            HealthSupplement healthSupplement = supplementMap.get(supplementDto.id());
            if (healthSupplement == null) continue; // 약 정보를 찾을 수 없는 경우 건너뛰기
            Boolean isTaking = takingSupplements.contains(supplementDto.id());
            List<DurTagDto> tags = durCheckService.checkForInteractions(healthSupplement, DurType.SUPPLEMENT,user.getUserProfile(), supplementUserContext, drugUserContext);

            result.add(new SupplementSearchDurDto(
                    healthSupplement.getId(),
                    healthSupplement.getProductName(),
                    supplementDto.ingredient(),
                    healthSupplement.getEnterprise(),
                    "",
                    tags,
                    isTaking
            ));
        }
        return result;
    }

}
