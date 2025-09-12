package com.oauth2.HealthSupplement.Import.SupplementImport.Controller;

import com.oauth2.HealthSupplement.Import.SupplementImport.Service.IntakeImportService;
import com.oauth2.HealthSupplement.Import.SupplementImport.Service.RatioService;
import com.oauth2.HealthSupplement.Import.SupplementImport.Service.SupplementFileParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/import/supplement")
@RequiredArgsConstructor
public class SupplementImportController {

    private final SupplementFileParser supplementFileParser;
    private final RatioService ratioService;
    private final IntakeImportService intakeImportService;

    @Value("${supplement}")
    private String supplement;

    @PostMapping("")
    public String importAllRawData() throws IOException {
        // URL 디코딩
        supplementFileParser.importFromFile(supplement);
        return "import success";
    }

    @PostMapping("/ratio")
    private String importRatio() throws IOException {
        ratioService.processRatioFile();
        return "import success";
    }

    @PostMapping("/intake")
    private String importIntake() throws IOException {
        intakeImportService.parseAndSave();
        return "import success";
    }

}
