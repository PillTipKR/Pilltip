package com.oauth2.AgenticAI.Tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oauth2.AgenticAI.Dto.ProductTool.FillteredDto;
import com.oauth2.AgenticAI.Dto.ProductTool.ProductCandidate;
import com.oauth2.AgenticAI.Dto.ProductTool.DurFilterRequest;
import com.oauth2.AgenticAI.Dto.ProductTool.DurFilterResult;
import com.oauth2.AgenticAI.Util.SessionUtils;
import com.oauth2.Drug.DUR.Service.DrugDurTaggingService;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DrugInfo.Domain.DrugEffect;
import com.oauth2.Drug.DrugInfo.Repository.DrugEffectRepository;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.User.UserInfo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DurFilterTool {

    // ⬇ 너희 실제 도메인 서비스/리포지토리 “직접” 주입
    private final DrugDurTaggingService durTaggingService;
    private final DrugRepository drugRepository;
    private final UserRepository userRepository; // 이름/패키지는 너희 프로젝트에 맞춰
    private final DrugEffectRepository drugEffectRepository;

    @Tool(name = "DurFilterTool", description = "후보 제품을 현재 사용자 기준 DUR로 필터링(서버가 사용자 주입).")
    public DurFilterResult filter(DurFilterRequest req) throws JsonProcessingException {

        // 1) 사용자 조회: req.userId → ThreadLocal(SessionCtx) → SecurityContext 순
        Long userId = SessionUtils.userId();
        var user = (userId != null)
                ? userRepository.findById(userId).orElse(null)
                : null;
        // 3) 후보별 평가
        List<FillteredDto> kept = new ArrayList<>();
        int removed = 0;
        for (ProductCandidate c : req.candidates()) {
            Drug drug = drugRepository.findByName(c.name().replaceAll("제품:","")).orElse(null);

            if (drug == null) {
                System.out.println("isNull");
                removed++;
                continue;
            }
            String effect = drugEffectRepository.findContentsByDrugIdAndType(drug.getId(), DrugEffect.Type.EFFECT).get(0);

            if (durTaggingService.checkDrugsDur(user,drug)) {
                kept.add(new FillteredDto(drug.getName(), effect));
            } else {
                removed++;
            }
        }

        return new DurFilterResult(kept, removed);
    }
}
