package com.oauth2.Drug.Import.DrugImport.Controller;

import com.oauth2.Drug.Import.DrugImport.Service.DrugImportService;
import com.oauth2.Drug.Import.DrugImport.Service.IngredientImportService;
import com.oauth2.Drug.Import.DrugImport.Service.PromptImporter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RequestMapping("/api/import")
@RestController
@RequiredArgsConstructor
public class DrugImportController {

    @Value("${drug1}")
    private String drug1;

    @Value("${drug2}")
    private String drug2;

    @Value("${drug3}")
    private String drug3;

    @Value("${dir}")
    private String dir;

    @Value("${promptCaution1}")
    private String promptCaution1;

    @Value("${promptCaution2}")
    private String promptCaution2;

    @Value("${promptCaution3}")
    private String promptCaution3;

    @Value("${promptIngCaution}")
    private String promptIngCaution;

    @Value("${promptDrug1}")
    private String promptDrug1;

    @Value("${promptDrug2}")
    private String promptDrug2;

    @Value("${promptDrug3}")
    private String promptDrug3;

    @Value("${promptDrug4}")
    private String promptDrug4;

    @Value("${promptDrug5}")
    private String promptDrug5;

    @Value("${promptDrug6}")
    private String promptDrug6;

    @Value("${promptDrug7}")
    private String promptDrug7;

    @Value("${promptDrug8}")
    private String promptDrug8;


    private final DrugImportService drugImportService;
    private final IngredientImportService ingredientImportService;
    private final PromptImporter promptImporter;

    @PostMapping("/drugs")
    public String importAllRawData() throws IOException {
        // URL 디코딩
        rawData();
        return "import success";
    }

    private void rawData() throws IOException {
        drugImportService.importFromFile(drug1);
        drugImportService.importFromFile(drug2);
        drugImportService.importFromFile(drug3);
    }

    @PostMapping("/all")
    public String importAll() throws IOException {
        rawData();
        return importAllPrompt();
    }

    @PostMapping("/prompt/all")
    public String importAllPrompt(){
        importCautionPrompt();
        return getPromptedDrug();
    }

    @PostMapping("/prompt/drugs")
    public String importDrugPrompt(){
        return getPromptedDrug();
    }

    private String getPromptedDrug() {
        promptImporter.importPromptInfo(promptDrug1);
        promptImporter.importPromptInfo(promptDrug2);
        promptImporter.importPromptInfo(promptDrug3);
        promptImporter.importPromptInfo(promptDrug4);
        promptImporter.importPromptInfo(promptDrug5);
        promptImporter.importPromptInfo(promptDrug6);
        promptImporter.importPromptInfo(promptDrug7);
        promptImporter.importPromptInfo(promptDrug8);

        System.out.println("약 정보 주입 완료");
        return "import success";
    }

    private void importCautionPrompt(){
        promptImporter.importPromptCaution(promptCaution1);
        promptImporter.importPromptCaution(promptCaution2);
        promptImporter.importPromptCaution(promptCaution3);
        System.out.println("주의사항 정보 주입 완료");
    }

    private void importIngrCaution() throws IOException {
        promptImporter.importPromptIngCaution(promptIngCaution);
    }

    @PostMapping("/prompt/caution/all")
    public void importCaution() throws IOException {
        importCautionPrompt();
        importIngrCaution();
    }



    @PostMapping("/ingredients")
    public ResponseEntity<?> uploadIngredients(@RequestParam("file") MultipartFile multipartFile) throws IOException {

        // 저장 경로 지정
        Path path = Paths.get(dir + multipartFile.getOriginalFilename());

        // 파일 저장
        multipartFile.transferTo(path);

        ingredientImportService.importIngredientsFromCsv(multipartFile);
        return ResponseEntity.ok("성분 데이터 임포트 완료");
    }

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        // 저장 경로 지정
        Path path = Paths.get(dir + multipartFile.getOriginalFilename());

        // 파일 저장
        multipartFile.transferTo(path);

        // 이후 서비스 호출
        drugImportService.importImageFromCsv(multipartFile);

        return ResponseEntity.ok("이미지 세팅 완료");
    }


} 