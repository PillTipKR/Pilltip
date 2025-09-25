package com.oauth2.AgenticAI.Tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oauth2.AgenticAI.Service.RagSearchService;
import com.oauth2.AgenticAI.Util.SessionUtils;
import com.oauth2.Drug.DUR.Domain.DurEntity;
import com.oauth2.Drug.DUR.Domain.DurType;
import com.oauth2.Drug.DUR.Dto.DurAnalysisResponse;
import com.oauth2.Drug.DUR.Service.DurService;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.UserInfo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DurTool {


    private final RagSearchService search;
    private final DurService dur;
    private final UserRepository userRepository;


    /** @Tool: 모델이 자동으로 호출할 메서드 */
    @Tool(name = "DurTool", description = "두 개의 약품 성분 또는 제품명(name1, name2)을 받아 두 약품 간의 상호작용(DUR) 정보를 검색하고 분석")
    public DurAnalysisResponse run(
            @P("name1") String name1,
            @P("name2") String name2
    ) throws JsonProcessingException {
        int topK = 1;
        var doc1 = search.searchDur(name1, topK);
        var doc2 = search.searchDur(name2, topK);

        String docid1 = doc1.get(0).getMetadata().get("ruleId").toString();
        DurType durType1 = getDurType(doc1.get(0).getMetadata().get("durType").toString());
        String docid2 = doc2.get(0).getMetadata().get("ruleId").toString();
        DurType durType2 = getDurType(doc2.get(0).getMetadata().get("durType").toString());
        User user = userRepository.findById(SessionUtils.userId()).orElse(null);
        if(user!=null && durType1 != null && durType2 != null) {
            return dur.generateTagsForDurEntities(user,
                    Long.parseLong(docid1),
                    Long.parseLong(docid2),
                    durType1,durType2);
        }
        return null;
    }

    private DurType getDurType(String text) {
        if(text.contains("DRUGINGR")) return DurType.DRUGINGR;
        else if(text.contains("SUPPLEMENT")) return DurType.SUPPLEMENT;
        else if(text.contains("DRUG")) return DurType.DRUG;
        else if(text.contains("SUPINGR")) return DurType.SUPINGR;
        else return null;
    }
}
