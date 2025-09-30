package com.oauth2.mlInference.Controller;

import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Service.AccountService;
import com.oauth2.Drug.DUR.Dto.SearchDurDto;
import com.oauth2.Drug.Search.Dto.SearchIndexDTO;
import com.oauth2.Drug.Search.Service.DrugSearchService;
import com.oauth2.mlInference.Service.mlInferenceService;
import com.oauth2.mlInference.Dto.PredictionResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/ml")
public class mlInferenceController {

    @Autowired
    private mlInferenceService mlInferenceService;

    @Autowired
    private DrugSearchService drugSearchService;

    @Autowired
    private com.oauth2.Drug.DUR.Service.DrugDurTaggingService drugDurTaggingService;

    @Autowired
    private AccountService accountService;

    @Value("${elastic.drug.drug}")
    private String drugField;

    @Value("${elastic.page}")
    private int pageSize;

    @PostMapping(value = "/predict", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> predict(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("upload-", multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);

        ResponseEntity<String> response = mlInferenceService.predictRaw(tempFile);
        tempFile.delete();
        return response;
    }

    @PostMapping(value = "/predict-and-search", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<com.oauth2.Account.Dto.ApiResponse<List<SearchDurDto>>> predictAndSearch(
            @AuthenticationPrincipal Account account,
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam(defaultValue = "0") int page,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws IOException {
        File tempFile = File.createTempFile("upload-", multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);

        try {
            PredictionResponse prediction = mlInferenceService.predict(tempFile);
            if (prediction == null || prediction.getResults() == null || prediction.getResults().isEmpty()) {
                return ResponseEntity.badRequest().body(com.oauth2.Account.Dto.ApiResponse.error("No prediction result", null));
            }
            String pillNameKo = prediction.getResults().get(0).getClassName();

            com.oauth2.User.UserInfo.Entity.User user = accountService.findUserByProfileId(profileId, account.getId());
            if (user == null) {
                return ResponseEntity.badRequest().body(com.oauth2.Account.Dto.ApiResponse.error("User not authenticated", null));
            }

            List<SearchIndexDTO> searchIndexDTOList = drugSearchService.getDrugSearch(pillNameKo, drugField, pageSize, page);
            List<SearchDurDto> result = drugDurTaggingService.generateTagsForDrugs(user, searchIndexDTOList);

            return ResponseEntity.ok(com.oauth2.Account.Dto.ApiResponse.success(result));
        } finally {
            tempFile.delete();
        }
    }
}
