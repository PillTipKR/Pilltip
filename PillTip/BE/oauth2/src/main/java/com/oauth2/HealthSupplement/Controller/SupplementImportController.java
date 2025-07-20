package com.oauth2.HealthSupplement.Controller;

import com.oauth2.HealthSupplement.Service.SupplementFileParser;
import com.oauth2.HealthSupplement.Service.SupplementService;
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

    private final SupplementService supplementService;
    private final SupplementFileParser supplementFileParser;

    @Value("${supplement}")
    private String supplement;

    @PostMapping("")
    public String importAllRawData() throws IOException {
        // URL 디코딩
        supplementFileParser.importFromFile(supplement);
        return "import success";
    }
}
