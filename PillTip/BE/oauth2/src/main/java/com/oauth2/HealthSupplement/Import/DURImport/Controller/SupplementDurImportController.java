package com.oauth2.HealthSupplement.Import.DURImport.Controller;

import com.oauth2.HealthSupplement.Import.DURImport.Service.HealthSupplementCautionService;
import com.oauth2.HealthSupplement.Import.DURImport.Service.SupplementInteractionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/import/supplement/dur")
public class SupplementDurImportController {

    private final SupplementInteractionService supplementInteractionService;
    private final HealthSupplementCautionService healthSupplementCautionService;
    private final Logger logger = LoggerFactory.getLogger(SupplementDurImportController.class);


    @PostMapping("/interaction")
    public ResponseEntity<String> saveIngInteraction() {
        try {
            supplementInteractionService.loadIng();
            return ResponseEntity.ok("주의 정보 저장 완료");
        } catch (Exception e) {
            logger.error("Error occurred in import interaction: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("에러: " + e.getMessage());
        }
    }

    @PostMapping("")
    public ResponseEntity<String> saveDur() throws IOException {
        healthSupplementCautionService.load();
        return ResponseEntity.ok("저장 완료!");
    }
}
