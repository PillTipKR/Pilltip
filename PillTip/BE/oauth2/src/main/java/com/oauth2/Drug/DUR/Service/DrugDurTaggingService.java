package com.oauth2.Drug.DUR.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oauth2.Drug.DUR.Dto.DurTagDto;
import com.oauth2.Drug.DUR.Dto.DurUserContext;
import com.oauth2.Drug.DUR.Dto.SearchDurDto;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.Drug.Search.Dto.SearchIndexDTO;
import com.oauth2.HealthSupplement.DUR.Service.SupplementDurCheckService;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.TakingPill.Entity.TakingPill;
import com.oauth2.User.TakingPill.Repositoty.TakingPillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DrugDurTaggingService {

    private final DrugRepository drugRepository;
    private final DurCheckService durCheckService; // 새로 추가된 서비스
    private final SupplementDurCheckService supplementDurCheckService;
    private final TakingPillRepository takingPillRepository;

    public List<SearchDurDto> generateTagsForDrugs(User user, List<SearchIndexDTO> drugs) throws JsonProcessingException {
        DurUserContext drugUserContext = durCheckService.buildUserContext(user);
        DurUserContext supplementUserContext = supplementDurCheckService.buildUserContext(user);

        List<Long> drugIds = drugs.stream().map(SearchIndexDTO::id).toList();
        Map<Long, Drug> drugMap = drugRepository.findAllById(drugIds).stream()
                .collect(Collectors.toMap(Drug::getId, drug -> drug));

        List<Long> takingPills = takingPillRepository.findByUser(user).stream()
                .map(TakingPill::getId).toList();

        List<SearchDurDto> result = new ArrayList<>();
        for (SearchIndexDTO drugDto : drugs) {
            Drug drug = drugMap.get(drugDto.id());
            if (drug == null) continue; // 약 정보를 찾을 수 없는 경우 건너뛰기
            Boolean isTaking = takingPills.contains(drugDto.id());
            List<DurTagDto> tags = durCheckService.checkForDrugAndSupplement(drug, user.getUserProfile(), drugUserContext, supplementUserContext);

            result.add(new SearchDurDto(
                    drug.getId(),
                    drug.getName(),
                    drugDto.ingredient(),
                    drug.getManufacturer(),
                    drug.getImage(),
                    tags,
                    isTaking
            ));
        }
        return result;
    }

}
