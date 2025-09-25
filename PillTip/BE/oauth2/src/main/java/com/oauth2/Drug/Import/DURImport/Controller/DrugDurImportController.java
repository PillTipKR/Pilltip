package com.oauth2.Drug.Import.DURImport.Controller;

import com.oauth2.Drug.Import.DURImport.Service.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/import/dur")
public class DrugDurImportController {

    private final TherapeuticDupService drugTherapeuticDupService;
    private final DrugCautionService drugCautionService;
    private final DrugInteractionService drugInteractionService;
    private final DrugIngrInteractionService drugIngrInteractionService;
    private final DrugIngrCautionService drugIngrCautionService;
    private final Logger logger = LoggerFactory.getLogger(DrugDurImportController.class);

    // txt 파일 업로드 & 파싱 실행
    @PostMapping("/therDup")
    public ResponseEntity<String> importTherDupFromPath() {
        try {
            drugTherapeuticDupService.parseAndSaveTherapeuticDup();
            return ResponseEntity.ok("DUR 효능군 중복 데이터가 성공적으로 저장되었습니다.");
        } catch (IOException e) {
            logger.error("Error occurred in import therDup: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("파일 처리 중 오류 발생: " + e.getMessage());
        }
    }

    @PostMapping("/caution/all")
    public ResponseEntity<String> importCautionFromPath() {
        try {
            drugCautionService.parseIngrAll();
            drugIngrCautionService.parseIngrAll();
            drugCautionService.parseAllAndSave();
            return ResponseEntity.ok("주의 정보 저장 완료");
        } catch (Exception e) {
            logger.error("Error occurred in import caution: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("에러: " + e.getMessage());
        }
    }

    @PostMapping("/all")
    public ResponseEntity<String> saveCaution() {
        try {
            drugCautionService.parseAllAndSave();
            return ResponseEntity.ok("주의 정보 저장 완료");
        } catch (Exception e) {
            logger.error("Error occurred in import caution: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("에러: " + e.getMessage());
        }
    }

    @PostMapping("/interaction")
    public ResponseEntity<String> saveInteraction() {
        try {
            drugInteractionService.loadAll();
            return ResponseEntity.ok("주의 정보 저장 완료");
        } catch (Exception e) {
            logger.error("Error occurred in import interaction: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("에러: " + e.getMessage());
        }
    }

    @PostMapping("/interaction/all")
    public ResponseEntity<String> saveInteractionAll() {
        try {
            drugInteractionService.loadAll();
            drugIngrInteractionService.loadIng();
            return ResponseEntity.ok("주의 정보 저장 완료");
        } catch (Exception e) {
            logger.error("Error occurred in import interaction: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("에러: " + e.getMessage());
        }
    }

    @PostMapping("/interactionIng")
    public ResponseEntity<String> saveIngInteraction() {
        try {
            drugIngrInteractionService.loadIng();
            return ResponseEntity.ok("주의 정보 저장 완료");
        } catch (Exception e) {
            logger.error("Error occurred in import interaction: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("에러: " + e.getMessage());
        }
    }
}
